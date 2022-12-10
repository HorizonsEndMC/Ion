package net.horizonsend.ion.server.extensions

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor.color

/** Does not support MiniMessage! */
fun Audience.sendUserError(message: String) {
	sendColouredMessage(message, 0xff7f3f)
	Ion.slF4JLogger.warn(message)
}

/** Does not support MiniMessage! */
fun Audience.sendServerError(message: String) {
	sendColouredMessage(message, 0xff3f3f)
	Ion.slF4JLogger.error(message)
}

/** Does not support MiniMessage! */
fun Audience.sendInformation(message: String) {
	sendColouredMessage(message, 0x3f3fff)
	Ion.slF4JLogger.info(message)
}

private fun Audience.sendColouredMessage(message: String, color: Int) = sendMessage(text(message, color(color)))