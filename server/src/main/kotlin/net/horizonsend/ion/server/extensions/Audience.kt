package net.horizonsend.ion.server.extensions

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.entity.Player

/** Returns the prefix that should be used when displaying server messages sent to this audience in the console. */
private val Audience.prefix
	get() = when (this) {
		is Player -> "to $name: "
		else -> ""
	}

/** Sends a message to the audience with the given colour. */
private fun Audience.sendColouredMessage(message: String, color: Int) = sendMessage(text(message,color(color)))

/**
 * Used to provide potentially irrelevant information, sends a gray coloured message.
 *
 * Note: Does not support MiniMessage!
 */
fun Audience.sendHint(message: String) = sendColouredMessage(message, 0x7f7f7f)

/**
 * Used to provide relevant information, sends a blue coloured message, and logs it.
 *
 * Note: Does not support MiniMessage!
 */
fun Audience.sendInformation(message: String) {
	sendColouredMessage(message, 0x7f7fff)
	Ion.slF4JLogger.info("$prefix$message")
}

/**
 * Used when the player is at fault, sends a orange coloured message, and logs it as a warning.
 *
 * Note: Does not support MiniMessage!
 */
fun Audience.sendUserError(message: String) {
	sendColouredMessage(message, 0xff7f3f)
	Ion.slF4JLogger.warn("$prefix$message")
}

/**
 * Used when the server is at fault, sends a red coloured message, and logs it as a error.
 *
 * Note: Does not support MiniMessage!
 */
fun Audience.sendServerError(message: String) {
	sendColouredMessage(message, 0xff3f3f)
	Ion.slF4JLogger.error("$prefix$message")
}
