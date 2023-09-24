package net.horizonsend.ion.common.utils.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun String.miniMessage() = MiniMessage.miniMessage().deserialize(this)
fun Component.plainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
