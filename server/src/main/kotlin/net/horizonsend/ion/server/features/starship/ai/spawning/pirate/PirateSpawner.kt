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
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "WEAKEST",
					nameList = mapOf(
						"<$PIRATE_DARK_RED>Rapscallion" to 200,
						"<$PIRATE_DARK_RED>Poopdeck Pete" to 2,
					),
					ships = mapOf(
						iskat.identifier to 3,
						voss.identifier to 2,
						hector.identifier to 2
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "WEAK",
					nameList = mapOf(
						"<$PIRATE_DARK_RED>Rapscallion" to 2,
						"<$PIRATE_DARK_RED>Swashbuckler" to 2,
					),
					ships = mapOf(
						iskat.identifier to 2,
						voss.identifier to 2,
						hector.identifier to 4,
						hiro.identifier to 4,
						wasp.identifier to 4,
						frenz.identifier to 4
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "NORMAL",
					nameList = mapOf(
						"<$PIRATE_DARK_RED>Swashbuckler" to 2,
						"<$PIRATE_DARK_RED>Swashbuckler Drake" to 2,
						"<$PIRATE_DARK_RED>Swashbuckler Pat" to 2,
					),
					ships = mapOf(
						hiro.identifier to 4,
						wasp.identifier to 4,
						frenz.identifier to 4
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "MODERATE",
					nameList = mapOf(
						"<$PIRATE_DARK_RED>Corsair Jones" to 2,
						"<$PIRATE_DARK_RED>Corsair Jones" to 2,
						"<$PIRATE_DARK_RED>Buccaneer Bonny" to 2,
						"<$PIRATE_DARK_RED>Buccaneer Jack" to 2,
					),
					ships = mapOf(
						hiro.identifier to 4,
						wasp.identifier to 4,
						frenz.identifier to 4,
						tempest.identifier to 2,
						velasco.identifier to 2
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "STRONG",
					nameList = mapOf(
						"<italic><$PIRATE_DARK_RED>Warlord Jay" to 2,
						"<italic><$PIRATE_DARK_RED>Warlord Steve" to 2,
						"<italic><$PIRATE_DARK_RED>Warlord Greg" to 2
					),
					ships = mapOf(
						frenz.identifier to 4,
						tempest.identifier to 2,
						velasco.identifier to 2,
						anaan.identifier to 2,
						vendetta.identifier to 2,
						cormorant.identifier to 2
					)
				),
				AISpawningConfiguration.AISpawnerTier(
					identifier = "EXTRA_STRONG",
					nameList = mapOf(
						"<bold><$PIRATE_DARK_RED>Raimus, Archon" to 2,
						"<bold><$PIRATE_DARK_RED>Warlord Drake" to 2,
					),
					ships = mapOf(
						anaan.identifier to 2,
						vendetta.identifier to 2,
						cormorant.identifier to 2,
						mantis.identifier to 2,
						hernstein.identifier to 2,
						fyr.identifier to 2
					)
				),
			),
			worldSettings = listOf(
				AISpawningConfiguration.AIWorldSettings(
					world = "Asteri",
					rolls = 5,
					tiers = mapOf(
						"WEAKEST" to 2,
						"WEAK" to 2,
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Regulus",
					rolls = 10,
					tiers = mapOf(
						"WEAK" to 10,
						"NORMAL" to 10,
						"MODERATE" to 2
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Sirius",
					rolls = 5,
					tiers = mapOf(
						"WEAK" to 10,
						"NORMAL" to 10
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Ilios",
					rolls = 10,
					tiers = mapOf(
						"WEAK" to 10,
						"NORMAL" to 10,
						"MODERATE" to 2
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Horizon",
					rolls = 30,
					tiers = mapOf(
						"MODERATE" to 5,
						"STRONG" to 10,
						"EXTRA_STRONG" to 5,
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Trench",
					rolls = 30,
					tiers = mapOf(
						"MODERATE" to 5,
						"STRONG" to 10,
						"EXTRA_STRONG" to 5,
					)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "AU-0821",
					rolls = 30,
					tiers = mapOf(
						"MODERATE" to 5,
						"STRONG" to 10,
						"EXTRA_STRONG" to 10,
					)
				)
			)
		)
	}
}
