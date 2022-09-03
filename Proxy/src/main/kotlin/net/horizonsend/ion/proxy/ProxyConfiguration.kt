package net.horizonsend.ion.proxy

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ProxyConfiguration(
	val discordBotToken: String = "",
	val motdFirstLine: String = "",
	val linkBypassRole: Long = 0,
	val discordServer: Long = 0,
	val unlinkedRole: Long = 0,
	val linkedRole: Long = 0,
	val onlineRole: Long = 0
)