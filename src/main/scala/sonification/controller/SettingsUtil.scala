package sonification.controller

import java.awt.Color
import scala.collection.mutable.{ Map => MMap }
import org.deckfour.xes.model.{ XEvent, XLog }
import com.typesafe.scalalogging.StrictLogging
import sonification.Metrics.{ TSU_defaultSettings, TSU_literalMapping1, TSU_literalMapping2, TSU_sonificationForCategory, TSU_updateSonification, TSU_visualizationForCategory, t }
import sonification.controller.MappingModel.{ Mapping, MappingCategoriesContinuous, MappingCategoriesLiteralEvent, MappingCategoriesLiteralTrace, SonificationToVisualization }
import sonification.music.MusicCommons._
import sonification.openxes.OpenXESHelper.RichLog
import org.deckfour.xes.model.XElement
import org.deckfour.xes.model.XTrace

object SettingsUtil extends StrictLogging {

  /**
   * TODO comment
   */
  def defaultSettings(traces: XLog): Settings = t(TSU_defaultSettings) {

    def findWithAtLeast1ValueSeq[T](toCheck: Seq[MappingCategory[T, XEvent]], acc: Seq[(MappingCategory[T, XEvent], Seq[T])] = Seq()): Seq[(MappingCategory[T, XEvent], Seq[T])] = toCheck match {
      case Nil => acc
      case head :: tail =>
        val values: Seq[T] = traces.eventValues[T](toCheck.head)
        if (!values.isEmpty) findWithAtLeast1ValueSeq(toCheck.tail, acc :+ (toCheck.head, values))
        else findWithAtLeast1ValueSeq(toCheck.tail, acc)
    }

    def findWithAtLeast1ValueSet[T](toCheck: Seq[MappingCategory[T, XEvent]], acc: Seq[(MappingCategory[T, XEvent], Seq[T])] = Seq()): Seq[(MappingCategory[T, XEvent], Seq[T])] = toCheck match {
      case Nil => acc
      case head :: tail =>
        val values: Seq[T] = traces.eventValues(toCheck.head).distinct
        if (!values.isEmpty) findWithAtLeast1ValueSet(toCheck.tail, acc :+ (toCheck.head, values))
        else findWithAtLeast1ValueSet(toCheck.tail, acc)
    }

    // literal
    val literals = findWithAtLeast1ValueSeq(MappingCategoriesLiteralEvent)
    // continuous
    val continuous = findWithAtLeast1ValueSet(MappingCategoriesContinuous)

    val mapping = MMap[MappingCategory[_, _ <: XElement], CategoryMapping]()
    literals.zipWithIndex foreach {
      case (literal, idx) =>
        val (category, values) = literal
        mapping += category -> {
          if (idx == 0) CategoryMapping(
            Some(ColorMapping(values.seq.distinct.sorted zip ColorsStream)),
            Some(MelodyMapping(values.seq.distinct.sorted zip MelodiesStream)))
          else if (idx == 1)
            CategoryMapping(
              Some(ShapeMapping(values.seq.distinct.sorted zip ShapesStream)),
              Some(InstrumentMapping(values.seq.distinct.sorted zip JFugueInstrumentsStream)))
          else CategoryMapping(None, None)
        }
    }
    // add trace level mappings
    MappingCategoriesLiteralTrace foreach { literal =>
      mapping += literal -> CategoryMapping(None, None)
    }

    for (idx <- 0 until continuous.length) {
      val (category, values) = continuous(idx)
      if (idx == 0) mapping += category -> CategoryMapping(None, Some(VolumeMapping(values)))
      else if (idx == 1) mapping += category -> CategoryMapping(None, Some(PanningMapping(values)))
      else mapping += category -> CategoryMapping(None, None)
    }

    // complete settings
    val settings = new Settings(
      // TODO check why necessary
      mapping = mapping.toMap.asInstanceOf[Map[MappingCategory[_, _ <: XElement], CategoryMapping]],
      traces = 0 until traces.size toSet)
    logger.debug(s"default setting $settings")
    settings
  }

  def updateSonification(category: MappingCategory[_, _ <: XElement], sonificationType: Option[SonificationType], traces: XLog, mapping: Mapping): Mapping = t(TSU_updateSonification) {

    // 1) update the target MappingCategory
    val categoryMapping = mapping(category)
    val updatedSonification = sonificationType.map(e => sonificationForCategory(category, e, traces))
    val visualizationType = sonificationType.flatMap(SonificationToVisualization)
    val updatedVisualization = visualizationType.map(e => visualizationForCategory(category, e, traces))
    var updatedMapping = mapping + (category -> categoryMapping.copy(sonification = updatedSonification, visualization = updatedVisualization))
    // 2) if another MappingCategory used this sonification set it to not mapped or
    // if instrument or melody disable potential drum rhythm and vice versa
    val toDisable: Set[SonificationType] = sonificationType match {
      case Some(InstrumentType) | Some(MelodyType) => Set(DrumType, RhythmType)
      case Some(DrumType) | Some(RhythmType) => Set(InstrumentType, MelodyType)
      case _ => Set()
    }
    println(s"toDisable $toDisable")
    for {
      (mappingCategory, categoryMapping) <- mapping if mappingCategory != category
      _ = println(s"yyy $mappingCategory - $categoryMapping")
      sonification <- categoryMapping.sonification if sonificationType.exists(_ == sonification.typ) || toDisable.contains(sonification.typ)
    } {
      updatedMapping += mappingCategory -> categoryMapping.copy(sonification = None, visualization = None)
    }
    logger.debug("updatedMapping\n" + updatedMapping)
    updatedMapping
  }

  private def sonificationForCategory(category: MappingCategory[_, _ <: XElement], sonificationType: SonificationType, traces: XLog): SonificationMapping = t(TSU_sonificationForCategory) {
    val values =
      if (category.isEvent) traces.eventValues(category.asInstanceOf[MappingCategory[_, XEvent]])
      else traces.traceValues(category.asInstanceOf[MappingCategory[_, XTrace]])

    sonificationType match {
      case InstrumentType => InstrumentMapping(values.asInstanceOf[Seq[String]].seq.distinct.sorted zip JFugueInstrumentsStream)
      case MelodyType => MelodyMapping(values.asInstanceOf[Seq[String]].seq.distinct.sorted zip MelodiesStream)
      case DrumType => DrumMapping(values.asInstanceOf[Seq[String]].seq.distinct.sorted zip DrumStream)
      case RhythmType => RhythmMapping(values.asInstanceOf[Seq[String]].seq.distinct.sorted zip RhythmsStream)
      case VolumeType => VolumeMapping(values.asInstanceOf[Seq[Double]])
      case PanningType => PanningMapping(values.asInstanceOf[Seq[Double]])
    }
  }

  private def visualizationForCategory(category: MappingCategory[_, _], visualizationType: VisualizationType, traces: XLog): VisualizationMapping = t(TSU_visualizationForCategory) {
    val values =
      if (category.isEvent) traces.eventValues(category.asInstanceOf[MappingCategory[_, XEvent]])
      else traces.traceValues(category.asInstanceOf[MappingCategory[_, XTrace]])

    visualizationType match {
      case ShapeType => ShapeMapping(values.asInstanceOf[Seq[String]].seq.distinct.sorted zip ShapesStream)
      case ColorType => ColorMapping(values.asInstanceOf[Seq[String]].seq.distinct.sorted zip ColorsStream)
    }
  }

  def literalMapping(sonification: Option[LiteralMapping[_]], visualization: Option[LiteralMapping[_]], key: String, enabled: Boolean): Unit = t(TSU_literalMapping1) {
    sonification.get.values(key).enabled = enabled
    visualization.get.values(key).enabled = enabled
  }

  def literalMapping(mapping: LiteralMapping[_], key: String, value: Any): Unit = t(TSU_literalMapping2) {
    mapping match {
      case e: ShapeMapping => e.values(key).value = value.asInstanceOf[UIShape]
      case e: ColorMapping => e.values(key).value = value.asInstanceOf[Color]
      case e: InstrumentMapping => e.values(key).value = value.asInstanceOf[String]
      case e: MelodyMapping => e.values(key).value = value.asInstanceOf[String]
      case e: DrumMapping => e.values(key).value = value.asInstanceOf[String]
      case e: RhythmMapping => e.values(key).value = value.asInstanceOf[String]
    }
  }
}