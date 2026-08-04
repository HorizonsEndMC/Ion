package net.horizonsend.ion.server.features.npcs.database.metadata

import kotlinx.serialization.Serializable

@Serializable
data class ServerShipDealerMetadata(
	val ships: List<String> = listOf()
) : UniversalNPCMetadata
