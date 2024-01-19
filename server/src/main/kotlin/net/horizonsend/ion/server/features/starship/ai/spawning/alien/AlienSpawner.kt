package net.horizonsend.ion.server.features.starship.ai.spawning.alien

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location

class AlienSpawner : BasicSpawner(
	"ALIEN",
	IonServer.aiSpawningConfiguration.spawners::alien
) {
	override val spawnMessage: Component = ofChildren(
		text("[", HEColorScheme.HE_LIGHT_GRAY),
		text("{3} System Alert", ALIEN_ACCENT),
		text("]", HEColorScheme.HE_LIGHT_GRAY),
		text(" An unknown starship signature is being broadcast, proceed with extreme caution.", ALIEN_STANDARD)
	)

	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<$ALIEN_ACCENT>An unknown starship signature is being broadcast in {1} at {3}, {4}",
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 7,
			minDistanceFromPlayer = 2500.0,
			maxDistanceFromPlayer = 4500.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "STANDARD",
					nameList = mapOf(
						"<$ALIEN_ACCENT><obfuscated>飞行员" to 5,
					),
					ships = mapOf(
						verdolithReinforced.identifier to 2,
						mianbao.identifier to 2,
						malingshu.identifier to 2
					)
				)
			),
			worldSettings = listOf(
				AISpawningConfiguration.AIWorldSettings(
					world = "Trench",
					rolls = 2,
					tiers = mapOf("STANDARD" to 2)
				),
				AISpawningConfiguration.AIWorldSettings(
					world = "AU-0821",
					rolls = 2,
					tiers = mapOf("STANDARD" to 2)
				)
			)
		)
	}
}
