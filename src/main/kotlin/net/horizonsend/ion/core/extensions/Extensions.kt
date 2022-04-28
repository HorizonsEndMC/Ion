package net.horizonsend.ion.core.extensions

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

fun Audience.sendMiniAction(message: String) = sendActionBar(miniMessage().deserialize(message))

fun Audience.sendMiniMessage(message: String) = sendMessage(miniMessage().deserialize(message))