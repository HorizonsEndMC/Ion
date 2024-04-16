package net.horizonsend.ion.server.features.ai.configuration

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.World

@Serializable
data class WorldSettings(
	val worldName: String,
	val probability: Double,

	val minDistanceFromPlayer: Double,
	val maxDistanceFromPlayer: Double,

	val templates: List<AITemplate.SpawningInformationHolder>
) {
	fun getWorld(): World = Bukkit.getWorld(worldName)!!
}
