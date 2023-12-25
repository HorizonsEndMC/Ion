package net.horizonsend.ion.server.features.starship.ai.spawning.template

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.templateMiniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.ai.spawning.AISpawner
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.World
import java.util.function.Supplier

abstract class BasicSpawner(
	identifier: String,
	configurationSupplier: Supplier<AISpawningConfiguration.AISpawnerConfiguration>
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

	/** Selects a starship template off of the configuration, picks, and serializes a name */
	open fun getStarshipTemplate(world: World): Pair<AISpawningConfiguration.AIStarshipTemplate, Component> {
		// If the value is null, it is trying to spawn a ship in a world that it is not configured for.
		val worldConfig = configuration.getWorld(world)!!
		val tierIdentifier = worldConfig.tierWeightedRandomList.random()
		val tier = configuration.getTier(tierIdentifier)
		val shipIdentifier = tier.shipsWeightedList.random()
		val name = MiniMessage.miniMessage().deserialize(tier.namesWeightedList.random())

		return IonServer.aiSpawningConfiguration.getShipTemplate(shipIdentifier) to name
	}
}
