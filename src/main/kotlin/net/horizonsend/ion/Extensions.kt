package net.horizonsend.ion

import java.util.EnumSet
import java.util.EnumSet.noneOf
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

internal val String.asMiniMessage get() = miniMessage().deserialize(this)

internal fun Audience.sendMiniMessage(message: String) = sendMessage(message.asMiniMessage)

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	noneOf(T::class.java).apply {
		addAll(elems)
	}