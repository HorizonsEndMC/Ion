package net.horizonsend.ion.server.extensions

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.kyori.adventure.text.Component

/**
 * Represents a type of feedback that can be sent to a player
 * @property colour The colour used when displaying feedback
 */
@Deprecated("Use newer audience extension functions")
enum class FeedbackType(
	@Deprecated("Use newer audience extension functions")
	val colour: Int,

	@Deprecated("Use newer audience extension functions")
	val loggingFunction: (Component) -> Unit = Ion.componentLogger::info
) {
	/** Something failed as a result of an issue on the server, this is generally rare */
	@Deprecated("Use newer audience extension functions")
	SERVER_ERROR(0xff8888, Ion.componentLogger::error),

	/** Something failed as a result of the player, such as an invalid command parameter, or lacking permission */
	@Deprecated("Use newer audience extension functions")
	USER_ERROR(0xff8844, Ion.componentLogger::warn),

	/** Information the player should be aware of */
	@Deprecated("Use newer audience extension functions")
	INFORMATION(0x8888ff),

	/** Used when confirming a command succeeded */
	@Deprecated("Use newer audience extension functions")
	SUCCESS(0x88ff88),

	/** Used when alerting the server of an event */
	@Deprecated("Use newer audience extension functions")
	ALERT(0xff3030)
}
