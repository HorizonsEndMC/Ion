package net.horizonsend.ion.server.features.starship.ai.spawning.privateer

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class PrivateerSingleSpawner : BasicSpawner(
	"PRIVATEER_SINGLE",
	IonServer.aiShipConfiguration.spawners::privateerSingle,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	override val spawnMessage: Component = ofChildren(
		Component.text("{3}", HEColorScheme.HE_LIGHT_GRAY),
		Component.text(" System Defense Forces ", PRIVATEER_LIGHT_TEAL),
		Component.text("have started a patrol.", HEColorScheme.HE_LIGHT_GRAY)
	)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer patrol <${HEColorScheme.HE_MEDIUM_GRAY.asHexString()}>operation vessel {0} spawned at {1}, {2}, {3}, in {4}",
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 15,
			tiers = listOf(
				AIShipConfiguration.AISpawnerTier(
					identifier = "EASY",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Rookie" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Trainee" to 2,
					),
					ships = mapOf(
						dagger.identifier to 2,
						protector.identifier to 2,
						furious.identifier to 2,
						inflict.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "NORMAL",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Rookie" to 2,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Trainee" to 5,
					),
					ships = mapOf(
						protector.identifier to 2,
						furious.identifier to 2,
						inflict.identifier to 2,
						veteran.identifier to 1,
						patroller.identifier to 1
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "MODERATE",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Pilot" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Veteran" to 2,
					),
					ships = mapOf(
						contractor.identifier to 2,
						teneta.identifier to 2,
						veteran.identifier to 2,
						patroller.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "ADVANCED",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Veteran" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Ace" to 2,
						"<${PRIVATEER_MEDIUM_TEAL}>System Defense <${PRIVATEER_LIGHT_TEAL}>Pilot" to 2
					),
					ships = mapOf(
						contractor.identifier to 4,
						daybreak.identifier to 2,
						veteran.identifier to 4,
						patroller.identifier to 2
					)
				),
				AIShipConfiguration.AISpawnerTier(
					identifier = "EXPERT",
					nameList = mapOf(
						"<${PRIVATEER_MEDIUM_TEAL}>Expert Privateer <${PRIVATEER_LIGHT_TEAL}>Ace" to 5,
						"<${PRIVATEER_MEDIUM_TEAL}>Expert Privateer <${PRIVATEER_LIGHT_TEAL}>Veteran" to 2
					),
					ships = mapOf(
						bulwark.identifier to 4,
						daybreak.identifier to 2,
						contractor.identifier to 1,
					)
				)
			),
			worldSettings = listOf(
				AIShipConfiguration.AIWorldSettings(world = "Asteri", rolls = 7, tiers = mapOf(
					"EASY" to 2,
					"NORMAL" to 2,
					"MODERATE" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Sirius", rolls = 10, tiers = mapOf(
					"EASY" to 2,
					"NORMAL" to 2,
					"MODERATE" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Regulus", rolls = 15, tiers = mapOf(
					"NORMAL" to 2,
					"MODERATE" to 2,
					"ADVANCED" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Ilios", rolls = 5, tiers = mapOf(
					"NORMAL" to 2,
					"MODERATE" to 2,
					"ADVANCED" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Horizon", rolls = 5, tiers = mapOf(
					"MODERATE" to 4,
					"ADVANCED" to 4,
					"EXPERT" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "Trench", rolls = 2, tiers = mapOf(
					"MODERATE" to 2,
					"ADVANCED" to 2,
					"EXPERT" to 2
				)),
				AIShipConfiguration.AIWorldSettings(world = "AU-0821", rolls = 2, tiers = mapOf(
					"MODERATE" to 2,
					"ADVANCED" to 4,
					"EXPERT" to 4
				))
			)
		)
	}
}
