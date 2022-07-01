package net.horizonsend.ion.common.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
@ConfigurationName("shared/config")
data class SharedConfiguration(
	val tablistHeaderMessage: String = ""
)