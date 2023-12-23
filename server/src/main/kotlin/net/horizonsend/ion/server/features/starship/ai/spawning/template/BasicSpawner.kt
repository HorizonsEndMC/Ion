package net.horizonsend.ion.server.features.starship.ai.spawning.template

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.templateMiniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.AISpawner
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.util.function.Supplier

abstract class BasicSpawner(
	identifier: String,
	configurationSupplier: Supplier<AIShipConfiguration.AISpawnerConfiguration>
) : AISpawner(identifier, configurationSupplier) {
	// No specific conditions
	override fun spawningConditionsMet(world: World, x: Int, y: Int, z: Int): Boolean = true

	protected abstract fun findSpawnLocation(): Location?

	/** 0: x, 1: y, 2: z, 3: world name, */
	protected abstract val spawnMessage: Component?

	override suspend fun triggerSpawn() {
		val loc = findSpawnLocation() ?: return
		val (x, y, z) = Vec3i(loc)

		if (!spawningConditionsMet(loc.world, x, y, z)) return

		spawnMessage?.let {
			Notify.online(template(
				message = it,
				paramColor = HEColorScheme.HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				x,
				y,
				z,
				loc.world.name
			))
		}

		val ships = getStarshipTemplates(loc.world)

		for ((template, pilotName) in ships) {
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
}
