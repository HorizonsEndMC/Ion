package net.horizonsend.ion.common.configuration.shared

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SharedConfiguration(
	val tablistHeaderMessage: String = ""
)