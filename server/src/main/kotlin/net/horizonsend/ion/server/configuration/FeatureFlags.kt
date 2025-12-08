package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class FeatureFlags(
	var economy: Boolean = true,
	val bounties: Boolean = true,
	val tutorials: Boolean = false,
	val combatNPCs: Boolean = true,
	val combatTimers: Boolean = true,
	val aiSpawns: Boolean = true,
	val customTurrets: Boolean = false,
	val miningLaserAnimation: Boolean = true,
	val graphTransfer: Boolean = true,
	val bigUpdateMultiblocks: Boolean = false,
	val tractorBeams: Boolean = false,
	val boardingRamps: Boolean = false,
	val loadSpawnerData: Boolean = true,
)
