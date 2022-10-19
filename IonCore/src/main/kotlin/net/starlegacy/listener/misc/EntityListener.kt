package net.starlegacy.listener.misc

import net.starlegacy.feature.multiblock.misc.MobDefender
import net.starlegacy.listener.SLEventListener
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
