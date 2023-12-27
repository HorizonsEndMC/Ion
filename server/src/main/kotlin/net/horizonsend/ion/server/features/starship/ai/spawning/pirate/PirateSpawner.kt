package net.horizonsend.ion.server.features.starship.ai.spawning.pirate

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class PirateSpawner : BasicSpawner(
	"PIRATE_SINGLE",
	IonServer.aiSpawningConfiguration.spawners::pirateSingle,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	override val spawnMessage: Component = ofChildren(
		Component.text("[", HEColorScheme.HE_LIGHT_GRAY),
		Component.text("{3} System Alert", PIRATE_DARK_RED),
		Component.text("]", HEColorScheme.HE_LIGHT_GRAY),
		Component.text(" Pirate activity detected!", PIRATE_LIGHT_RED)
	)

	companion object {
		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<${HEColorScheme.HE_MEDIUM_GRAY}>A pirate {0} has been identified in the area of {1}, {3}, in {4}. <$PIRATE_SATURATED_RED>Please avoid the sector until the threat has been cleared.",
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 15,
			minDistanceFromPlayer = 2000.0,
			maxDistanceFromPlayer = 4000.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "PIRATE_TEST",
					nameList = mapOf(
						"<$PIRATE_DARK_RED>Rapscallion" to 2,
					),
					ships = mapOf(
						iskat.identifier to 2,
						voss.identifier to 2,
						hector.identifier to 2,
						hiro.identifier to 2,
						wasp.identifier to 2,
						frenz.identifier to 2,
						tempest.identifier to 2,
						velasco.identifier to 2,
						anaan.identifier to 2,
						vendetta.identifier to 2,
						cormorant.identifier to 2,
						mantis.identifier to 2,
						hernstein.identifier to 2,
						fyr.identifier to 2,
						bloodStar.identifier to 2
					)
				)
			),
			worldSettings = listOf(
				AISpawningConfiguration.AIWorldSettings(
					world = "Asteri",
					tiers = mapOf(
						"PIRATE_TEST" to 10
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Regulus",
					tiers = mapOf(
						"PIRATE_TEST" to 10
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Sirius",
					tiers = mapOf(
						"PIRATE_TEST" to 10
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Ilios",
					tiers = mapOf(
						"PIRATE_TEST" to 10
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Horizon",
					tiers = mapOf(
						"PIRATE_TEST" to 10
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Trench",
					tiers = mapOf(
						"PIRATE_TEST" to 10
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "AU-0821",
					tiers = mapOf(
						"PIRATE_TEST" to 10
					)
				)
			)
		)
	}
}
