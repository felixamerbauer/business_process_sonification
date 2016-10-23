package sonification.controller

import org.jfugue.player.Player

object Test extends App {
  val ms ="T120 X[BALANCE_COARSE]=0 C D E F G A B"
  new Player().play(ms)
}