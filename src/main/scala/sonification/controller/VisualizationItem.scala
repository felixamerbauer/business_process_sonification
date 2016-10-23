package sonification.controller

import java.awt.Color

import org.deckfour.xes.model.XEvent

import sonification.ui.left.XY

/**
 * Required info to draw a shape
 * @param xy drawing coordinates
 * @param shape shape in use
 * @param color color of shape
 * @param instrument sonification property
 * @param melody sonification property
 * @param volume sonification property
 * @param panning sonification property
 */
case class VisualizationItem(
    val xy: XY,
    val shapeValue: Any,
    val shape: Enabler[UIShape],
    val colorValue: Any,
    val color: Enabler[Color],
    // whether instrument or rhythm
    val instrument: Boolean,
    val instrumentDrumValue: Any,
    val instrumentDrum: Enabler[String],
    val melodyRhythmValue: Any,
    val melodyRhythm: Enabler[String],
    val volumeValue: Option[Any],
    val volume: Option[Int],
    val panningValue: Option[Any],
    val panning: Option[Int],
    val event: XEvent) {
  override def toString = s"$xy/$shapeValue/$shape/$colorValue/[${color.value.getRed}|${color.value.getGreen}|${color.value.getBlue} ${color.enabled}]/$instrumentDrumValue/$instrumentDrum/$melodyRhythmValue/$melodyRhythm/$volumeValue/$volume/$panning"
}

