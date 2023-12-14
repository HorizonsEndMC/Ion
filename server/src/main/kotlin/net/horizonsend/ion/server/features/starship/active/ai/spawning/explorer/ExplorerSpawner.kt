package net.horizonsend.ion.server.features.starship.active.ai.spawning.explorer

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ai.spawning.template.BasicSpawner
import org.bukkit.Location

object ExplorerSpawner : BasicSpawner(
	"EXPLORATION_VESSEL",
	IonServer.aiShipConfiguration.spawners::explorationVessel,
) {
	override fun findSpawnLocation(): Location? = ExplorerUtils.findSpawnLocation()

	val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(

	)
}
