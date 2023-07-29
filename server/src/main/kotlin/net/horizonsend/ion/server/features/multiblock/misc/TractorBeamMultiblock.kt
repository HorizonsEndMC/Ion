package net.horizonsend.ion.server.features.multiblock.misc

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.LegacyBlockUtils
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlass
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.event.player.PlayerToggleSneakEvent

object TractorBeamMultiblock : Multiblock(), InteractableMultiblock, Listener {
	override val name = "tractorbeam"

	override val signText = createSignText(
		line1 = "&7Tractor",
		line2 = "&7Beam",
		line3 = "[-?::]",
		line4 = "[:->:]"
	)

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).anySlab()
		at(-1, +0, +1).anySlab()
		at(+1, +0, +1).anySlab()
		at(+0, +0, +2).anySlab()

		at(+0, +0, +1).anyGlass()
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.item?.type != Material.CLOCK) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.clickedBlock?.type?.isWallSign != true) return

		tryDescend(player)
	}

	private fun tryDescend(player: Player) {
		val below = player.location.block.getRelative(BlockFace.DOWN)

		if (below.type != Material.GLASS && !below.type.isStainedGlass) return

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

		val block = below.getRelative(BlockFace.DOWN, distance)

		if (!checkMultiblock(block)) return

		player.teleport(
			block.location.add(0.5, 1.5, 0.5),
			TeleportCause.PLUGIN,
			*TeleportFlag.Relative.values()
		)
	}

	private fun tryAscend(player: Player) {
		val blockStandingIn = player.location.block

		for (i in player.world.minHeight..(player.world.maxHeight - blockStandingIn.y)) {
			val block = blockStandingIn.getRelative(BlockFace.UP, i)
			if (block.type == Material.AIR) continue

			if (!block.type.isGlass) continue

			if (!checkMultiblock(block)) continue

			player.teleport(
				block.location.add(0.5, 1.5, 0.5),
				TeleportCause.PLUGIN,
				*TeleportFlag.Relative.values()
			)
		}
	}

	/** From the glass block, check if it is part of a valid tractor beam **/
	private fun checkMultiblock(block: Block): Boolean {
		for (face in LegacyBlockUtils.PIPE_DIRECTIONS) {
			val sign = block.getRelative(face, 2)
			if (!sign.type.isWallSign) continue

			if (Multiblocks[sign.getState(false) as Sign] !is TractorBeamMultiblock) continue

			return true
		}

		return false
	}

	// Bring the player up if they right click while facing up with a clock
	// and there's a tractor beam above them
	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerInteractEventC(event: PlayerInteractEvent) {
		if (event.item?.type != Material.CLOCK) return
		if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.player.location.pitch > -60) return

		tryAscend(event.player)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerJumpEvent(event: PlayerJumpEvent) {
		if (event.player.inventory.itemInMainHand.type != Material.CLOCK) return

		tryAscend(event.player)
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerToggleSneakEvent(event: PlayerToggleSneakEvent) {
		if (!event.isSneaking) return
		if (ActiveStarships.findByPilot(event.player) != null) return
		if (event.player.inventory.itemInMainHand.type != Material.CLOCK) return

		tryDescend(event.player)
	}
}
