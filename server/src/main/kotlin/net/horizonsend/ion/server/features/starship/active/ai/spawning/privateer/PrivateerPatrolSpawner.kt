package net.horizonsend.ion.server.features.starship.active.ai.spawning.privateer

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.templateMiniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.World

class PrivateerPatrolSpawner : PrivateerSpawner(
	"PRIVATEER_PATROL",
	IonServer.aiShipConfiguration.spawners::PRIVATEER_PATROL,
) {
	override fun spawningConditionsMet(world: World, x: Int, y: Int, z: Int): Boolean {
		return true
	}

	override suspend fun triggerSpawn() {
		val loc = findLocation() ?: return
		val (x, y, z) = Vec3i(loc)

		if (!spawningConditionsMet(loc.world, x, y, z)) return

		val (template, pilotName) = getStarshipTemplate(loc.world)

		val deferred = spawnAIStarship(template, loc, createController(template, pilotName))

		deferred.invokeOnCompletion {
			IonServer.server.sendMessage(templateMiniMessage(
				message = configuration.miniMessageSpawnMessage,
				paramColor = HEColorScheme.HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				template.getName(),
				x,
				y,
				z,
				loc.world.name
			))
		}
	}
}
