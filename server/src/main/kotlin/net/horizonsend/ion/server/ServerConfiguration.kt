package net.horizonsend.ion.server

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ServerConfiguration(
	val serverName: String? = null,
	val ParticleColourChoosingMoneyRequirement: Double? = 5.0
)
