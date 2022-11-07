package net.horizonsend.ion.proxy

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ProxyConfiguration(
	val discordBotToken: String = "",
	val motdFirstLine: String = "",
	val linkBypassRole: Long = 0,
	val discordServer: Long = 0,
	val globalChannel: Long = 0,
	val unlinkedRole: Long = 0,
	val linkedRole: Long = 0,
	val voteSites: Map<String, String> = mapOf("Example1.com" to "Example1Name", "Example2.com" to "Example2Name")
)