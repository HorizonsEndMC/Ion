@file:Suppress("Unused")
package net.horizonsend.ion.common.extensions

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

object Colors {
	const val SERVER_ERROR = 0xff3f3f
	const val USER_ERROR = 0xff7f3f
	const val ALERT = SERVER_ERROR
	const val INFORMATION = 0x7f7fff
	const val SUCCESS = 0x3fff3f
	const val HINT = 0x7f7f7f
}

private fun Audience.action(message: String, color: Int) = sendActionBar(construct(message, color))

// Messages //
fun Audience.serverError(message: String) = loggedMessage(message, Colors.SERVER_ERROR, logger::error)
fun Audience.userError(message: String) = loggedMessage(message, Colors.USER_ERROR, logger::warn)
fun Audience.alert(message: String) = loggedMessage(message, Colors.ALERT)
fun Audience.information(message: String) = loggedMessage(message, Colors.INFORMATION)
fun Audience.success(message: String) = loggedMessage(message, Colors.SUCCESS)
fun Audience.hint(message: String) = loggedMessage(message, Colors.HINT) {}

// Actions //
fun Audience.serverErrorAction(message: String) = action(message, Colors.SERVER_ERROR)
fun Audience.userErrorAction(message: String) = action(message, Colors.USER_ERROR)
fun Audience.alertAction(message: String) = action(message, Colors.ALERT)
fun Audience.informationAction(message: String) = action(message, Colors.INFORMATION)
fun Audience.successAction(message: String) = action(message, Colors.SUCCESS)
fun Audience.hintAction(message: String) = action(message, Colors.HINT)

// Action Messages //
fun Audience.serverErrorActionMessage(message: String) {
	loggedMessage(message, Colors.SERVER_ERROR, logger::error)
	action(message, Colors.SERVER_ERROR)
}

fun Audience.userErrorActionMessage(message: String) {
	loggedMessage(message, Colors.USER_ERROR, logger::warn)
	action(message, Colors.USER_ERROR)
}

fun Audience.alertActionMessage(message: String) {
	loggedMessage(message, Colors.ALERT)
	action(message, Colors.ALERT)
}

fun Audience.informationActionMessage(message: String) {
	loggedMessage(message, Colors.INFORMATION)
	action(message, Colors.INFORMATION)
}

fun Audience.successActionMessage(message: String) {
	loggedMessage(message, Colors.SUCCESS)
	action(message, Colors.SUCCESS)
}

fun Audience.hintActionMessage(message: String) {
	loggedMessage(message, Colors.HINT) {}
	action(message, Colors.HINT)
}
