package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackActionMessage
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import org.bukkit.entity.EnderCrystal
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPlaceEvent

@Suppress("Unused")
class EntityListener: Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	fun onEntityPlace(event: EntityPlaceEvent) {
		if (event.entity is EnderCrystal) {
			event.isCancelled = true
			event.player?.sendFeedbackActionMessage(FeedbackType.INFORMATION, "End Crystals are disabled on this server!")
		}
	}
}