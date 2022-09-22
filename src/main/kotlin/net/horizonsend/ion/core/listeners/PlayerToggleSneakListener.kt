package net.horizonsend.ion.core.listeners

import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.misc.TractorBeamMultiblock
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.LegacyBlockUtils
import net.starlegacy.util.isStainedGlass
import net.starlegacy.util.isWallSign
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerToggleSneakEvent

object PlayerToggleSneakListener : SLEventListener() {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerToggleSneakEvent(event: PlayerToggleSneakEvent) {
		ActiveStarships.findByPilot(event.player) ?: return
		if (event.player.inventory.itemInMainHand.type != Material.CLOCK && !event.isSneaking) return

		val below = event.player.location.block.getRelative(BlockFace.DOWN)
		if (below.type == Material.GLASS || below.type.isStainedGlass) {
			for (face in LegacyBlockUtils.PIPE_DIRECTIONS) {
				val sign = below.getRelative(face, 2)
				if (!sign.type.isWallSign) continue

				if (Multiblocks[sign.getState(false) as Sign] !is TractorBeamMultiblock) continue
				var distance = 1
				val maxDistance = below.y - 1

				while (distance < maxDistance) {
					val relative = below.getRelative(BlockFace.DOWN, distance)

					if (relative.type != Material.AIR) {
						break
					}

					distance++
				}

				if (distance < 3) return

				val relative = below.getRelative(BlockFace.DOWN, distance)
				if (relative.type != Material.AIR) {
					event.player.teleport(relative.location.add(0.5, 1.5, 0.5))
				}
			}
		}
	}
}