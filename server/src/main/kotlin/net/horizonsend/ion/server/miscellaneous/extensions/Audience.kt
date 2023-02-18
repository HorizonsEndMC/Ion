@file:Suppress("Unused")
package net.horizonsend.ion.server.miscellaneous.extensions

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.entity.Player

@Suppress("NOTHING_TO_INLINE")
private inline fun construct(message: String, color: Int): Component =
	empty().color(color(color)).append(miniMessage().deserialize(message))

private inline val Audience.prefix get() = when (this) {
	is Player -> "to $name: "
	else -> ""
}

private inline fun Audience.loggedMessage(
	message: String,
	color: Int,
	crossinline loggingFunction: (Component) -> Unit = Ion.componentLogger::info
) {
	val component = construct(message, color)
	sendMessage(component)
	loggingFunction(empty().append(text(prefix)).append(component))
}

object HEColors {
	const val SERVER_ERROR = 0xff3f3f
	const val USER_ERROR = 0xff7f3f
	const val ALERT = SERVER_ERROR
	const val INFORMATION = 0x7f7fff
	const val SUCCESS = 0x3fff3f
	const val HINT = 0x7f7f7f
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Audience.action(message: String, color: Int) = sendActionBar(construct(message, color))

// Messages //
fun Audience.serverError(message: String) = loggedMessage(message, HEColors.SERVER_ERROR, Ion.componentLogger::error)
fun Audience.userError(message: String) = loggedMessage(message, HEColors.USER_ERROR, Ion.componentLogger::warn)
fun Audience.alert(message: String) = loggedMessage(message, HEColors.ALERT)
fun Audience.information(message: String) = loggedMessage(message, HEColors.INFORMATION)
fun Audience.success(message: String) = loggedMessage(message, HEColors.SUCCESS)
fun Audience.hint(message: String) = loggedMessage(message, HEColors.HINT) {}

// Actions //
fun Audience.serverErrorAction(message: String) = action(message, HEColors.SERVER_ERROR)
fun Audience.userErrorAction(message: String) = action(message, HEColors.USER_ERROR)
fun Audience.alertAction(message: String) = action(message, HEColors.ALERT)
fun Audience.informationAction(message: String) = action(message, HEColors.INFORMATION)
fun Audience.successAction(message: String) = action(message, HEColors.SUCCESS)
fun Audience.hintAction(message: String) = action(message, HEColors.HINT)

// Action Messages //
fun Audience.serverErrorActionMessage(message: String) {
	loggedMessage(message, HEColors.SERVER_ERROR, Ion.componentLogger::error)
	action(message, HEColors.SERVER_ERROR)
}

fun Audience.userErrorActionMessage(message: String) {
	loggedMessage(message, HEColors.USER_ERROR, Ion.componentLogger::warn)
	action(message, HEColors.USER_ERROR)
}

fun Audience.alertActionMessage(message: String) {
	loggedMessage(message, HEColors.ALERT)
	action(message, HEColors.ALERT)
}

fun Audience.informationActionMessage(message: String) {
	loggedMessage(message, HEColors.INFORMATION)
	action(message, HEColors.INFORMATION)
}

fun Audience.successActionMessage(message: String) {
	loggedMessage(message, HEColors.SUCCESS)
	action(message, HEColors.SUCCESS)
}

fun Audience.hintActionMessage(message: String) {
	loggedMessage(message, HEColors.HINT) {}
	action(message, HEColors.HINT)
}
