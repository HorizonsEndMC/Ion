package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.common.utils.text.colors.ABYSSAL_DESATURATED_RED
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.Player
import java.util.UUID

object Halloweeeeeen : IonServerComponent() {
	val spooked = mutableSetOf<UUID>()

	override fun onEnable() {
		Tasks.asyncRepeat(1200L, 1200L, ::checkPlayers)
	}

	private fun checkPlayers() {
		for (player in Bukkit.getOnlinePlayers()) checkPlayer(player)
	}

	private fun checkPlayer(player: Player) {
		if (spooked.contains(player.uniqueId)) return
		if (testRandom(0.9)) return

		Tasks.sync {
			val rayCast = player.world.rayTrace(player.eyeLocation, player.location.direction, 200.0, FluidCollisionMode.ALWAYS, false, 0.1) { false } ?: return@sync
			if (rayCast.hitBlock == null) return@sync

			player.sendMessage(Component.text("The abyss stares back...", ABYSSAL_DESATURATED_RED))
			spooked.add(player.uniqueId)
		}
	}
}
