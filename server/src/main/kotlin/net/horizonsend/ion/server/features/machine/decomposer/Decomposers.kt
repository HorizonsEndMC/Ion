package net.horizonsend.ion.server.features.machine.decomposer

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.type.misc.DecomposerMultiblock
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.miscellaneous.utils.CHISELED_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockSateSafe
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.max

object Decomposers : IonServerComponent() {
	val busySigns = mutableMapOf<Location, DecomposeTask>()

	private const val MAX_LENGTH = 100
	private const val BLOCKS_PER_SECOND = 1000
	private val FRAME_MATERIAL = CHISELED_TYPES

	@EventHandler
	fun onClick(event: PlayerInteractEvent) {
		val sign = event.clickedBlock?.state as? Sign ?: return
		val multiblock = Multiblocks[sign] as? DecomposerMultiblock ?: return
		val signLoc = sign.location

		if (event.action == Action.RIGHT_CLICK_BLOCK) {
			val forward = sign.getFacing().oppositeFace
			val up = BlockFace.UP
			val right = forward.rightFace

			val origin: Location = signLoc.clone()
				.add(forward.direction.multiply(2))
				.add(up.direction)
				.add(right.direction)

			val frameOrigin: Location = signLoc.clone().add(forward.direction)

			val width = getDimension(frameOrigin, right)
			val height = getDimension(frameOrigin, up)
			val length = getDimension(frameOrigin, forward)

			val area = height * length
			val delay = max(10L, area / BLOCKS_PER_SECOND * 20L)

			val offset = calculateOffset(origin, width, height, length, right, up, forward)

			if (length == 0 || width == 0 || height == 0) return event.player.userError("Decomposer has zero volume! Make sure the chiseled blocks extend to the right of the multiblock.")

			if (CombatTimer.isPvpCombatTagged(event.player)) {
				event.player.userError("Cannot enable decomposers while in combat")
				return
			}

//			if (offset > width) return event.player.userError("Decomposer empty!")

			val task = DecomposeTask(
				signLoc,
				width,
				offset,
				height,
				length,
				origin,
				right,
				up,
				forward,
				event.player.uniqueId,
				multiblock
			)

			if (busySigns.containsKey(signLoc)) {
				event.player.userError("Decomposer in use")
				return
			}

			task.runTaskTimer(IonServer, delay, delay)

			event.player.success("Started Decomposer")

			busySigns[signLoc] = task
		} else {
			(busySigns[signLoc] ?: return event.player.information("Decomposer not in use")).cancel()

			event.player.information("Cancelled running decomposer")
		}
	}

	private fun getDimension(origin: Location, direction: BlockFace): Int {
		var dimension = 0
		var tempBlock = origin.block

		while (dimension < MAX_LENGTH) {
			tempBlock = tempBlock.getRelativeIfLoaded(direction)
				?: return dimension

			if (!FRAME_MATERIAL.contains(tempBlock.type)) {
				return dimension
			}

			dimension++
		}

		return dimension
	}

	private fun calculateOffset(
		origin: Location,
		width: Int,
		height: Int,
		length: Int,
		right: BlockFace,
		up: BlockFace,
		forward: BlockFace
	): Int {
		val world = origin.world

		for (offsetWidth: Int in 0 until height) {
			for (offsetForward: Int in 0 until length) {
				for (offsetUp: Int in 0 until width) {
					val loc = origin.clone()

					loc.add(up.direction.multiply(offsetUp))
					loc.add(forward.direction.multiply(offsetForward))
					loc.add(right.direction.multiply(offsetWidth))

					val block = getNMSBlockSateSafe(world, Vec3i(loc))
					if (block?.isAir == false) return offsetWidth
				}
			}
		}

		return width + 1
	}
}
