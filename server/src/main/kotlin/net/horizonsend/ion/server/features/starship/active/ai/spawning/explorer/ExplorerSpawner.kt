package net.horizonsend.ion.server.features.starship.active.ai.spawning.explorer

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class ExplorerSpawner : BasicSpawner(
	"EXPLORATION_VESSEL",
	IonServer.aiShipConfiguration.spawners::explorationVessel,
) {
	override fun findSpawnLocation(): Location? = ExplorerUtils.findSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(

		) // TODO
	}
}
