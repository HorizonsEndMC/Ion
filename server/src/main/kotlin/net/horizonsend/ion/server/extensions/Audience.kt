package net.horizonsend.ion.server.extensions

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

@Suppress("NOTHING_TO_INLINE")
private inline fun Audience.action(message: String, color: Int) = sendActionBar(construct(message, color))

// Messages //
fun Audience.serverError(message: String) = loggedMessage(message, 0xff3f3f, Ion.componentLogger::error)
fun Audience.userError(message: String) = loggedMessage(message, 0xff7f3f, Ion.componentLogger::warn)
fun Audience.alert(message: String) = loggedMessage(message, 0xff3f3f)
fun Audience.information(message: String) = loggedMessage(message, 0x7f7fff)
fun Audience.success(message: String) = loggedMessage(message, 0x3fff3f)
fun Audience.hint(message: String) = loggedMessage(message, 0x7f7f7f) {}

// Actions //
fun Audience.serverErrorAction(message: String) = action(message, 0xff3f3f)
fun Audience.userErrorAction(message: String) = action(message, 0xff7f3f)
fun Audience.alertAction(message: String) = action(message, 0xff3f3f)
fun Audience.informationAction(message: String) = action(message, 0x7f7fff)
fun Audience.successAction(message: String) = action(message, 0x3fff3f)
fun Audience.hintAction(message: String) = action(message, 0x7f7f7f)

// Action Messages //
fun Audience.serverErrorActionMessage(message: String) {
	loggedMessage(message, 0xff3f3f, Ion.componentLogger::error)
	action(message, 0xff3f3f)
}

fun Audience.userErrorActionMessage(message: String) {
	loggedMessage(message, 0xff7f3f, Ion.componentLogger::warn)
	action(message, 0xff7f3f)
}

fun Audience.alertActionMessage(message: String) {
	loggedMessage(message, 0xff3f3f)
	action(message, 0xff3f3f)
}

fun Audience.informationActionMessage(message: String) {
	loggedMessage(message, 0x7f7fff)
	action(message, 0x7f7fff)
}

fun Audience.successActionMessage(message: String) {
	loggedMessage(message, 0x3fff3f)
	action(message, 0x3fff3f)
}

fun Audience.hintActionMessage(message: String) {
	loggedMessage(message, 0x7f7f7f) {}
	action(message, 0x7f7f7f)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun parseFeedback(message: String, vararg parameters: Any): String {
	var newMessage = message

	parameters.forEachIndexed { index, parameter ->
		newMessage = newMessage.replace("{$index}", "$parameter")
	}

	return message
}

@Deprecated("Use newer audience extension functions")
internal fun Audience.sendFeedbackAction(type: FeedbackType, message: String, vararg parameters: Any): Unit =
	sendActionBar(construct(parseFeedback(message, parameters), type.colour))

@Deprecated("Use newer audience extension functions")
internal fun Audience.sendFeedbackMessage(type: FeedbackType, message: String, vararg parameters: Any): Unit =
	loggedMessage(parseFeedback(message, parameters), type.colour, type.loggingFunction)

@Deprecated("Use newer audience extension functions")
internal fun Audience.sendFeedbackActionMessage(type: FeedbackType, message: String, vararg parameters: Any) {
	loggedMessage(parseFeedback(message, parameters), type.colour, type.loggingFunction)
	action(message, type.colour)
}
