package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.common.utils.text.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AISpawnerConfiguration
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AISpawnerTier
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIWorldSettings
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location

class MiningCorpSpawner  : BasicSpawner(
	"MINING_CORP_SINGLE",
	IonServer.aiSpawningConfiguration.spawners::miningCorpSpawner,
) {
	override fun findSpawnLocation(): Location? = findMiningCorpSpawnPosition(configuration)

	override val spawnMessage: Component = ofChildren(
		text("The ", HE_MEDIUM_GRAY),
		text("Mining ", MINING_CORP_LIGHT_ORANGE),
		text("Guild ", MINING_CORP_DARK_ORANGE),
		text("branch of {3} requests non-violence during extraction operations.", HE_MEDIUM_GRAY)
	)

	companion object {
		val miningGuild = "<$MINING_CORP_LIGHT_ORANGE>Mining<$MINING_CORP_DARK_ORANGE> Guild"

		val defaultConfiguration = AISpawnerConfiguration(
			miniMessageSpawnMessage = "$miningGuild <${HE_MEDIUM_GRAY}>extraction vessel {0} spawned at {1}, {3}, in {4}",
			pointChance = 1.0,
			pointThreshold = 20 * 60 * 15,
			minDistanceFromPlayer = 1000.0,
			maxDistanceFromPlayer = 2500.0,
			tiers = listOf(
				AISpawnerTier(
					identifier = "STANDARD",
					nameList = mapOf(
						"<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Lieutenant <white>1" to 2,
						"<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Lieutenant <white>2" to 2,
						"<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Lieutenant <white>3" to 2
					),
					ships = mapOf(
						woodpecker.identifier to 2,
						typeV11.identifier to 2,
						typeA21b.identifier to 2,
						typeI41.identifier to 2
					)
				),
				AISpawnerTier(
					identifier = "MODERATE",
					nameList = mapOf(
						"<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Ship's Captain <white>1" to 2,
						"<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Ship's Captain <white>2" to 2,
						"<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Ship's Captain <white>3" to 2
					),
					ships = mapOf(
						woodpecker.identifier to 2,
						typeV11.identifier to 2,
						typeA21b.identifier to 2,
						typeI41.identifier to 2,
						badger.identifier to 3
					)
				),
				AISpawnerTier(
					identifier = "ADVANCED",
					nameList = mapOf(
						"<${MINING_CORP_DARK_ORANGE.asHexString()}>Ship's Captain <white>1" to 2,
						"<${MINING_CORP_DARK_ORANGE.asHexString()}>Ship's Captain <white>2" to 2,
						"<${MINING_CORP_DARK_ORANGE.asHexString()}>Ship's Captain <white>3" to 2
					),
					ships = mapOf(
						woodpecker.identifier to 2,
						typeV11.identifier to 2,
						typeA21b.identifier to 2,
						typeI41.identifier to 2,
						beaver.identifier to 6,
						badger.identifier to 2
					)
				),
				AISpawnerTier(
					identifier = "ULTRA",
					nameList = mapOf(
						"<${MINING_CORP_DARK_ORANGE.asHexString()}>Ship's Captain <white>1" to 2,
						"<${MINING_CORP_DARK_ORANGE.asHexString()}>Ship's Captain <white>2" to 2,
						"<${MINING_CORP_DARK_ORANGE.asHexString()}>Ship's Captain <white>3" to 2
					),
					ships = mapOf(
						ostrich.identifier to 2,
						badger.identifier to 2,
						beaver.identifier to 3
					)
				)
			),
			worldSettings = listOf(
				AIWorldSettings(
					world = "Asteri",
					rolls = 3,
					tiers = mapOf(
						"STANDARD" to 10,
						"MODERATE" to 2
					)
				),
				AIWorldSettings(
					world = "Regulus",
					rolls = 7,
					tiers = mapOf(
						"STANDARD" to 4,
						"MODERATE" to 8,
						"ADVANCED" to 1
					)
				),
				AIWorldSettings(
					world = "Sirius",
					rolls = 5,
					tiers = mapOf(
						"STANDARD" to 10,
						"MODERATE" to 2
					)
				),
				AIWorldSettings(
					world = "Ilios",
					rolls = 2,
					tiers = mapOf(
						"STANDARD" to 2,
						"MODERATE" to 2
					)
				),
				AIWorldSettings(
					world = "Horizon",
					rolls = 10,
					tiers = mapOf(
						"MODERATE" to 8,
						"ADVANCED" to 1,
						"ULTRA" to 1
					)
				),
				AIWorldSettings(
					world = "Trench",
					rolls = 20,
					tiers = mapOf(
						"STANDARD" to 2,
						"MODERATE" to 8,
						"ADVANCED" to 4,
						"ULTRA" to 2
					)
				),
				AIWorldSettings(
					world = "AU-0821",
					rolls = 2,
					tiers = mapOf(
						"STANDARD" to 2,
						"MODERATE" to 8,
						"ADVANCED" to 4,
						"ULTRA" to 2
					)
				)
			)
		)
	}
}
