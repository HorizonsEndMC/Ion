package net.horizonsend.ion.server.extensions

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.format.TextColor.color
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

fun Audience.sendServerError(message: String) {
	sendColouredMiniMessage(message, 0xff3f3f)
	Ion.slF4JLogger.error(message)
}

fun Audience.sendUserError(message: String) = sendColouredMiniMessage(message, 0xff7f3f)

private fun Audience.sendColouredMiniMessage(message: String, color: Int) =
	sendMessage(empty().append(miniMessage().deserialize(message)).color(color(color)))