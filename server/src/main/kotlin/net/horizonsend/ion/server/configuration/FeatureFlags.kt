package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class FeatureFlags(
	val ECONOMY: Boolean = true,
	val BOUNTIES: Boolean = true,
)
