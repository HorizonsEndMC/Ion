package net.horizonsend.ion.server.features.starship.ai.spawning.tsaii

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.kyori.adventure.text.Component
import org.bukkit.Location

class TsaiiMultiSpawner : BasicSpawner(
	"TSAII_FLEET",
	IonServer.aiSpawningConfiguration.spawners::tsaiiMulti,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	override val spawnMessage: Component? = null

	companion object {
		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = ""
		) // TODO
	}
}
