package net.horizonsend.ion.server.features.starship.active.ai.spawning.privateer

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class PrivateerFleetSpawner : BasicSpawner(
	"PRIVATEER_FLEET",
	IonServer.aiShipConfiguration.spawners::privateerFleet,
) {
	override fun findSpawnLocation(): Location? = PrivateerUtils.findLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration() // TODO
	}
}
