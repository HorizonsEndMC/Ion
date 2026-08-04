package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.Bat
import org.bukkit.entity.Enderman
import org.bukkit.entity.Ghast
import org.bukkit.entity.Monster
import org.bukkit.entity.Slime
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTeleportEvent

object EntityListener : SLEventListener() {
	@EventHandler
	fun onEntitySpawn(event: EntitySpawnEvent) {
		if (event.entity !is Monster && event.entity !is Slime && event.entity !is Ghast && event.entity !is Bat) {
			return
		}

		if (!MobDefender.cancelSpawn(event.location)) {
			return
		}

		event.isCancelled = true
	}

	@EventHandler
	fun onEndermanTeleport(event: EntityTeleportEvent) {
		if (event.entity !is Enderman) return

		val newLoc = event.to ?: return

		if (!MobDefender.cancelSpawn(newLoc)) return

		event.isCancelled = true
	}

	@EventHandler
	fun onEndermanGrief(event: EntityChangeBlockEvent) {
		if (event.entity !is Enderman) return

		val newLoc = event.block.location

		if (!MobDefender.cancelSpawn(newLoc)) return

		event.isCancelled = true
	}
}
