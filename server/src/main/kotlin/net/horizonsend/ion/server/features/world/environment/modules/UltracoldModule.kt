package net.horizonsend.ion.server.features.world.environment.modules

import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.isInside
import net.horizonsend.ion.server.features.world.environment.tickEnvironmentModule
import org.bukkit.GameMode

class UltracoldModule(manager: WorldEnvironmentManager) : EnvironmentModule(manager) {
	override fun tickSync() {
		for (player in world.players) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			if (isInside(player.location, 1)) continue

			if (!tickEnvironmentModule(player, 10)) {
				player.freezeTicks = minOf(player.freezeTicks + 3, 200)
			}
		}
	}
}
