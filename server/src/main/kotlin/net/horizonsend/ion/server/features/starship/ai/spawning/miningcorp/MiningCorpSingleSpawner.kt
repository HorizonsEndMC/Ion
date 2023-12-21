package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AISpawnerConfiguration
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AISpawnerTier
import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIWorldSettings
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class MiningCorpSingleSpawner  : BasicSpawner(
	"MINING_CORP_TRANSPORT",
	IonServer.aiShipConfiguration.spawners::miningCorpSingleSpawner,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AISpawnerConfiguration(
			miniMessageSpawnMessage = "<${MINING_CORP_LIGHT_ORANGE.asHexString()}>Privateer patrol <${HEColorScheme.HE_MEDIUM_GRAY}>operation vessel {0} spawned at {1}, {2}, {3}, in {4}",
			pointChance = 1.0,
			pointThreshold = 2500,
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
					tiers = mapOf(
						"STANDARD" to 10,
						"MODERATE" to 2
					)
				),
				AIWorldSettings(
					world = "Regulus",
					tiers = mapOf(
						"STANDARD" to 4,
						"MODERATE" to 8,
						"ADVANCED" to 1
					)
				),
				AIWorldSettings(
					world = "Sirius",
					tiers = mapOf(
						"STANDARD" to 10,
						"MODERATE" to 2
					)
				),
				AIWorldSettings(
					world = "Ilios",
					tiers = mapOf(
						"STANDARD" to 2,
						"MODERATE" to 2
					)
				),
				AIWorldSettings(
					world = "Horizon",
					tiers = mapOf(
						"MODERATE" to 8,
						"ADVANCED" to 1,
						"ULTRA" to 1
					)
				),
				AIWorldSettings(
					world = "Trench",
					tiers = mapOf(
						"STANDARD" to 2,
						"MODERATE" to 8,
						"ADVANCED" to 4,
						"ULTRA" to 2
					)
				),
				AIWorldSettings(
					world = "AU-0821",
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
