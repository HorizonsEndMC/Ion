package net.horizonsend.ion.extensions

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

val String.asMiniMessage get() = miniMessage().deserialize(this)

fun Audience.sendMiniMessage(message: String) = sendMessage(message.asMiniMessage)