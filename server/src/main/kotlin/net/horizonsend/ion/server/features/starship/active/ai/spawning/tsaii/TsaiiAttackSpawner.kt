package net.horizonsend.ion.server.features.starship.active.ai.spawning.tsaii

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ai.spawning.privateer.findPrivateerSpawnLocation
import net.horizonsend.ion.server.features.starship.active.ai.spawning.template.BasicSpawner
import org.bukkit.Location

class TsaiiAttackSpawner : BasicSpawner(
	"TSAII_ATTACK",
	IonServer.aiShipConfiguration.spawners::tsaiiAttack,
) {
	override fun findSpawnLocation(): Location? = findPrivateerSpawnLocation(configuration)

	companion object {
		val defaultConfiguration = AIShipConfiguration.AISpawnerConfiguration() // TODO
	}
}
