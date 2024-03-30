package net.horizonsend.ion.server.configuration

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.starship.ai.spawning.alien.AlienSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.alien.alienTemplates
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.ExplorerSingleSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.explorer.explorerTemplates
import net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp.MiningCorpSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp.miningGuildTemplates
import net.horizonsend.ion.server.features.starship.ai.spawning.pirate.PirateSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.pirate.pirateShips
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PrivateerSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.privateerTemplates
import net.horizonsend.ion.server.features.starship.ai.spawning.tsaii.TsaiiSpawner
import net.horizonsend.ion.server.features.starship.ai.spawning.tsaii.tsaiiTemplates
import net.horizonsend.ion.server.features.starship.modules.AICreditRewardProvider
import net.horizonsend.ion.server.features.starship.modules.AIItemRewardProvider
import net.horizonsend.ion.server.features.starship.modules.AIXPRewardProvider
import net.horizonsend.ion.server.features.starship.modules.RewardsProvider
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import org.apache.commons.lang.math.DoubleRange
import org.bukkit.Bukkit
import org.bukkit.World
import java.io.File
import kotlin.jvm.optionals.getOrNull

/** Registration and spawning parameters of AI ships **/
@Serializable
data class AISpawningConfiguration(
	val templates: MutableList<AIStarshipTemplate> = mutableListOf(
		*privateerTemplates,
		*pirateShips,
		*tsaiiTemplates,
		*explorerTemplates,
		*miningGuildTemplates,
		*alienTemplates
	),
	val spawners: AISpawners = AISpawners()
) {
	fun getShipTemplate(identifier: String) = templates.first { it.identifier == identifier }

	@Serializable
	data class AISpawners(
		val miningCorp: AISpawnerConfiguration = MiningCorpSpawner.defaultConfiguration,
		val privateer: AISpawnerConfiguration = PrivateerSpawner.defaultConfiguration,
		val explorer: AISpawnerConfiguration = ExplorerSingleSpawner.defaultConfiguration,
		val pirate: AISpawnerConfiguration = PirateSpawner.defaultConfiguration,
		val tsaii: AISpawnerConfiguration = TsaiiSpawner.defaultConfiguration,
		val alien: AISpawnerConfiguration = AlienSpawner.defaultConfiguration,
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
		var color: Int = Integer.parseInt("ff0000", 16),

		var type: StarshipType = StarshipType.SHUTTLE,

		var controllerFactory: String = "STARFIGHTER",

		val rewardProviders: List<AIRewardsProviderConfiguration> = listOf(
			CreditRewardProviderConfiguration(creditReward = 100.0),
			SLXPRewardProviderConfiguration(xpMultiplier = 1.0),
		),

		var maxSpeed: Int = -1,
		var engagementRange: Double = 500.0,

		val manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
		val autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),

		val smackInformation: SmackInformation? = null,
		val radiusMessageInformation: RadiusMessageInformation? = null,
		val reinforcementInformation: ReinforcementInformation? = null
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
		data class SmackInformation(
			val prefix: String,
			val messages: List<String>
		)

		@Serializable
		data class RadiusMessageInformation(
			val prefix: String,
			val messages: Map<Double, String>
		)

		@Serializable
		data class ReinforcementInformation(
			val activationThreshold: Double,
			val delay: Long,
			val broadcastMessage: String?,
			val configuration: AISpawnerConfiguration
		)

		@Serializable
		data class CreditRewardProviderConfiguration(
			val creditReward: Double
		) : AIRewardsProviderConfiguration {
			override fun createRewardsProvider(starship: ActiveControlledStarship, template: AIStarshipTemplate): RewardsProvider {
				return AICreditRewardProvider(starship, this)
			}
		}

		@Serializable
		data class SLXPRewardProviderConfiguration(
			val xpMultiplier: Double
		) : AIRewardsProviderConfiguration {
			override fun createRewardsProvider(starship: ActiveControlledStarship, template: AIStarshipTemplate): RewardsProvider {
				return AIXPRewardProvider(starship, this)
			}
		}

		@Serializable
		data class ItemRewardProviderConfiguration(
			val items: List<ServerConfiguration.PlanetSpawnConfig.DroppedItem>
		) : AIRewardsProviderConfiguration {
			override fun createRewardsProvider(starship: ActiveControlledStarship, template: AIStarshipTemplate): RewardsProvider {
				return AIItemRewardProvider(starship, this)
			}
		}

		@Serializable
		sealed interface AIRewardsProviderConfiguration {
			fun createRewardsProvider(starship: ActiveControlledStarship, template: AIStarshipTemplate): RewardsProvider
		}
	}
}
