package sonification

import java.awt.{ Component, Container, Graphics2D }
import java.awt.Color
import java.awt.Color.{ RED, WHITE, blue, cyan, gray, green, magenta, orange, pink, red, yellow }
import java.awt.event.{ ActionEvent, ActionListener }
import java.text.SimpleDateFormat
import scala.collection.mutable.Buffer
import org.processmining.framework.util.ui.widgets.ProMTextField
import com.typesafe.scalalogging.StrictLogging
import javax.swing.Timer
import javax.swing.event.{ ChangeEvent, ChangeListener, DocumentEvent, DocumentListener }
import sonification.controller.{ Controller, LiteralMapping }
import sonification.music.MelodyStaccatoValidationCache
import sonification.ui.left.Shapes.{ createCircle, createCross, createRectangle, createRhombus, createStar }
import sonification.ui.left.XY
import java.io.File
import java.nio.file.Paths
import sonification.music.RhythmStaccatoValidationCache

package object controller extends StrictLogging {
  
  sealed trait State
  case object Stop extends State
  case object Play extends State
  case object Pause extends State

  case class ZoomLevel(label: String, zoomFactor: Int)

  val FitPage = ZoomLevel("Fit Page", 1)

  val ZoomLevels = Seq(
    FitPage,
    ZoomLevel("200%", 2),
    ZoomLevel("500%", 5),
    ZoomLevel("1.000%", 10),
    ZoomLevel("2.000%", 20),
    ZoomLevel("5.000%", 50))

  implicit def code2actionListener(code: => Unit): ActionListener = new ActionListener {
    override def actionPerformed(event: ActionEvent) { code }
  }
  implicit def code2changeListener(code: => Unit): ChangeListener = new ChangeListener {
    override def stateChanged(event: ChangeEvent) { code }
  }

  // TODO maybe a type alias would be enough
  sealed abstract class UIShape(val name: String) extends Serializable {
    def draw(xy: XY, color: Color, itemsize: Int)(implicit g2d: Graphics2D): Unit
    override def toString = name
  }
  final object Square extends UIShape("Square") {
    override def draw(xy: XY, color: Color, itemsize: Int)(implicit g2d: Graphics2D) {
      g2d.setColor(color)
      val square = createRectangle(xy, itemsize)
      // TODO check difference draw/fill
      g2d.draw(square)
      g2d.fill(square)
    }
  }
  final object Circle extends UIShape("Circle") {
    override def draw(xy: XY, color: Color, itemsize: Int)(implicit g2d: Graphics2D) {
      g2d.setColor(color)
      val circle = createCircle(xy, itemsize / 2)
      g2d.draw(circle)
      g2d.fill(circle)
    }
  }
  final object Star extends UIShape("Star") {
    override def draw(xy: XY, color: Color, itemsize: Int)(implicit g2d: Graphics2D) {
      g2d.setColor(color)
      val star = createStar(xy, itemsize)
      g2d.draw(star)
      g2d.fill(star)
    }
  }
  final object Cross extends UIShape("Cross") {
    override def draw(xy: XY, color: Color, itemsize: Int)(implicit g2d: Graphics2D) {
      g2d.setColor(color)
      val cross = createCross(xy, itemsize)
      g2d.draw(cross)
      g2d.fill(cross)
    }
  }
  final object Rhombus extends UIShape("Rhombus") {
    override def draw(xy: XY, color: Color, itemsize: Int)(implicit g2d: Graphics2D) {
      g2d.setColor(color)
      val rhombus = createRhombus(xy, itemsize)
      g2d.draw(rhombus)
      g2d.fill(rhombus)
    }
  }
  // some color
  val Colors: Array[Color] = Array(red, green, blue, pink, yellow, gray, magenta, orange, cyan)
  // returns (endless) colors: green, magenta ... orange, pink ... cyan, green, magenta, ...
  val ColorsStream = Stream from (0) map (e => Colors(e % Colors.length))

  // curently supported shapes
  // TODO maybe use some case objects 
  val AllShapes: Array[UIShape] = Array(Square, Circle, Star, Cross, Rhombus)
  // returns (endles) shapes: rectangle, circle ... plus, rectangle ...
  val ShapesStream = Stream from (0) map (e => AllShapes(e % AllShapes.length))

  val VolumesStream = Stream.continually(100)

  val Pannings = Array("left", "center", "right")
  val PanningsStream = Stream.continually(Pannings).flatten


  // handle common behaviour for all delayed document listeners
  trait DelayedDocumentListener extends DocumentListener {
    final override def changedUpdate(e: DocumentEvent) {}
    final override def removeUpdate(e: DocumentEvent) { timer.restart() }
    final override def insertUpdate(e: DocumentEvent) { timer.restart() }
    def timer: Timer
  }

  def documentListener(tf: ProMTextField, sonification: LiteralMapping[_], key: String, code: (LiteralMapping[_], String, String) => Unit, delay: Int) = new DocumentListener with DelayedDocumentListener {
    //    logger.debug("initializing " + tf.getText())
    private class TimerListener extends ActionListener {

      override def actionPerformed(evt: ActionEvent) {
        logger.debug(s"action on $key ${tf.getText()}")
        code(sonification, key, tf.getText)
        val timer = evt.getSource.asInstanceOf[Timer]
        timer.stop()
      }
    }

    val timer: Timer = new Timer(delay, new TimerListener())
  }

  /**
   * delayed validator for text fields
   * @param tf tex field
   * @param delay time after change before validation is started
   * @param valid validation check
   * @return document listener that can be attached to text fields
   */
  def delayedValidator(tf: ProMTextField, delay: Int, valid: String => Boolean) = new DocumentListener with DelayedDocumentListener {
    //    logger.debug("initializing " + tf.getText())
    private class TimerListener extends ActionListener {

      override def actionPerformed(evt: ActionEvent) {
        val ok = valid(tf.getText())
        logger.debug(s"action on ${tf.getText()} => $ok")
        if (!ok) tf.setBackground(RED) else tf.setBackground(WHITE)
        val timer = evt.getSource.asInstanceOf[Timer]
        timer.stop()
      }
    }

    val timer: Timer = new Timer(delay, new TimerListener())

  }

  /**
   * delayed validator for text fields with music strings
   * @param tf text field
   * @param delay time after change before validation is started
   * @return document listener that can be attached to text fields
   */
  def melodyValidator(tf: ProMTextField, delay: Int, ctrl: Controller) = delayedValidator(tf, delay, MelodyStaccatoValidationCache.valid)

  def rhtythmValidator(tf: ProMTextField, delay: Int, ctrl: Controller) = delayedValidator(tf, delay, RhythmStaccatoValidationCache.valid)
  // helper for container with elements that can be enabled/disabled
  //  type StateChange = (State,State)
  trait Modifiable { self: Container =>
    val modifiables = Buffer[(Component, (State, State) => Boolean)]()
    val executables = Buffer[(State, State) => Unit]()

    def act(oldState: State, newState: State) {
      for ((component, changeFunction) <- modifiables) {
        component.setEnabled(changeFunction(oldState, newState))
      }
      executables foreach (_(oldState, newState))
    }
  }

  def enableFor(states: State*): (State, State) => Boolean = (_, newState) => states.contains(newState)

  val NoneStream: Stream[Option[Any]] = Stream.continually(None)

  def calculatePositionString(ms: Long, speed: Option[Double] = None): String = {
    def ps(speed: Double) = {
      val msCur = Math.round(ms / speed)
      val msRest = msCur % 1000
      val seconds = (msCur / 1000) % 60
      val minutes = (msCur / (1000 * 60)) % 60
      val hours = (msCur / (1000 * 60 * 60)) % 24
      val days = (msCur / (1000 * 60 * 60 * 24)).toInt
      val daysString = if (days > 0) s"$days days " else ""
      f"$daysString$hours%02d:$minutes%02d:$seconds%02d:$msRest%03d"
    }
    // unaltered speed
    val unaltered = ps(1d)
    speed map {e=>
      s"$unaltered > ${ps(e)}"      
    } getOrElse unaltered
  }
  
  val dtf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

}