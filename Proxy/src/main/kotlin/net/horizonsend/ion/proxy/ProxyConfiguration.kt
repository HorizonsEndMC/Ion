package net.horizonsend.ion.proxy

import net.horizonsend.ion.common.annotations.ConfigurationName
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
@ConfigurationName("proxy")
data class ProxyConfiguration(
	val tablistHeaderMessage: String = "",
	val discordBotToken: String = "",
	val discordServer: Long = 0,
	val linkedRole: Long = 0
)