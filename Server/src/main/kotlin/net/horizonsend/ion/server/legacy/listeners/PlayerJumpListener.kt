package net.horizonsend.ion.server.legacy.listeners

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.misc.TractorBeamMultiblock
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.LegacyBlockUtils
import net.starlegacy.util.isStainedGlass
import net.starlegacy.util.isWallSign
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

object PlayerJumpListener : SLEventListener() {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerJumpEvent(event: PlayerJumpEvent) {
		if (event.player.inventory.itemInMainHand.type != Material.CLOCK) return

		val original = event.player.location.block
		for (i in event.player.world.minHeight..(event.player.world.maxHeight - original.y)) {
			val block = original.getRelative(BlockFace.UP, i)
			if (block.type == Material.AIR) continue

			if (block.type == Material.GLASS || block.type.isStainedGlass) {
				for (face in LegacyBlockUtils.PIPE_DIRECTIONS) {
					val sign = block.getRelative(face, 2)
					if (!sign.type.isWallSign) continue

					if (Multiblocks[sign.getState(false) as Sign] !is TractorBeamMultiblock) continue
					event.player.teleport(block.location.add(0.5, 1.5, 0.5))
				}
				continue
			}
			return
		}
	}

}