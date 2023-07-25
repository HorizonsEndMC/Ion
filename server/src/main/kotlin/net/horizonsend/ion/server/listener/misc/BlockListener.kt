package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.misc.CustomItems
import net.horizonsend.ion.server.features.multiblock.misc.MobDefender
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.stripColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.world.StructureGrowEvent

object BlockListener : SLEventListener() {
	// Disable block physics for portals to prevent airlocks from breaking
	@EventHandler
	fun onBlockPhysicsEvent(event: BlockPhysicsEvent) {
		if (event.block.type != Material.END_PORTAL) return
		event.isCancelled = true
	}

	// Don't allow breaking blocks with custom items
	@EventHandler
	fun onBlockBreakEventA(event: BlockBreakEvent) {
		val item = event.player.inventory.itemInMainHand
		val customItem = CustomItems[item]
		if (customItem != null && !customItem.id.startsWith("power_tool_")) {
			event.isCancelled = true
		}
	}

	// Prevent huge mushroom trees from growing as large mushroom blocks are used as custom ores
	@EventHandler
	fun onStructureGrowEvent(event: StructureGrowEvent) {
		if (!event.blocks.any { it.type == Material.BROWN_MUSHROOM_BLOCK }) return
		event.isCancelled = true
	}

	// Attempt to remove mob defenders at the location of blocks broken
	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreakEvent(event: BlockBreakEvent) {
		if (event.isCancelled) return
		MobDefender.removeDefender(event.block.location)
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onSignChange(event: SignChangeEvent) {
		for (i in 0 until 4) {
			event.setLine(i, event.getLine(i)?.stripColor())
		}
	}
}
