package net.horizonsend.ion.server.features.ai.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import org.bukkit.Bukkit
import org.bukkit.World

/** Registration and spawning parameters of AI ships **/
@Serializable
class AISpawningConfiguration {
	/**
	 * @param miniMessageSpawnMessage The custom message to send when this spawner spawns a ship, uses string templates {0}, {1}, etc.
	 * @param pointChance Chance for a ship to spawn whenever this spawner is triggered.
	 * @param worldSettings each contains a list of defined AI ship template identifiers, and their number of rolls when this world is selected.
	 *
	 * @see AIStarshipTemplate
	 * @See AISpawner
	 **/
	@Serializable
	data class AISpawnerConfiguration(
		val miniMessageSpawnMessage: String = "",
		val pointChance: Double = 1.0,
		val pointThreshold: Int = 20 * 60 * 15,
		val minDistanceFromPlayer: Double = 1500.0,
		val maxDistanceFromPlayer: Double = 3500.0,
		val tiers: List<AISpawnerTier> = listOf(AISpawnerTier()),
		val worldSettings: List<AIWorldSettings> = listOf(AIWorldSettings())
	) {
		fun getWorld(world: World) = worldSettings.firstOrNull { it.world == world.name }
	}

	/**
	 * Each world has a number of rolls for selection when a ship spawns
	 *
	 * @param identifier, the tier of this identifier
	 * @param ships Map of AI ship templates to their number of rolls.
	 *
	 * @see AISpawnerConfiguration
	 * @see AIStarshipTemplate
	 **/
	@Serializable
	data class AISpawnerTier(
		val identifier: String = "BASIC",
		val nameList: Map<String, Int> = mapOf("<Red><Bold>Level 1 thug" to 1),
		val ships: Map<String, Int> = mapOf("VESTA" to 1),
	)

	/**
	 * Each world has a number of rolls for selection when a ship spawns
	 *
	 * @param world The bukkit world's name.
	 * @param rolls then number of rolls for this world.
	 * @param tiers Map of AI ship templates to their number of rolls.
	 *
	 * @see AISpawnerConfiguration
	 * @see AIStarshipTemplate
	 **/
	@Serializable
	data class AIWorldSettings(
		val world: String = "world",
		val rolls: Int = 1,
		val tiers: Map<String, Int> = mapOf("BASIC" to 1),
	) {
		fun getWorld(): World = Bukkit.getWorld(world) ?: throw NullPointerException("World $world not found!")
	}
}
