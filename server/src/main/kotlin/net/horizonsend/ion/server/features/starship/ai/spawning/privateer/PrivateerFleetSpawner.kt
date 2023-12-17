package net.horizonsend.ion.server.features.starship.ai.spawning.privateer

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class PrivateerFleetSpawner : BasicSpawner(
	"PRIVATEER_FLEET",
	IonServer.aiShipConfiguration.spawners::privateerFleet,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration() // TODO
	}
}
