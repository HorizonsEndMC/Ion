package net.horizonsend.ion

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

internal val String.asMiniMessage get() = miniMessage().deserialize(this)
@Deprecated("Avoid using MiniMessage directly.", replaceWith = ReplaceWith("sendFeedbackMessage(?, message, ?)", "net.horizonsend.ion.feedback.sendFeedbackMessage"))
internal fun Audience.sendMiniMessage(message: String) = sendMessage(message.asMiniMessage)