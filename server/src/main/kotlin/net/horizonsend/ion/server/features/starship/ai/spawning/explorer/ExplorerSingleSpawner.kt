package net.horizonsend.ion.server.features.starship.ai.spawning.explorer

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class ExplorerSingleSpawner : BasicSpawner(
	"EXPLORATION_VESSEL",
	IonServer.aiShipConfiguration.spawners::explorationVessel,
) {
	override fun findSpawnLocation(): Location? = findExplorerSpawnLocation(configuration)

	override val spawnMessage: Component = ofChildren(
		Component.text("[", HEColorScheme.HE_LIGHT_GRAY),
		Component.text("", HEColorScheme.HE_LIGHT_GRAY),
		Component.text("{3} System Alert", EXPLORER_LIGHT_CYAN),
		Component.text("]", HEColorScheme.HE_LIGHT_GRAY),
		Component.text(" Explorer ", EXPLORER_MEDIUM_CYAN),
		Component.text("activity in the area.", HEColorScheme.HE_LIGHT_GRAY)
	) // TODO

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer patrol <${HEColorScheme.HE_MEDIUM_GRAY.asHexString()}>operation vessel {0} spawned at {1}, {2}, {3}, in {4}",
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 15,
			tiers = listOf(
				AIShipConfiguration.AISpawnerTier(
					identifier = "SMALL",
					nameList = mapOf(
						"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot" to 5,
//						"<$EXPLORER_LIGHT_CYAN>Rookie <$EXPLORER_MEDIUM_CYAN>Pilot" to 3,
					),
					ships = mapOf(
						minhaulCheth.identifier to 2,
						minhaulRedstone.identifier to 2,
						minhaulTitanium.identifier to 2,
					)
				),
//				AIShipConfiguration.AISpawnerTier(
//					identifier = "NORMAL",
//					nameList = mapOf(
//						"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Rookie" to 2,
//						"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Trainee" to 5,
//					),
//					ships = mapOf(
//						protector.identifier to 2,
//						furious.identifier to 2,
//						inflict.identifier to 2,
//						veteran.identifier to 1,
//						patroller.identifier to 1
//					)
//				),
//				AIShipConfiguration.AISpawnerTier(
//					identifier = "MODERATE",
//					nameList = mapOf(
//						"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Pilot" to 5,
//						"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Veteran" to 2,
//					),
//					ships = mapOf(
//						contractor.identifier to 2,
//						teneta.identifier to 2,
//						veteran.identifier to 2,
//						patroller.identifier to 2
//					)
//				),
//				AIShipConfiguration.AISpawnerTier(
//					identifier = "ADVANCED",
//					nameList = mapOf(
//						"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Veteran" to 5,
//						"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Ace" to 2,
//						"<$PRIVATEER_MEDIUM_TEAL>System Defense <$PRIVATEER_LIGHT_TEAL>Pilot" to 2
//					),
//					ships = mapOf(
//						contractor.identifier to 4,
//						daybreak.identifier to 2,
//						veteran.identifier to 4,
//						patroller.identifier to 2
//					)
//				),
			),
			worldSettings = listOf(
				AIShipConfiguration.AIWorldSettings(world = "Asteri", rolls = 7, tiers = mapOf(
					"SMALL" to 2,
				)),
				AIShipConfiguration.AIWorldSettings(world = "Sirius", rolls = 10, tiers = mapOf(
					"SMALL" to 2,
				)),
				AIShipConfiguration.AIWorldSettings(world = "Regulus", rolls = 15, tiers = mapOf(
					"SMALL" to 2,
				)),
				AIShipConfiguration.AIWorldSettings(world = "Ilios", rolls = 5, tiers = mapOf(
					"SMALL" to 2,
				)),
				AIShipConfiguration.AIWorldSettings(world = "Horizon", rolls = 5, tiers = mapOf(
					"SMALL" to 4,
				)),
				AIShipConfiguration.AIWorldSettings(world = "Trench", rolls = 2, tiers = mapOf(
					"SMALL" to 2,
				)),
				AIShipConfiguration.AIWorldSettings(world = "AU-0821", rolls = 2, tiers = mapOf(
					"SMALL" to 2,
				))
			)
		)
	}
}
