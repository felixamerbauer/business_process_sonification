package sonification.music

import org.scalatest.{ FunSuite, Matchers }

import sonification.controller.{ CostTotal, PanningType, SettingsUtil }

class StaccatoUpdateVerifier extends FunSuite with Matchers with StaccatoHelper {

  test("generated.xes - CostToal:Some(Volume) -> None") {
    val (staccato, xes, settings) = generateStaccato(s"$dir/generated.xes")
    // update
    val updatedMapping = SettingsUtil.updateSonification(CostTotal, None, xes, settings.mapping)
    val updatedSettings = settings.copy(mapping = updatedMapping)
    val updatedStaccato = StaccatoGenerator.makeStaccato(xes, updatedSettings).trim
    // check the updated version
    val proof = readStaccato(s"$dir/generated_volume_none.staccato")
    check(updatedStaccato, proof)
  }

  test("generated.xes - CostToal:Some(Volume) -> Some(panning)") {
    val (staccato, xes, settings) = generateStaccato(s"$dir/generated.xes")
    // update
    val updatedMapping = SettingsUtil.updateSonification(CostTotal, Some(PanningType), xes, settings.mapping)
    val updatedSettings = settings.copy(mapping = updatedMapping)
    val updatedStaccato = StaccatoGenerator.makeStaccato(xes, updatedSettings).trim
    // check the updated version
    val proof = readStaccato(s"$dir/generated_volume_panning.staccato")
    check(updatedStaccato, proof)
  }
}