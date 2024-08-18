package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class FeatureFlags(
	val economy: Boolean = true,
	val bounties: Boolean = true,
	val tutorials: Boolean = false,
	val combatNPCs: Boolean = true,
)
