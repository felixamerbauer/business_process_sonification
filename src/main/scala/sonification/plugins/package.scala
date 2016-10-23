//package sonification
//
//import org.processmining.framework.plugin.PluginContext
//import org.processmining.framework.plugin.events.Logger.MessageLevel
//
//package object plugins {
//  // TODO logging using the ProM framework doesn't work yet
//  def debug(mes: String)(implicit context: PluginContext) = context.log(mes, MessageLevel.DEBUG)
//  def error(mes: String)(implicit context: PluginContext) = context.log(mes, MessageLevel.ERROR)
//  def test(mes: String)(implicit context: PluginContext) = context.log(mes, MessageLevel.TEST)
//  def normal(mes: String)(implicit context: PluginContext) = context.log(mes, MessageLevel.NORMAL)
//
//}