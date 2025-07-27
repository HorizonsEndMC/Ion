package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import java.util.function.Supplier

class AIConvoyTemplate<C : ConvoyContext>(
	val identifier: String,
	val spawnMechanicBuilder: (context : C) -> SpawnerMechanic,
	val difficultySupplier: (String) -> Supplier<Int>,
)
