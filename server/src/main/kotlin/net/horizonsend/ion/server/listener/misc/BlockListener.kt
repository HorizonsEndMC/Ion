package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.multiblock.type.misc.MobDefender
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.stripColor
import org.bukkit.Material
import org.bukkit.block.data.MultipleFacing
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
		if (event.block.type == Material.END_PORTAL) {
			event.isCancelled = true
			return
		} else if(event.block.type.isGlassPane) {
			for(face in CARDINAL_BLOCK_FACES) {
				val relative = event.block.getRelativeIfLoaded(face) ?: continue
				if(relative.type != Material.GRINDSTONE) continue

				val data = event.block.blockData as MultipleFacing
				data.setFace(face, true)
				event.block.blockData = data
			}
			return
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
