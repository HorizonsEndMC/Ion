package net.horizonsend.ion.server.features.ai.faction

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.CompositeSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import org.bukkit.Location

object AIConvoyRegistry {
	private val templates = mutableMapOf<String, AIConvoyTemplate>()

	val TC_CARAVAN = registerConvoy("trade_caravan") {
		locationProvider { Location() }
		difficultySupplier { world -> getTradeRouteDifficulty(world) }

		spawnMechanic {
			CompositeSpawner(
				locationProvider = locationProvider,
				groupMessage = text("<gray>A trade caravan arrives."),
				individualSpawnMessage = SpawnMessage("A ship appears."),
				difficultySupplier = difficultySupplier,
				components = listOf(
					SingleSpawn(...
				),
				BagSpawner(...
			)
			)
			)
		}

		behavior { controller ->
			controller.addUtilModule(PassiveTradeRouteModule(controller))
		}
	}

	fun register(template: AIConvoyTemplate): AIConvoyTemplate {
		require(!templates.containsKey(template.identifier)) {
			"Convoy template already registered: ${template.identifier}"
		}
		templates[template.identifier] = template
		return template
	}

	fun get(identifier: String): AIConvoyTemplate? = templates[identifier]

	fun all(): Collection<AIConvoyTemplate> = templates.values
}
