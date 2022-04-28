package net.horizonsend.ion.core.extensions

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

@Deprecated(
	"Do not use MiniMessage Directly",
	ReplaceWith("sendFeedbackMessage(?, message, ?)", "net.horizonsend.ion.core.feedback.sendFeedbackMessage")
)

fun Audience.sendMiniMessage(message: String) = sendMessage(miniMessage().deserialize(message))