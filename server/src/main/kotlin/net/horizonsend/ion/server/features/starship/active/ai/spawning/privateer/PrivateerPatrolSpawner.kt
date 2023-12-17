package net.horizonsend.ion.server.features.starship.active.ai.spawning.privateer

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class PrivateerPatrolSpawner : BasicSpawner(
	"PRIVATEER_PATROL",
	IonServer.aiShipConfiguration.spawners::privateerPatrol,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<#89d7b0>Privateer patrol <#E1E1E1>operation vessel {0} spawned at {1}, {2}, {3}, in {4}",
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 15,
			tiers = listOf(
				AIShipConfiguration.AISpawnerTier(
					identifier = "BASIC",
					rolls = 1,
					nameList = mapOf(
						"Basic Privateer Pilot 1" to 2,
						"Basic Privateer Pilot 2" to 2,
						"Basic Privateer Pilot 3" to 2
					),
					ships = mapOf(
						bulwark.identifier to 2,
						contractor.identifier to 2,
						dagger.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "ADVANCED",
					rolls = 1,
					nameList = mapOf(
						"Advanced Privateer Pilot 1" to 2,
						"Advanced Privateer Pilot 2" to 2,
						"Advanced Privateer Pilot 3" to 2
					),
					ships = mapOf(
						bulwark.identifier to 2,
						contractor.identifier to 2,
						dagger.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "EXPERT",
					rolls = 1,
					nameList = mapOf(
						"Expert Privateer Pilot 1" to 2,
						"Expert Privateer Pilot 2" to 2,
						"Expert Privateer Pilot 3" to 2
					),
					ships = mapOf(
						bulwark.identifier to 2,
						contractor.identifier to 2,
						dagger.identifier to 2
					)
				)
			),
			worldSettings = listOf(
				AIShipConfiguration.AIWorldSettings(world = "Asteri", rolls = 2, tiers = mapOf("BASIC" to 2)),
				AIShipConfiguration.AIWorldSettings(world = "Sirius", rolls = 2, tiers = mapOf("BASIC" to 2)),
				AIShipConfiguration.AIWorldSettings(world = "Regulus", rolls = 2, tiers = mapOf("BASIC" to 2)),
				AIShipConfiguration.AIWorldSettings(world = "Ilios", rolls = 2, tiers = mapOf("BASIC" to 2)),
				AIShipConfiguration.AIWorldSettings(world = "Horizon", rolls = 2, tiers = mapOf("BASIC" to 2)),
				AIShipConfiguration.AIWorldSettings(world = "Trench", rolls = 2, tiers = mapOf("BASIC" to 2)),
				AIShipConfiguration.AIWorldSettings(world = "AU-0821", rolls = 2, tiers = mapOf("BASIC" to 2)),)
		)
	}
}
