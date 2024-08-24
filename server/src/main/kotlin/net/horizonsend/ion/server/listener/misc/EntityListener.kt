package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.Ghast
import org.bukkit.entity.Monster
import org.bukkit.entity.Slime
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntitySpawnEvent

object EntityListener : SLEventListener() {
	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
		if (event.entity !is Monster && event.entity !is Slime && event.entity !is Ghast) {
			return
		}

		if (!MobDefender.cancelSpawn(event.location)) {
			return
		}

		event.isCancelled = true
	}
}
