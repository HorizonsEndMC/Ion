package net.horizonsend.ion.proxy

import kotlinx.serialization.Serializable

@Serializable
data class ProxyConfiguration(
	val discordEnabled: Boolean = false,
	val discordBotToken: String = "",
	val motdFirstLine: String = "",
	val discordServer: Long = 0,
	val globalChannel: Long = 0
)

