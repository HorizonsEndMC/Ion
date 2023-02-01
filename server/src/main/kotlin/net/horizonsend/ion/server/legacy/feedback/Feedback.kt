package net.horizonsend.ion.server.legacy.feedback

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.kyori.adventure.title.Title
import java.time.Duration

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

/**
 * @param type The type of feedback
 * @param title The feedback title, use "{index}" to insert variables into the message
 * @param subtitle The feedback subtitle, use "{index}" to insert variables into the message
 * @param fadeIn Time it takes for the message to fade in (in milliseconds)
 * @param stay Time the message stays on the screen (in milliseconds)
 * @param fadeOut Time it takes for the message to fade out (in milliseconds)
 * @param parameters Variables to insert into the message
 * @see FeedbackType
 */
internal fun Audience.sendFeedbackTitle(
	type: FeedbackType,
	title: String = "",
	subtitle: String = "",
	fadeIn: Long = 0,
	stay: Long = 0,
	fadeOut: Long = 0,
	vararg parameters: Any
) {
	val times = Title.Times.times(
		Duration.ofMillis(fadeIn),
		Duration.ofMillis(stay),
		Duration.ofMillis(fadeOut)
	)

	showTitle(
		Title.title(
			parseFeedback(type, title, parameters.toList()),
			parseFeedback(type, subtitle, parameters.toList()),
			times
		)
	)
}

private fun parseFeedback(type: FeedbackType, message: String, parameters: Collection<Any>): Component {
	var newMessage = "<${type.colour}>$message"

	parameters.forEachIndexed { index, parameter ->
		newMessage = newMessage.replace(
			"{$index}",
			"<reset>${
			when (parameter) {
				is Number -> "$parameter"
				else -> "\"$parameter<reset>\""
			}
			}<${type.colour}>"
		)
	}

	return miniMessage().deserialize(newMessage)
}
