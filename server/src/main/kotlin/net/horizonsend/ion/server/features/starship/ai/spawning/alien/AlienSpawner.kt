package net.horizonsend.ion.server.features.starship.ai.spawning.alien

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class AlienSpawner : BasicSpawner(
	"ALIEN",
	IonServer.aiSpawningConfiguration.spawners::alienSpawner
) {
	override val spawnMessage: Component? = null

	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration()
	}
}
