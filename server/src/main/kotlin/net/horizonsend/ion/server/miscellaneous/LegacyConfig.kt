package net.horizonsend.ion.server.miscellaneous

data class LegacyConfig(
	// @Comment("Path to folder to keep configuration files shared between servers in")
	val sharedFolder: String = "shared/starlegacy",

	// @Comment("Whether this is the master server which runs certain tasks like settlement purges")
	val master: Boolean = false,

	// @Comment("Whether to allow players in the same group to attack eachother on this server")
	val allowFriendlyFire: Boolean = false,

	// @Comment("The multiplier for the cost of territories in credits for nations and settlements")
	val territoryCost: Int = 10,

	// @Comment("Settings for chat channels")
	val chat: Chat = Chat(),

	val dutyModeMonitorWebhook: String? = null,
) {
	data class Chat(
		// @Comment("The maximum amount of blocks away someone can be and still see someone's local chat")
		val localDistance: Int = 200,

		// @Comment("A list of names of worlds in which global chat should be disabled")
		val noGlobalWorlds: List<String> = listOf()
	)
}
