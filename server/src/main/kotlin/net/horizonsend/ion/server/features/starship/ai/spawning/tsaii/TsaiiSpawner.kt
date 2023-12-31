package net.horizonsend.ion.server.features.starship.ai.spawning.tsaii

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class TsaiiSpawner : BasicSpawner(
	"TSAII_ATTACK",
	IonServer.aiSpawningConfiguration.spawners::tsaiiSingle,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	override val spawnMessage: Component = ofChildren(
		Component.text("[", HEColorScheme.HE_LIGHT_GRAY),
		Component.text("{3} System Alert", TSAII_LIGHT_ORANGE),
		Component.text("]", HEColorScheme.HE_LIGHT_GRAY),
		Component.text(" Tsaii Raider activity detected!", TSAII_MEDIUM_ORANGE)
	)

	companion object {
		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<${TSAII_DARK_ORANGE}>Dangerous Tsaii Raiders {0} has been reported in the area of {1}, {3}, in {4}. <$TSAII_MEDIUM_ORANGE>Please avoid the sector until the threat has been cleared!",
			minDistanceFromPlayer = 2500.0,
			maxDistanceFromPlayer = 4500.0,
			pointThreshold = 30 * 20 * 60,
			pointChance = 0.25,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "STANDARD",
					nameList = mapOf(
						"<$TSAII_DARK_ORANGE>Tsaii Raider" to 2,
						"<$TSAII_DARK_ORANGE>Tsaii Champion" to 2,
						"<$TSAII_DARK_ORANGE>Tsaii Warlord" to 2
					),
					ships = mapOf(
						raider.identifier to 5,
						scythe.identifier to 5,
						swarmer.identifier to 5,
					)
				)
			),
			worldSettings = listOf(
				AISpawningConfiguration.AIWorldSettings(
					world = "Horizon",
					rolls = 20,
					tiers = mapOf("STANDARD" to 2,)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "Trench",
					rolls = 15,
					tiers = mapOf("STANDARD" to 2,)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "AU-0821",
					rolls = 15,
					tiers = mapOf("STANDARD" to 2,)
				)
			)
		)
	}
}
