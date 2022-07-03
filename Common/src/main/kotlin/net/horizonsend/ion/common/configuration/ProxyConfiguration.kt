package net.horizonsend.ion.common.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
@ConfigurationName("proxy")
data class ProxyConfiguration(
	val tablistHeaderMessage: String = ""
)