import org.jfugue.player.Player
package object jfugue {
  
  trait PlayerProvider {
    implicit val player = new Player()
  }
  
  implicit class StringImprovements(val s: String) {
    def playNew() = new Player().play(s)
    def play()(implicit player: Player) = player.play(s)
  }

}


