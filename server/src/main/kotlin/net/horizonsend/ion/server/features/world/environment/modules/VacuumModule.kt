package net.horizonsend.ion.server.features.world.environment.modules

import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.isInside
import net.horizonsend.ion.server.features.world.environment.isWearingSpaceSuit
import net.horizonsend.ion.server.features.world.environment.tickPressureFieldModule
import org.bukkit.GameMode
import org.bukkit.entity.Player

class VacuumModule(manager: WorldEnvironmentManager) : EnvironmentModule(manager) {
	var interval = 0

	override fun tickSync() {
		interval++
		if (interval % 10 != 0) return

		for (player in world.players) {
			if (player.gameMode != GameMode.SURVIVAL || player.isDead) return

			checkSuffocation(player)
		}
	}

	private fun checkSuffocation(player: Player) {

		if (isWearingSpaceSuit(player)) return

		if (isInside(player.eyeLocation, 1)) return

		if (!tickPressureFieldModule(player, 10)) return

		player.damage(0.5)
	}
}
