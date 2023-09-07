package net.horizonsend.ion.server.miscellaneous.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun Component.plainText(): String = PlainTextComponentSerializer.plainText().serialize(this)

fun String.miniMessage() = MiniMessage.miniMessage().deserialize(this)
