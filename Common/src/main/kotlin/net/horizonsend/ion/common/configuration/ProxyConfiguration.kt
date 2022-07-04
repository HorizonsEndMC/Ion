package net.horizonsend.ion.common.configuration

import net.horizonsend.ion.common.annotations.ConfigurationName
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
@ConfigurationName("proxy")
data class ProxyConfiguration(
	val tablistHeaderMessage: String = ""
)