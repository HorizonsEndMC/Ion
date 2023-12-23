package net.horizonsend.ion.server.features.starship.ai.spawning.pirate

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class PirateMultiSpawner : BasicSpawner(
	"PIRATE_FLEET",
	IonServer.aiShipConfiguration.spawners::pirateFleet,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	override val spawnMessage: Component? = null

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration() // TODO
	}
}
