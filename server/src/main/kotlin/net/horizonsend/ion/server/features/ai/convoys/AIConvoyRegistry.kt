package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.common.utils.text.colors.WATCHER_ACCENT
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.configuration.util.VariableIntegerAmount
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PERSEUS_EXPLORERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.SYSTEM_DEFENSE_FORCES
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.CaravanModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner.Companion.asBagSpawned
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.CompositeSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.RandomShipSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.AMPH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.BULWARK
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.CONTRACTOR
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DAGGER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DESSLE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MINHAUL_CHETHERITE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MINHAUL_REDSTONE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MINHAUL_TITANIUM
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.NIMBLE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.PATROLLER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.STRIKER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TENETA
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VETERAN
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.WAYFINDER
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import org.bukkit.Location
import java.util.function.Supplier

object AIConvoyRegistry {
	private val templates = mutableMapOf<String, AIConvoyTemplate>()

	val SMALL_TC_CARAVAN = registerConvoy("SMALL_TC_CARAVAN") {
		routeProvider { TraceCityCaravanRoute() }
		difficultySupplier {2}

		spawnMechanicWithCity  { city ->
			val candidates = city.allowedDestinations
			val routeCities = if (candidates.isNullOrEmpty()) {
				TradeCities.getAll()
			} else {
				candidates
			}

			val route = TraceCityCaravanRoute(
				cites = routeCities.shuffled().toMutableList(),
				source = city
			)

			val mechanic = CompositeSpawner(
				locationProvider = {routeProvider.get().getSourceLocation()},
				difficultySupplier = difficultySupplier,
				groupMessage = "Small convoy fleet!".miniMessage(),
				individualSpawnMessage = SpawnMessage.WorldMessage("Ship joined the convoy!".miniMessage()),
				onPostSpawn = { controller ->
					controller.metadata["convoy_route"] = route
				},
				components = listOf(
					SingleSpawn(
						RandomShipSupplier(
							PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_CHETHERITE),
							PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_REDSTONE),
							PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_TITANIUM),
							PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.AMPH)
						),
						{routeProvider.get().getSourceLocation()},
						SpawnMessage.WorldMessage("Flag trade ship joined the convoy!".miniMessage()),
						difficultySupplier
					),
					BagSpawner(
						formatLocationSupplier(routeProvider.get().getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
						VariableIntegerAmount(5, 15),
						"<$PRIVATEER_LIGHT_TEAL>Privateer <$HE_MEDIUM_GRAY>Auxiliary caravan ships in {3}, at {0} {2}".miniMessage(),
						null,
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(WAYFINDER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(STRIKER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(NIMBLE).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(DESSLE).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(MINHAUL_CHETHERITE).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(MINHAUL_REDSTONE).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(MINHAUL_TITANIUM).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 3),
						asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(AMPH).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 5),
						difficultySupplier = difficultySupplier
					),
				BagSpawner(
					formatLocationSupplier(routeProvider.get().getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
					VariableIntegerAmount(5, 15),
					"<$PRIVATEER_LIGHT_TEAL>Privateer <$HE_MEDIUM_GRAY>Escorting defense force spotted in {3}, at {0} {2}".miniMessage(),
					null,
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 3),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 5),
					difficultySupplier = difficultySupplier
				),
			),
			)

			mechanic
		}

		behavior { controller ->
			val route = controller.metadata["convoy_route"] as? ConvoyRoute ?: return@behavior
			controller.addUtilModule(CaravanModule(
				controller,
				controller.getUtilModule(AIFleetManageModule::class.java)!!.fleet,
				get("SMALL_TC_CARAVAN")!!,
				route.getSourceLocation(),
				route))
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
