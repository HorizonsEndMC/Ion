package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.Location
import java.util.function.Supplier

class AIConvoyTemplate(
	val identifier: String,
	val spawnMechanicBuilder: (source : TradeCityData) -> SpawnerMechanic,
	val routeProvider: Supplier<ConvoyRoute>,
	val difficultySupplier: (String) -> Supplier<Int>,
	val postSpawnBehavior: (AIController) -> Unit = {}
)

class ConvoyBuilder(private val identifier: String) {
	private lateinit var _routeProvider: Supplier<ConvoyRoute>
	private lateinit var _difficultySupplier: (String) -> Supplier<Int>
	private var postSpawnBehavior: (AIController) -> Unit = {}
	private lateinit var spawnMechanicBuilder: (source : TradeCityData) -> SpawnerMechanic

	val routeProvider: Supplier<ConvoyRoute> get() = _routeProvider
	val difficultySupplier: (String) -> Supplier<Int> get() = _difficultySupplier

	fun routeProvider(block: () -> ConvoyRoute) {
		_routeProvider = Supplier(block)
	}

	fun difficultySupplier(block: (String) -> Int) {
		_difficultySupplier = { worldName -> Supplier { block(worldName) } }
	}

	fun behavior(block: (AIController) -> Unit) {
		postSpawnBehavior = block
	}

	fun spawnMechanic(block: () -> SpawnerMechanic) {
		spawnMechanicBuilder = { _: TradeCityData, -> block() }
	}

	// New DSL
	fun spawnMechanicWithCity(block: (TradeCityData) -> SpawnerMechanic) {
		spawnMechanicBuilder = block
	}

	fun build(): AIConvoyTemplate {
		return AIConvoyTemplate(
			identifier = identifier,
			routeProvider = routeProvider,
			difficultySupplier = difficultySupplier,
			postSpawnBehavior = postSpawnBehavior,
			spawnMechanicBuilder = spawnMechanicBuilder
		)
	}
}

fun registerConvoy(id: String, block: ConvoyBuilder.() -> Unit): AIConvoyTemplate {
	return AIConvoyRegistry.register(ConvoyBuilder(id).apply(block).build())
}
