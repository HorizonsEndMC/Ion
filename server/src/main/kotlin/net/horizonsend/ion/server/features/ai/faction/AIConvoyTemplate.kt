package net.horizonsend.ion.server.features.ai.faction

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.Location
import java.util.function.Supplier

class AIConvoyTemplate(
	val identifier: String,
	val spawnMechanicBuilder: () -> SpawnerMechanic,
	val locationProvider: Supplier<Location?>,
	val difficultySupplier: (String) -> Supplier<Int>,
	val postSpawnBehavior: (AIController) -> Unit = {}
)

class ConvoyBuilder(private val identifier: String) {
	private lateinit var _locationProvider: Supplier<Location?>
	private lateinit var _difficultySupplier: (String) -> Supplier<Int>
	private var postSpawnBehavior: (AIController) -> Unit = {}
	private lateinit var spawnMechanicBuilder: () -> SpawnerMechanic

	val locationProvider: Supplier<Location?> get() = _locationProvider
	val difficultySupplier: (String) -> Supplier<Int> get() = _difficultySupplier

	fun locationProvider(block: () -> Location?) {
		_locationProvider = Supplier(block)
	}

	fun difficultySupplier(block: (String) -> Int) {
		_difficultySupplier = { worldName -> Supplier { block(worldName) } }
	}

	fun behavior(block: (AIController) -> Unit) {
		postSpawnBehavior = block
	}

	fun spawnMechanic(block: () -> SpawnerMechanic) {
		spawnMechanicBuilder = block
	}

	fun build(): AIConvoyTemplate {
		return AIConvoyTemplate(
			identifier = identifier,
			locationProvider = locationProvider,
			difficultySupplier = difficultySupplier,
			postSpawnBehavior = postSpawnBehavior,
			spawnMechanicBuilder = spawnMechanicBuilder
		)
	}
}

fun registerConvoy(id: String, block: ConvoyBuilder.() -> Unit): AIConvoyTemplate {
	return AIConvoyRegistry.register(ConvoyBuilder(id).apply(block).build())
}
