package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.isBed
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object InteractListener : SLEventListener() {
	// Put power into the sign if right clicking with a battery

	/*
	// When not in creative mode, make breaking a custom item drop the proper drops
	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreakEvent(event: BlockBreakEvent) {
		if (event.isCancelled) return
		if (event.player.gameMode == GameMode.CREATIVE) return

		val block = event.block
		val customBlock = CustomBlocks[block] ?: return

		if (event.isDropItems) {
			event.isDropItems = false
			block.type = Material.AIR
		}

		val itemUsed = event.player.inventory.itemInMainHand
		val location = block.location.toCenterLocation()
		Tasks.sync {
			for (drop in customBlock.getDrops(itemUsed)) {
				block.world.dropItem(location, drop)
			}
		}
	}
	 */

	// Disable beds
	@EventHandler
	fun onPlayerInteractEventH(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		val item = event.clickedBlock!!
		val player = event.player

		if (item.type.isBed) {
			event.isCancelled = true
			player.successActionMessage("Beds are disabled on this server! Use a cryopod instead")
		}
	}
}
