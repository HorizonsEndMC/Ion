package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.Location
import java.util.function.Supplier

class AIConvoyTemplate(
	val identifier: String,
	val spawnMechanicBuilder: (source : TradeCityData) -> SpawnerMechanic,
	val difficultySupplier: (String) -> Supplier<Int>,
)
