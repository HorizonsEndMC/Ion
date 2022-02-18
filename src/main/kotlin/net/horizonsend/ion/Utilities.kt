package net.horizonsend.ion

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

val String.asMiniMessage: Component
	get() = miniMessage().deserialize(this)

fun Audience.sendMiniMessage(message: String) = sendMessage(message.asMiniMessage)