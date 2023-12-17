package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class StandardTransportOperation  : BasicSpawner(
	"MINING_CORP_TRANSPORT",
	IonServer.aiShipConfiguration.spawners::miningCorpTransport,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration() // TODO
	}
}
