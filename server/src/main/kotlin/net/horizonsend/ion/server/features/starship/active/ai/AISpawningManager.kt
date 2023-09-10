package net.horizonsend.ion.server.features.starship.active.ai

import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

object AISpawningManager : IonServerComponent(true) {
	val AIEncounterDelay = 15 * 60 * 20 //TODO switch this back before push

	val spawners = listOf<AISpawner>(
		VestaSpawner()
	)

	@OptIn(ExperimentalCoroutinesApi::class)
	override fun onEnable() {
		Tasks.asyncRepeat(20 * 15, 20 * 15) {
			val spawner = spawners.shuffled().firstOrNull() ?: return@asyncRepeat

			val loc = spawner.findLocation(Bukkit.getWorld("world")!!)

			val deferred = spawner.spawn(loc)

			deferred.invokeOnCompletion { throwable ->
				val ship = deferred.getCompleted()

				AIManager.activeShips.add(ship)

				throwable?.let { IonServer.server.debug("AI Starship at could not be spawned: ${throwable.message}!") }
				Notify.online(Component.text("Spawning AI ship at ${Vec3i(loc)}"))
			}
		}
	}
}
