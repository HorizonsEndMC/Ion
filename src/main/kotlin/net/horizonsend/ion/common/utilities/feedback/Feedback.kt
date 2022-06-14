package net.horizonsend.ion.common.utilities.feedback

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.entity.Player

/*
 Paper's sendRichMessage is not used as parseFeedback handles MiniMessage deserialization because sendRichMessage does
 not exist for things like actions and titles.
*/

/**
 * @param type The type of feedback
 * @param message The feedback message, use "{index}" to insert variables into the message
 * @param parameters Variables to insert into the message
 * @see FeedbackType
 */
fun Audience.sendFeedbackAction(type: FeedbackType, message: String, vararg parameters: Any): Unit =
	sendActionBar(parseFeedback(type, message, parameters.toList()))

/**
 * @param type The type of feedback
 * @param message The feedback message, use "{index}" to insert variables into the message
 * @param parameters Variables to insert into the message
 * @see FeedbackType
 */
fun Audience.sendFeedbackMessage(type: FeedbackType, message: String, vararg parameters: Any): Unit =
	sendMessage(parseFeedback(type, message, parameters.toList()))

/**
 * @param type The type of feedback
 * @param message The feedback message, use "{index}" to insert variables into the message
 * @param parameters Variables to insert into the message
 * @see FeedbackType
 */
fun Audience.sendFeedbackActionMessage(type: FeedbackType, message: String, vararg parameters: Any) {
	parseFeedback(type, message, parameters.toList()).also { feedback ->
		sendActionBar(feedback)
		sendMessage(feedback)
	}
}

private fun parseFeedback(type: FeedbackType, message: String, parameters: Collection<Any>): Component {
	var newMessage = "<${type.colour}>$message"

	parameters.forEachIndexed { index, parameter ->
		newMessage = newMessage.replace(
			"{$index}",
			"<white>${
			when (parameter) {
				is Number -> parameter.toString()
				is Player -> parameter.name
				else -> "\"$parameter\""
			}
			}</white>"
		)
	}

	return miniMessage().deserialize(newMessage)
}