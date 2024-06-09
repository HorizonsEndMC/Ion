package net.horizonsend.ion.server.listener.gear

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.gear.TreeCutter
import net.horizonsend.ion.server.features.misc.getPower
import net.horizonsend.ion.server.features.misc.removePower
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

object PowerToolListener : SLEventListener() {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@Suppress("Unused")
	fun onInteract(event: PlayerInteractEvent) {
		if (event.action != Action.LEFT_CLICK_BLOCK || event.player.gameMode == GameMode.CREATIVE) {
			return
		}

		val item = event.item ?: return
		val customItem = CustomItems[item]
		if (customItem == null || !customItem.id.startsWith("power_tool_")) {
			return
		}
		val type = customItem.id.split("_")[2]
		val player = event.player
		val block = event.clickedBlock ?: return
		val blockType = block.type
		when (type) {
			"chainsaw" -> {
				val breakEvent = BlockBreakEvent(block, player)
				Bukkit.getPluginManager().callEvent(breakEvent)
				if (breakEvent.isCancelled) return

				event.isCancelled = true

				if (!TreeCutter.isApplicable(blockType)) {
					return
				}

				if (getPower(item) < 1000) {
					player.sendMessage(ChatColor.RED.toString() + "Out of power.")
					return
				}

				removePower(item, 1000)

				TreeCutter(event.player, block).runTaskAsynchronously(IonServer)
				return
			}

			else -> log.warn("Unhandled power tool $type")
		}
	}
}
