package net.horizonsend.ion.server.features.starship.ai.spawning.explorer

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class ExplorerSingleSpawner : BasicSpawner(
	"EXPLORATION_VESSEL",
	IonServer.aiShipConfiguration.spawners::explorationVessel,
) {
	override fun findSpawnLocation(): Location? = findExplorerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(

		) // TODO
	}
}
