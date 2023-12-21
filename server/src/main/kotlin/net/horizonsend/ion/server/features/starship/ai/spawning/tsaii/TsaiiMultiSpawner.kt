package net.horizonsend.ion.server.features.starship.ai.spawning.tsaii

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class TsaiiMultiSpawner : BasicSpawner(
	"TSAII_FLEET",
	IonServer.aiShipConfiguration.spawners::tsaiiRaid,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = ""
		) // TODO
	}
}
