package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class MiningCorpMultiSpawner : BasicSpawner(
	"MINING_CORP_SPAWNER",
	IonServer.aiShipConfiguration.spawners::miningCorpMultiSpawner,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration) // TODO

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration() // TODO
	}
}
