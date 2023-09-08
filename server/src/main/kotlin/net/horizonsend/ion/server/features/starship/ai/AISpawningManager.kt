package net.horizonsend.ion.server.features.starship.ai

import net.horizonsend.ion.server.IonServerComponent
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

	override fun onEnable() {
		println("ENABLING AI SHIP SPAWNING")

		Tasks.asyncRepeat(20 * 15, 20 * 15) {
			println("running tasks")
			val spawner = spawners.shuffled().firstOrNull() ?: return@asyncRepeat

			val loc = spawner.findLocation(Bukkit.getWorld("world")!!)

			AIManager.activeShips.add(spawner.spawn(loc))
			Notify.online(Component.text("Spawning AI ship at ${Vec3i(loc)}"))
		}
	}
}
