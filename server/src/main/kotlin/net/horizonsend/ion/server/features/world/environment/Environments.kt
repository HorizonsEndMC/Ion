package net.horizonsend.ion.server.features.world.environment

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent

object Environments : IonServerComponent() {
	override fun onEnable() {
		Tasks.syncRepeat(10, 10, ::tickSyncEnironments)
		Tasks.asyncRepeat(10, 10, ::tickAsyncEnironments)
	}

	private fun tickSyncEnironments() {
		for (world in IonWorld.all()) {
			world.enviornmentManager.tickSync()
		}
	}

	private fun tickAsyncEnironments() {
		for (world in IonWorld.all()) {
			world.enviornmentManager.tickAsync()
		}
	}

	@EventHandler
	fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
		val oldModules = event.from.ion.enviornmentManager.modules
		oldModules.forEach { it.removeEffects(event.player) }
	}
}
