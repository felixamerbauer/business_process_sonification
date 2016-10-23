package sonification

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import com.typesafe.scalalogging.StrictLogging
import com.codahale.metrics.Slf4jReporter
import org.slf4j.LoggerFactory

/**
 *  Provides metric methods using <a href="https://dropwizard.github.io/metrics">Dropwizard Metrics</a>
 */
object Metrics extends StrictLogging {

  // a registry for all metrics 
  var registry: MetricRegistry = _

  private def t(id: String): Timer = registry.timer(id)

  // Concrete timers
  var TParseXES: Timer = _
  // StaccatoGenerator
  var TSG_musicEvents: Timer = _
  var TSG_staccato: Timer = _
  var TSG_makeStaccato: Timer = _
  // MusicCommons
  var TMC_makeStaccato: Timer = _
  var TMC_makeMidi: Timer = _
  var TMC_staccatoValid: Timer = _
  var TMC_duration: Timer = _
  var TMC_sequence: Timer = _
  // SettingsUil
  var TSU_defaultSettings: Timer = _
  var TSU_updateSonification: Timer = _
  var TSU_updateVisualization: Timer = _
  var TSU_sonificationForCategory: Timer = _
  var TSU_visualizationForCategory: Timer = _
  var TSU_literalMapping1: Timer = _
  var TSU_literalMapping2: Timer = _
  // Visualization
  var TV_pageBorders: Timer = _
  var TV_calculateBufferedImage: Timer = _
  var TV_draw: Timer = _
  var TV_paintComponent: Timer = _
  var TV_findItem: Timer = _
  // VisualizationCalculator
  var TVC_calculateVisualization: Timer = _
  // StaccatoValidation
  var SVC_validate: Timer = _
  // OpenXESHelper
  var OX_serialize: Timer = _
  // Midi2WavRender
  var M2W_render: Timer = _
  var M2W_findAudioSynthesizer: Timer = _
  var M2W_send: Timer = _

  reset()

  def reset() {
    logger.info("reset")
    registry = new MetricRegistry()
    // Concrete timers
    TParseXES = t("parseXES")
    // StaccatoGenerator
    TSG_musicEvents = t("SG_musicEvents")
    TSG_staccato = t("SG_musicEvents")
    TSG_makeStaccato = t("SG_makeStaccato")
    // MusicCommons
    TMC_makeStaccato = t("MC-makeStaccato")
    TMC_makeMidi = t("MC-makeMidi")
    TMC_staccatoValid = t("MC-staccatoValid")
    TMC_duration = t("MC-duration")
    TMC_sequence = t("MC-sequence")
    // SettingsUil
    TSU_defaultSettings = t("SU-defaultSettings")
    TSU_updateSonification = t("SU-updateSonification")
    TSU_updateVisualization = t("SU-updateVisualization")
    TSU_sonificationForCategory = t("SU-sonificationForCategory")
    TSU_visualizationForCategory = t("SU-visualizationForCategory")
    TSU_literalMapping1 = t("SU-literalMapping1")
    TSU_literalMapping2 = t("SU-literalMapping2")
    // Visualization
    TV_pageBorders = t("V-pageBorders")
    TV_calculateBufferedImage = t("V-calculateBufferedImage")
    TV_draw = t("V-draw")
    TV_paintComponent = t("V-paintComponent")
    TV_findItem = t("V-findItem")
    // VisualizationCalculator
    TVC_calculateVisualization = t("VC-calculateVisualization")
    // StaccatoValidation
    SVC_validate = t("SV_validateStaccato")
    // OpenXESHelper
    OX_serialize = t("OX_serialize")
    // Midi2WavRender
    M2W_render = t("M2W-render")
    M2W_findAudioSynthesizer = t("M2W-findAudioSynthesizer")
    M2W_send = t("M2W-send")
  }

  /**
   * Execute some code and measure the time it requires to the supplied timer
   * @param timer the timer to report to
   * @param block code to executed (call by name)
   * @return the return value of the executed code
   */
  def t[R](timer: Timer)(block: => R): R = {
    val context = timer.time()
    try {
      block
    } finally {
      context.stop()
    }
  }
}