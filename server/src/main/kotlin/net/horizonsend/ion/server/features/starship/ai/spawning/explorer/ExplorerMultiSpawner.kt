package net.horizonsend.ion.server.features.starship.ai.spawning.explorer

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class ExplorerMultiSpawner : BasicSpawner(
	"EXPLORER_CONVOY",
	IonServer.aiSpawningConfiguration.spawners::tsaiiMulti,
) {
	override fun findSpawnLocation(): Location? = findExplorerSpawnLocation(configuration)

	override val spawnMessage: Component? = null

	companion object {
		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration() // TODO
	}
}
