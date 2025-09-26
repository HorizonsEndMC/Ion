package net.horizonsend.ion.server.features.world.environment.modules

import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import org.bukkit.entity.Player

abstract class EnvironmentModule(val manager: WorldEnvironmentManager) {
	protected val world get() = manager.world.world

	open fun tickSync() {}
	open fun tickAsync() {}

	open fun removeEffects(player: Player) {}
}
