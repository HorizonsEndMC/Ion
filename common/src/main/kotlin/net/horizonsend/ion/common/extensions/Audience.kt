@file:Suppress("Unused")
package net.horizonsend.ion.common.extensions

import net.horizonsend.ion.common.utils.text.colors.Colors
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

var prefixProvider: (Audience) -> String = { "" }

private val logger = ComponentLogger.logger("Ion")

private fun construct(message: String, color: Int): Component =
	empty().color(color(color)).append(miniMessage().deserialize(message))

private fun Audience.loggedMessage(message: String, color: Int, loggingFunction: (Component) -> Unit = logger::info) {
	val component = construct(message, color)
	sendMessage(component)
	loggingFunction(empty().append(text(prefixProvider(this))).append(component))
}

private fun Audience.action(message: String, color: Int) = sendActionBar(construct(message, color))

private fun Audience.actionMessage(message: String, color: Int, loggingFunction: (Component) -> Unit = logger::info) {
	loggedMessage(message, color, loggingFunction)
	action(message, color)
}

// Messages //
fun Audience.serverError(message: String) = loggedMessage(message, Colors.SERVER_ERROR, logger::error)
fun Audience.userError(message: String) = loggedMessage(message, Colors.USER_ERROR, logger::warn)
fun Audience.alert(message: String) = loggedMessage(message, Colors.ALERT)
fun Audience.information(message: String) = loggedMessage(message, Colors.INFORMATION)
fun Audience.special(message: String) = loggedMessage(message, Colors.SPECIAL)
fun Audience.success(message: String) = loggedMessage(message, Colors.SUCCESS)
fun Audience.hint(message: String) = loggedMessage(message, Colors.HINT) {}

// Actions //
fun Audience.serverErrorAction(message: String) = action(message, Colors.SERVER_ERROR)
fun Audience.userErrorAction(message: String) = action(message, Colors.USER_ERROR)
fun Audience.alertAction(message: String) = action(message, Colors.ALERT)
fun Audience.informationAction(message: String) = action(message, Colors.INFORMATION)
fun Audience.specialAction(message: String) = action(message, Colors.SPECIAL)
fun Audience.successAction(message: String) = action(message, Colors.SUCCESS)
fun Audience.hintAction(message: String) = action(message, Colors.HINT)

// Action Messages //
fun Audience.serverErrorActionMessage(message: String) = actionMessage(message, Colors.SERVER_ERROR, logger::error)
fun Audience.userErrorActionMessage(message: String) = actionMessage(message, Colors.USER_ERROR, logger::warn)
fun Audience.alertActionMessage(message: String) = actionMessage(message, Colors.ALERT)
fun Audience.informationActionMessage(message: String) = actionMessage(message, Colors.INFORMATION)
fun Audience.specialActionMessage(message: String) = actionMessage(message, Colors.SPECIAL)
fun Audience.successActionMessage(message: String) = actionMessage(message, Colors.SUCCESS)
fun Audience.hintActionMessage(message: String) = actionMessage(message, Colors.HINT) {}
