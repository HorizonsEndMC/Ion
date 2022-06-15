package net.horizonsend.ion.common.configuration.server

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ServerConfiguration(
	val serverType: ServerType = ServerType.SURVIVAL
)