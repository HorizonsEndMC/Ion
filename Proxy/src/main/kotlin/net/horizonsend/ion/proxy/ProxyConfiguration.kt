package net.horizonsend.ion.proxy

import net.horizonsend.ion.common.annotations.ConfigurationName
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
@ConfigurationName("proxy")
data class ProxyConfiguration(
	val discordBotToken: String = "",
	val motdFirstLine: String = "",
	val discordServer: Long = 0,
	val unlinkedRole: Long = 0,
	val linkedRole: Long = 0,
	val onlineRole: Long = 0
)