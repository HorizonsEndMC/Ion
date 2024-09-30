package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.spawning.SpawnerScheduler
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import org.bukkit.World

class SingleWorldSpawner(
	identifier: String,
	val world: World,
	override val scheduler: SpawnerScheduler,
	mechanic: SpawnerMechanic
) : AISpawner("${identifier}_${world.name.uppercase()}", mechanic) {
	init {
		scheduler.setSpawner(this)
	}
}

