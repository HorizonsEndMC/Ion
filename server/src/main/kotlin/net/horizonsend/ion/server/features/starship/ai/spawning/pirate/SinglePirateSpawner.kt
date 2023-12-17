package net.horizonsend.ion.server.features.starship.ai.spawning.pirate

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class SinglePirateSpawner : BasicSpawner(
	"PIRATE_SINGLE",
	IonServer.aiShipConfiguration.spawners::singlePirate,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration() // TODO
	}
}
