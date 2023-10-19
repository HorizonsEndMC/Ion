package net.horizonsend.ion.server.configuration

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import org.apache.commons.lang.math.DoubleRange
import org.bukkit.Bukkit
import org.bukkit.World
import java.io.File
import kotlin.jvm.optionals.getOrNull

/** Registration and spawning parameters of AI ships **/
@Serializable
data class AIShipConfiguration(
	val spawnRate: Long = 20 * 60 * 15,
	val templates: List<AIStarshipTemplate> = listOf(AIStarshipTemplate()),
	val spawners: List<AISpawnerConfiguration> = listOf(AISpawnerConfiguration())
) {
	fun getShipTemplate(identifier: String) = templates.first { it.identifier == identifier }
	fun spawnerWeightedRandomList(): WeightedRandomList<AISpawnerConfiguration> = WeightedRandomList(spawners.associateWith { it.rolls })

	/**
	 * Each world has a number of rolls for selection when a ship spawns
	 * Feeds config values to an AISpawner with the same identifier.
	 * Allows varied ships by world.
	 *
	 * @param worldSettings contains a list of defined AI ship template identifiers, and their number of rolls when this world is selected
	 *
	 * @see AIStarshipTemplate
	 * @See AISpawner
	 **/
	@Serializable
	data class AISpawnerConfiguration (
		val identifier: String = "CARGO_MISSION",
		val rolls: Int = 1,
		val miniMessageSpawnMessage: String = "",
		val worldSettings: Map<AIWorldSettings, Int> = mapOf(AIWorldSettings() to 1)
	) {
		@Transient
		val worldWeightedRandomList = WeightedRandomList(worldSettings)

		fun getWorld(world: World) = worldSettings.keys.firstOrNull() { it.world == world.name }

		fun availableForWorld(worldName: String) = worldSettings.keys.any { it.world == worldName }
	}

	/**
	 * Each world has a number of rolls for selection when a ship spawns
	 *
	 * @param rolls, then number of rolls for this world
	 *
	 * @see AISpawnerConfiguration
	 **/
	@Serializable
	data class AIWorldSettings(
		val world: String = "world",
		val rolls: Int = 1,
		val ships: Map<String, Int> = mapOf("VESTA" to 1),
	) {
		fun getWorld(): World = Bukkit.getWorld(world)!!

		@Transient
		val shipsWeightedList: WeightedRandomList<String> = WeightedRandomList(ships)
	}

	@Serializable
	data class AIStarshipTemplate(
		val identifier: String = "VESTA",
		val name: String = "Vesta",
		val schematicName: String = "Vesta",
		val miniMessageName: String = "<red><bold>Vesta",
		val type: StarshipType = StarshipType.SHUTTLE,
		val weaponSets: Set<WeaponSet> = setOf(),
	) {
		init {
			if (AISpawningManager.templates.values.contains(this)) error("Identifiers must be unique!")

			AISpawningManager.templates[identifier] = this
		}

		@Transient
		val schematicFile: File = IonServer.dataFolder.resolve("aiShips").resolve("$schematicName.schem")

		fun getSchematic(): Clipboard? = AISpawningManager.schematicCache[schematicName].getOrNull()

		@Serializable
		data class WeaponSet(val name: String, private val engagementRangeMin: Double, private val engagementRangeMax: Double) {
			@Transient
			val engagementRange = DoubleRange(engagementRangeMin, engagementRangeMax)
		}
	}
}
