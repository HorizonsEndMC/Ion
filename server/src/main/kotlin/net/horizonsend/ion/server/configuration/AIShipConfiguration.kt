package net.horizonsend.ion.server.configuration

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.ExplorerMultiSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.ExplorerSingleSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.dessle
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.exotranChetherite
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.exotranRedstone
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.exotranTitanium
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.minhaulCheth
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.minhaulRedstone
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.minhaulTitanium
import net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp.MiningCorpMultiSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp.MiningCorpSingleSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.pirate.PirateMultiSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.pirate.PirateSingleSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PrivateerMultiSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PrivateerSingleSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.bulwark
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.contractor
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.dagger
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.furious
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.inflict
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.patroller
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.protector
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.teneta
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.veteran
import net.horizonsend.ion.server.features.starship.ai.spawning.tsaii.TsaiiMultiSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.tsaii.TsaiiSingleSpawner
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import org.apache.commons.lang.math.DoubleRange
import org.bukkit.Bukkit
import org.bukkit.World
import java.io.File
import kotlin.jvm.optionals.getOrNull

/** Registration and spawning parameters of AI ships **/
@Serializable
data class AIShipConfiguration(
	val templates: MutableList<AIStarshipTemplate> = mutableListOf(
		// Privateer Start
		bulwark,
		contractor,
		dagger,
		patroller,
		protector,
		veteran,
		teneta,
		furious,
		inflict,
		// Privateer End
		// Pirate Start
		// Pirate End
		// Tsaii Start
		// Tsaii End
		// Explorer Start
		minhaulCheth,
		minhaulRedstone,
		minhaulTitanium,
//		exotranChetherite,
//		exotranRedstone,
//		exotranTitanium,
//		dessle,
		// Explorer End
		// Mining Corp Start
		// Mining Corp End
	),
	val spawners: AISpawners = AISpawners()
) {
	fun getShipTemplate(identifier: String) = templates.first { it.identifier == identifier }

	@Serializable
	data class AISpawners(
		val miningCorpSingleSpawner: AISpawnerConfiguration = MiningCorpSingleSpawner.defaultConfiguration,
		val miningCorpMultiSpawner: AISpawnerConfiguration = MiningCorpMultiSpawner.defaultConfiguration,
		val privateerMulti: AISpawnerConfiguration = PrivateerMultiSpawner.defaultConfiguration,
		val privateerSingle: AISpawnerConfiguration = PrivateerSingleSpawner.defaultConfiguration,
		val explorationVessel: AISpawnerConfiguration = ExplorerSingleSpawner.defaultConfiguration,
		val explorationConvoy: AISpawnerConfiguration = ExplorerMultiSpawner.defaultConfiguration,
		val singlePirate: AISpawnerConfiguration = PirateSingleSpawner.defaultConfiguration,
		val pirateFleet: AISpawnerConfiguration = PirateMultiSpawner.defaultConfiguration,
		val tsaiiAttack: AISpawnerConfiguration = TsaiiSingleSpawner.defaultConfiguration,
		val tsaiiRaid: AISpawnerConfiguration = TsaiiMultiSpawner.defaultConfiguration,
	)

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
		@Transient
		val worldWeightedRandomList = WeightedRandomList(worldSettings.associateWith { it.rolls })

		fun getWorld(world: World) = worldSettings.firstOrNull { it.world == world.name }

		fun getTier(identifier: String) = tiers.firstOrNull { it.identifier == identifier } ?: throw NoSuchElementException("Tier $identifier not found!")
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
	) {
		@Transient
		val shipsWeightedList: WeightedRandomList<String> = WeightedRandomList(ships)

		@Transient
		val namesWeightedList: WeightedRandomList<String> = WeightedRandomList(nameList)
	}

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

		@Transient
		val tierWeightedRandomList: WeightedRandomList<String> = WeightedRandomList(tiers)
	}

	@Serializable
	data class AIStarshipTemplate(
		val identifier: String = "VESTA",
		var schematicName: String = "Vesta",
		var miniMessageName: String = "<red><bold>Vesta",
		var type: StarshipType = StarshipType.SHUTTLE,
		var controllerFactory: String = "STARFIGHTER",
		var xpMultiplier: Double = 1.0,
		var creditReward: Double = 100.0,
		val manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
		val autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
		val mobs: MutableSet<MobSpawner> = mutableSetOf()
	) {
		init {
//			if (AISpawningManager.templates.values.contains(this)) error("Identifiers must be unique! $identifier already exists!")

			AISpawningManager.templates[identifier] = this
		}

		@Transient
		val schematicFile: File = IonServer.dataFolder.resolve("aiShips").resolve("$schematicName.schem")

		fun getSchematic(): Clipboard? = AISpawningManager.schematicCache[identifier].getOrNull()

		@Serializable
		data class WeaponSet(val name: String, private val engagementRangeMin: Double, private val engagementRangeMax: Double) {
			@Transient
			val engagementRange = DoubleRange(engagementRangeMin, engagementRangeMax)
		}

		@Serializable
		data class MobSpawner(
			val entityLocations: Set<Vec3i>,
			val entity: ServerConfiguration.PlanetSpawnConfig.Mob
		)
	}
}
