package sonification.controller

object Update extends Enumeration {
  type Update = Value
  val 
  // log, trace, event changes
  ULog, UTraces, UFilter, 
  // mapping changes
  USonificationMapping, UVisualizationMapping, UGlobalMapping,
  // playback settings
  UVolume, USpeed, UDuration, UMetronom, UJumpTo,
  // UI settings
  UZoom = Value
}


