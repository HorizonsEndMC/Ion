@file:Suppress("Unused")
package net.horizonsend.ion.common.extensions

import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.template
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.title.Title

var prefixProvider: (Audience) -> String = { "" }

private val logger = ComponentLogger.logger("Ion")

private fun construct(message: String, color: Int, parameters: Array<out Any>): Component =
	empty().color(color(color)).append(template(miniMessage().deserialize(message), *parameters))

private fun Audience.loggedMessage(message: String, color: Int, parameters: Array<out Any>, loggingFunction: (Component) -> Unit = logger::info) {
	val component = construct(message, color, parameters)
	sendMessage(component)
	loggingFunction(empty().append(text(prefixProvider(this))).append(component))
}

private fun Audience.action(message: String, color: Int, parameters: Array<out Any>) = sendActionBar(construct(message, color, parameters))

private fun Audience.actionMessage(message: String, color: Int, parameters: Array<out Any>, loggingFunction: (Component) -> Unit = logger::info) {
	loggedMessage(message, color, parameters, loggingFunction)
	action(message, color, parameters)
}

private fun Audience.title(message: String, color: Int, parameters: Array<out Any>) = showTitle(Title.title(construct(message,color, parameters), construct(" ",color, parameters)))
private fun Audience.subtitle(message: String, color: Int, parameters: Array<out Any>) = showTitle(Title.title(construct(" ",color, parameters), construct(message,color, parameters)))

// Messages //
fun Audience.serverError(message: String, vararg parameters: Any) = loggedMessage(message, Colors.SERVER_ERROR, parameters, logger::error)
fun Audience.userError(message: String, vararg parameters: Any) = loggedMessage(message, Colors.USER_ERROR, parameters, logger::warn)
fun Audience.alert(message: String, vararg parameters: Any) = loggedMessage(message, Colors.ALERT, parameters)
fun Audience.information(message: String, vararg parameters: Any) = loggedMessage(message, Colors.INFORMATION, parameters)
fun Audience.special(message: String, vararg parameters: Any) = loggedMessage(message, Colors.SPECIAL, parameters)
fun Audience.success(message: String, vararg parameters: Any) = loggedMessage(message, Colors.SUCCESS, parameters)
fun Audience.hint(message: String, vararg parameters: Any) = loggedMessage(message, Colors.HINT, parameters) {}

// Actions //
fun Audience.serverErrorAction(message: String, vararg parameters: Any) = action(message, Colors.SERVER_ERROR, parameters)
fun Audience.userErrorAction(message: String, vararg parameters: Any) = action(message, Colors.USER_ERROR, parameters)
fun Audience.alertAction(message: String, vararg parameters: Any) = action(message, Colors.ALERT, parameters)
fun Audience.informationAction(message: String, vararg parameters: Any) = action(message, Colors.INFORMATION, parameters)
fun Audience.specialAction(message: String, vararg parameters: Any) = action(message, Colors.SPECIAL, parameters)
fun Audience.successAction(message: String, vararg parameters: Any) = action(message, Colors.SUCCESS, parameters)
fun Audience.hintAction(message: String, vararg parameters: Any) = action(message, Colors.HINT, parameters)

// Action Messages //
fun Audience.serverErrorActionMessage(message: String, vararg parameters: Any) = actionMessage(message, Colors.SERVER_ERROR, parameters, logger::error)
fun Audience.userErrorActionMessage(message: String, vararg parameters: Any) = actionMessage(message, Colors.USER_ERROR, parameters, logger::warn)
fun Audience.alertActionMessage(message: String, vararg parameters: Any) = actionMessage(message, Colors.ALERT, parameters)
fun Audience.informationActionMessage(message: String, vararg parameters: Any) = actionMessage(message, Colors.INFORMATION, parameters)
fun Audience.specialActionMessage(message: String, vararg parameters: Any) = actionMessage(message, Colors.SPECIAL, parameters)
fun Audience.successActionMessage(message: String, vararg parameters: Any) = actionMessage(message, Colors.SUCCESS, parameters)
fun Audience.hintActionMessage(message: String, vararg parameters: Any) = actionMessage(message, Colors.HINT, parameters) {}

// Titles //
fun Audience.serverErrorTitle(message: String, vararg parameters: Any) = title(message, Colors.SERVER_ERROR, parameters)
fun Audience.userErrorTitle(message: String, vararg parameters: Any) = title(message, Colors.USER_ERROR, parameters)
fun Audience.alertTitle(message: String, vararg parameters: Any) = title(message, Colors.ALERT, parameters)
fun Audience.informationTitle(message: String, vararg parameters: Any) = title(message, Colors.INFORMATION, parameters)
fun Audience.specialTitle(message: String, vararg parameters: Any) = title(message, Colors.SPECIAL, parameters)
fun Audience.successTitle(message: String, vararg parameters: Any) = title(message, Colors.SUCCESS, parameters)

// Subtitles //
fun Audience.serverErrorSubtitle(message: String, vararg parameters: Any) = subtitle(message, Colors.SERVER_ERROR, parameters)
fun Audience.userErrorSubtitle(message: String, vararg parameters: Any) = subtitle(message, Colors.USER_ERROR, parameters)
fun Audience.alertSubtitle(message: String, vararg parameters: Any) = subtitle(message, Colors.ALERT, parameters)
fun Audience.informationSubtitle(message: String, vararg parameters: Any) = subtitle(message, Colors.INFORMATION, parameters)
fun Audience.specialSubtitle(message: String, vararg parameters: Any) = subtitle(message, Colors.SPECIAL, parameters)
fun Audience.successSubtitle(message: String, vararg parameters: Any) = subtitle(message, Colors.SUCCESS, parameters)
