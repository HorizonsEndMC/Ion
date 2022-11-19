package net.horizonsend.ion.server.legacy.feedback

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
	SUCCESS("#88ff88"),

	/**
	 * Used when alerting the server of an event
	 */
	ALERT("#ff3030")
}