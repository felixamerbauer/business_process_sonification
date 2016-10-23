package sonification.controller

import java.awt.Color
import scala.collection.immutable.TreeMap
import scala.collection.mutable.{ Map => MMap }
import org.deckfour.xes.model.{ XElement, XEvent, XTrace }
import MappingModel.{ coarsePanningMappingFunction, coarseVolumeMappingFunction }
import sonification.openxes.OpenXESHelper
import scala.collection.SortedSet

trait SimpleName {
  val simpleName = this.getClass.getSimpleName.replace("$", "")
}

// object only needed to allow definition of types and vals
object MappingModel {
  // mapping
  type Mapping = Map[MappingCategory[_, _ <: XElement], CategoryMapping]

  val VisualizationTypes = Set[VisualizationType](ShapeType, ColorType)

  val SonificationTypes = Set[SonificationType](InstrumentType, MelodyType, DrumType, RhythmType, VolumeType, PanningType)

  val SonificationToVisualization = Map[SonificationType, Option[VisualizationType]](
    InstrumentType -> Some(ShapeType),
    MelodyType -> Some(ColorType),
    DrumType -> Some(ShapeType),
    RhythmType -> Some(ColorType),
    VolumeType -> None,
    PanningType -> None)

  val MappingCategoriesLiteralEvent = Seq[MappingCategory[String, XEvent]](
    ConceptName,
    CostCurrency,
    LifecycleTransition,
    OrganizationGroup,
    OrganizationResource,
    OrganizationRole)

  val MappingCategoriesLiteralTrace = Seq[MappingCategory[String, XTrace]](
    TraceName)

  val MappingCategoriesLiteral = MappingCategoriesLiteralEvent ++ MappingCategoriesLiteralTrace

  val MappingCategoriesContinuous = Seq[MappingCategory[Double, XEvent]](CostTotal)

  val MappingCategories: Seq[MappingCategory[_, _]] = MappingCategoriesLiteral ++ MappingCategoriesContinuous

  val MappingCategoryTypes = Set[MappingCategoryType](Continuous, Literal)

  type ContinuousMappingFunction = Seq[Double] => Seq[Int]

  def out(min: Int, max: Int)(in: Seq[Double]): Seq[Int] = {
    val factor = (max - min) / (in.max - in.min)
    in.map { e =>
      val tmp = min + (e - in.min) * factor
      Math.round(tmp).toInt
    }
  }

  def distribution(min: Int, max: Int): ContinuousMappingFunction = out(min, max)

  // for midi volume coarse
  val coarseVolumeMappingFunction: ContinuousMappingFunction = out(10, 127)
  // for midi panning coarse
  val coarsePanningMappingFunction: ContinuousMappingFunction = out(0, 127)

}

sealed trait MappingCategoryType
final case object Continuous extends MappingCategoryType
final case object Literal extends MappingCategoryType

sealed trait MappingCategoryApplication
final case object Trace extends MappingCategoryApplication
final case object Event extends MappingCategoryApplication

// this contains all the information to know where to look in the XES file
sealed abstract class MappingCategory[T, U <: XElement](val extractor: U => Option[T], val typ: MappingCategoryType, val application: MappingCategoryApplication = Event) extends SimpleName with Serializable {
  //  import reflect.runtime.universe._
  val isEvent: Boolean = application == Event
}

final case object ConceptName extends MappingCategory[String, XEvent](OpenXESHelper.conceptNameAttributeValue, Literal)
final case object LifecycleTransition extends MappingCategory[String, XEvent](OpenXESHelper.lifecycleTransitionAttributeValue, Literal)
final case object OrganizationResource extends MappingCategory[String, XEvent](OpenXESHelper.organizationResourceAttributeValue, Literal)
final case object OrganizationGroup extends MappingCategory[String, XEvent](OpenXESHelper.organizationGroupAttributeValue, Literal)
final case object OrganizationRole extends MappingCategory[String, XEvent](OpenXESHelper.organizationRoleAttributeValue, Literal)
final case object CostTotal extends MappingCategory[Double, XEvent](OpenXESHelper.costTotalAttributeValue, Continuous)
final case object CostCurrency extends MappingCategory[String, XEvent](OpenXESHelper.costCurrencyAttributeValue, Literal)
final case object TraceName extends MappingCategory[String, XTrace](OpenXESHelper.traceConceptNameAttributeValue, Literal, Trace)

sealed trait SonificationType {
  def typ: MappingCategoryType
}
final case object InstrumentType extends SonificationType { val typ = Literal }
final case object MelodyType extends SonificationType { val typ = Literal }
final case object DrumType extends SonificationType { val typ = Literal }
final case object RhythmType extends SonificationType { val typ = Literal }
final case object VolumeType extends SonificationType { val typ = Continuous }
final case object PanningType extends SonificationType { val typ = Continuous }

class Enabler[T](var value: T, var enabled: Boolean = true) extends Serializable {
  override def toString = s"$value/$enabled"
}

sealed trait LiteralMapping[T] {
  val values: MMap[String, Enabler[T]]
  def isEnabled(key: String): Boolean = values.get(key).exists(_.enabled)
}

sealed abstract class SonificationMapping(val mandatory: Boolean, val typ: SonificationType) extends SimpleName with Serializable {
  type In
  type Out
  def values: scala.collection.Map[In, Out]
  override def toString = values.map { case (k, v) => s"$k -> $v" }.mkString(s"${this.getClass.getSimpleName} ", ", ", "")
}

final case class InstrumentMapping(values: MMap[String, Enabler[String]]) extends SonificationMapping(true, InstrumentType) with LiteralMapping[String] {
  type In = String
  type Out = Enabler[String]
}

final case class MelodyMapping(values: MMap[String, Enabler[String]]) extends SonificationMapping(true, MelodyType) with LiteralMapping[String] {
  type In = String
  type Out = Enabler[String]
}

final case class DrumMapping(values: MMap[String, Enabler[String]]) extends SonificationMapping(true, DrumType) with LiteralMapping[String] {
  type In = String
  type Out = Enabler[String]
}

final case class RhythmMapping(values: MMap[String, Enabler[String]]) extends SonificationMapping(true, RhythmType) with LiteralMapping[String] {
  type In = String
  type Out = Enabler[String]
}

object InstrumentMapping {
  def apply(seq: Seq[(String, String)]): InstrumentMapping = InstrumentMapping(MMap(seq.map(e => e._1 -> new Enabler(e._2)): _*))
  def apply(set: Set[(String, String)]): InstrumentMapping = InstrumentMapping(set.toSeq)
}
object MelodyMapping {
  def apply(seq: Seq[(String, String)]): MelodyMapping = MelodyMapping(MMap(seq.map(e => e._1 -> new Enabler(e._2)): _*))
  def apply(set: Set[(String, String)]): MelodyMapping = MelodyMapping(set.toSeq)
}
object DrumMapping {
  def apply(seq: Seq[(String, String)]): DrumMapping = DrumMapping(MMap(seq.map(e => e._1 -> new Enabler(e._2)): _*))
  def apply(set: Set[(String, String)]): DrumMapping = DrumMapping(set.toSeq)
}
object RhythmMapping {
  def apply(seq: Seq[(String, String)]): RhythmMapping = RhythmMapping(MMap(seq.map(e => e._1 -> new Enabler(e._2)): _*))
  def apply(set: Set[(String, String)]): RhythmMapping = RhythmMapping(set.toSeq)
}

final case class VolumeMapping(values: TreeMap[Double, Int]) extends SonificationMapping(false, VolumeType) {
  type In = Double
  type Out = Int
}

final case class PanningMapping(values: TreeMap[Double, Int]) extends SonificationMapping(false, PanningType) {
  type In = Double
  type Out = Int
}

object VolumeMapping {
  def apply(in: Seq[Double]): VolumeMapping = VolumeMapping(TreeMap(in zip coarseVolumeMappingFunction(in): _*))
}
object PanningMapping {
  def apply(in: Seq[Double]): PanningMapping = PanningMapping(TreeMap(in zip coarsePanningMappingFunction(in): _*))
}

sealed trait VisualizationType {
  def typ: MappingCategoryType
}
final case object ColorType extends VisualizationType { val typ = Literal }
final case object ShapeType extends VisualizationType { val typ = Literal }

abstract class VisualizationMapping(val typ: VisualizationType) extends SimpleName with Serializable {
  type Out
  def values: MMap[String, Enabler[Out]]
}

final case class ColorMapping(values: MMap[String, Enabler[Color]] = MMap.empty) extends VisualizationMapping(ColorType) with LiteralMapping[Color] {
  type Out = Color
}

final case class ShapeMapping(values: MMap[String, Enabler[UIShape]] = MMap.empty) extends VisualizationMapping(ShapeType) with LiteralMapping[UIShape] {
  type Out = UIShape
}

object ColorMapping {
  def apply(seq: Seq[(String, Color)]): ColorMapping = ColorMapping(MMap(seq.map(e => e._1 -> new Enabler(e._2)): _*))
  def apply(set: Set[(String, Color)]): ColorMapping = ColorMapping(set.toSeq)
}
object ShapeMapping {
  def apply(seq: Seq[(String, UIShape)]): ShapeMapping = ShapeMapping(MMap(seq.map(e => e._1 -> new Enabler(e._2)): _*))
  def apply(set: Set[(String, UIShape)]): ShapeMapping = ShapeMapping(set.toSeq)
}

case class Staccatos(melodies: Seq[String], rhythms: Seq[String]) {
  lazy val isEmpty = melodies.isEmpty && rhythms.isEmpty
}
object Staccatos {
  val empty = Staccatos(Seq.empty, Seq.empty)
}

case class CategoryMapping(visualization: Option[VisualizationMapping], sonification: Option[SonificationMapping]) {
  def allStaccatos: Staccatos = sonification match {
    case Some(m: MelodyMapping) => Staccatos(m.values.values.filter(_.enabled).map(_.value).toSeq, Seq.empty)
    case Some(r: RhythmMapping) => Staccatos(Seq.empty, r.values.values.filter(_.enabled).map(_.value).toSeq)
    case _ => Staccatos(Seq.empty, Seq.empty)
  }
}