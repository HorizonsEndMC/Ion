@file:Suppress("unused") // API

package net.horizonsend.ion.utilities.feedback

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

/**
 * Represents a type of feedback that can be sent to a player
 * @property colour The colour used when displaying feedback
 */
enum class FeedbackType(val colour: String) {
	/**
	 * Something failed as a result of an issue on the server, this is generally rare
	 */
	SERVER_ERROR("#ff8888"),

	/**
	 * Something failed as a result of the player, such as an invalid command parameter, or lacking permission
	 */
	USER_ERROR("#ff8844"),

	/**
	 * Information the player should be aware of
	 */
	INFORMATION("#8888ff"),

	/**
	 * Used when confirming a command succeeded
	 */
	SUCCESS("#88ff88")
}

/**
 * @param type The type of feedback
 * @param message The feedback message, use "{index}" to insert variables into the message
 * @param parameters Variables to insert into the message
 * @see FeedbackType
 */
internal fun Audience.sendFeedbackAction(type: FeedbackType, message: String, vararg parameters: Any): Unit =
	sendActionBar(parseFeedback(type, message, parameters.toList()))

/**
 * @param type The type of feedback
 * @param message The feedback message, use "{index}" to insert variables into the message
 * @param parameters Variables to insert into the message
 * @see FeedbackType
 */
internal fun Audience.sendFeedbackMessage(type: FeedbackType, message: String, vararg parameters: Any): Unit =
	sendMessage(parseFeedback(type, message, parameters.toList()))

/**
 * @param type The type of feedback
 * @param message The feedback message, use "{index}" to insert variables into the message
 * @param parameters Variables to insert into the message
 * @see FeedbackType
 */
internal fun Audience.sendFeedbackActionMessage(type: FeedbackType, message: String, vararg parameters: Any) {
	parseFeedback(type, message, parameters.toList()).also { feedback ->
		sendActionBar(feedback)
		sendMessage(feedback)
	}
}

private fun parseFeedback(type: FeedbackType, message: String, parameters: Collection<Any>): Component {
	var newMessage = "<${type.colour}>$message"

	parameters.forEachIndexed { index, parameter ->
		newMessage = newMessage.replace(
			"{$index}", "<white>${
				when (parameter) {
					is Number -> "$parameter"
					else -> "\"$parameter\""
				}
			}</white>"
		)
	}

	return miniMessage().deserialize(newMessage)
}