package sonification.ui.left

import java.awt.Color
import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.collection.JavaConversions.asScalaBuffer
import org.deckfour.xes.model.XLog
import com.typesafe.scalalogging.StrictLogging
import sonification.Metrics.{ TVC_calculateVisualization, t }
import sonification.controller.{ ColorMapping, Enabler, InstrumentMapping }
import sonification.controller.{ MelodyMapping, PanningMapping, Settings, ShapeMapping, UIShape, VisualizationItem, VolumeMapping }
import sonification.controller.MappingModel.{ MappingCategoriesContinuous, MappingCategoriesLiteral }
import sonification.openxes.OpenXESHelper
import sonification.openxes.OpenXESHelper.{ RichLog, RichTrace }
import sonification.controller.MappingCategory
import org.deckfour.xes.model.XTrace
import org.deckfour.xes.model.XEvent
import sonification.controller.DrumMapping
import sonification.controller.RhythmMapping
import sonification.controller.InstrumentType

/**
 * @author felixamerbauer
 */
object VisusalizationCalculator extends StrictLogging {
  // defines the item size and padding for the visualization
  val ItemSizeMain = 20
  //  ...and the overview
  val ItemSizeOverview = 10
  val Padding = 1
  // to keep it simple
  assert(ItemSizeMain % 2 == 0)
  assert(ItemSizeOverview % 2 == 0)

  def calcX(width: Int, position: Double, itemWidth: Int): Int = Math.round(width * position + itemWidth / 2).toInt

  /**
   * Calculate shapes to draw
   * @param width width of drawing area
   * @param height height of drawing area
   * @param padding empty space on each side (items may have their center on the border of the padding as the padding is big enough so that the items are not drawn beyond the container)
   * @return items/shapes to draw
   */
  def calculateVisualization(traces: XLog, settings: Settings, width: Int, height: Int, itemSize: Int): Seq[VisualizationItem] = t(TVC_calculateVisualization) {
    logger.debug(s"calculateVisualization width=$width, height=$height, itemSize=$itemSize")
    // calculate the visualization only when possible otherwise return an empty list
    if (!settings.traces.isEmpty) {
      // TODO result should be cached
      val isInstrument = settings.mapping.values.exists(_.sonification.exists(e=>e.typ == InstrumentType))
      val (firstStart, lastEnd, totalDuration) = traces.firstStartLastEndTotalDuration
      (for {
        (trace, traceIdx) <- traces.zipWithIndex
        (traceStart, traceEnd, traceDuration) = trace.startEndDuration
        event <- trace
        // time is mandatory
        time <- OpenXESHelper.timeTransitionAttributeValue(event)
      } yield {
        var shape: Enabler[UIShape] = null
        var shapeValue: Any = null
        var color: Enabler[Color] = null
        var colorValue: Any = null
        var instrumentDrum: Enabler[String] = null
        var instrumentDrumValue: Any = null
        var melodyRhythm: Enabler[String] = null
        var melodyRhythmValue: Any = null
        var volume: Option[Int] = None
        var volumeValue: Option[Any] = None
        var panning: Option[Int] = None
        var panningValue: Option[Any] = None
        // Literal Mappings
        for {
          category <- MappingCategoriesLiteral
          isEvent = category.isEvent
          key <- {
            // TODO simplify
            if (isEvent) category.asInstanceOf[MappingCategory[String, XEvent]].extractor(event)
            else category.asInstanceOf[MappingCategory[String, XTrace]].extractor(trace)
          }
          //          _ = println(settings)
          categoryMapping = settings.mapping(category)
        } {
          categoryMapping.visualization foreach (_ match {
            case s: ShapeMapping =>
              shapeValue = key
              shape = s.values(key)
            case c: ColorMapping =>
              colorValue = key
              color = c.values(key)
          })
          // sonification info for tooltip
          categoryMapping.sonification foreach (_ match {
            case i: InstrumentMapping =>
              instrumentDrumValue = key
              instrumentDrum = i.values(key)
            case d: DrumMapping =>
              instrumentDrumValue = key
              instrumentDrum = d.values(key)
            case m: MelodyMapping =>
              melodyRhythmValue = key
              melodyRhythm = m.values(key)
            case r: RhythmMapping =>
              melodyRhythmValue = key
              melodyRhythm = r.values(key)
            case _ =>
          })
        }
        // Continuous Mappings
        for {
          category <- MappingCategoriesContinuous
          key <- category.extractor(event)
          categoryMapping = settings.mapping(category) if categoryMapping.sonification.isDefined
        } {
          categoryMapping.sonification foreach (_ match {
            case p: PanningMapping =>
              panningValue = Some(key)
              panning = Some(p.values(key))
            case v: VolumeMapping =>
              volumeValue = Some(key)
              volume = Some(v.values(key))
            case _ =>
          })
        }
        // visualization
        val position = (time.getTime - firstStart.getTime) / totalDuration.toDouble
        val x = calcX(width - itemSize, position, itemSize)
        val effectiveHeight = height - itemSize - Padding * 2
        val y = (traceIdx + 1) * effectiveHeight / (traces.size + 1) + itemSize / 2 + VisusalizationCalculator.Padding
        Some(VisualizationItem(
          XY(x, y),
          shapeValue,
          shape,
          colorValue,
          color,
          isInstrument,
          instrumentDrumValue,
          instrumentDrum,
          melodyRhythmValue,
          melodyRhythm,
          volumeValue,
          volume,
          panningValue,
          panning,
          event))
      }).flatten
    } else Seq()
  }

}