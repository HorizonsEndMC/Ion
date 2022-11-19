package net.starlegacy.feature.misc

import kotlin.math.max
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.starlegacy.SLComponent
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.misc.DecomposerMultiblock
import net.starlegacy.util.CHISELED_TYPES
import net.starlegacy.util.getFacing
import net.starlegacy.util.getRelativeIfLoaded
import net.starlegacy.util.rightFace
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object Decomposers : SLComponent() {
	val busySigns = mutableSetOf<Location>()

	private const val MAX_LENGTH = 100
	private const val BLOCKS_PER_SECOND = 1000
	private val FRAME_MATERIAL = CHISELED_TYPES

	@EventHandler
	fun onClick(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) {
			return
		}

		val sign = event.clickedBlock?.state as? Sign ?: return

		val multiblock = Multiblocks[sign] as? DecomposerMultiblock ?: return

		val signLoc = sign.location

		val forward = sign.getFacing().oppositeFace
		val up = BlockFace.UP
		val right = forward.rightFace

		val origin: Location = signLoc.clone()
			.add(forward.direction.multiply(2))
			.add(up.direction)
			.add(right.direction)

		val frameOrigin: Location = signLoc.clone().add(forward.direction)

		if (!busySigns.add(signLoc)) {
			event.player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Decomposer in use")
			return
		}

		val width = getDimension(frameOrigin, right)
		val height = getDimension(frameOrigin, up)
		val length = getDimension(frameOrigin, forward)
		val area = height * length
		val delay = max(10L, area / BLOCKS_PER_SECOND * 20L)

		DecomposeTask(
			signLoc,
			width,
			height,
			length,
			origin,
			right,
			up,
			forward,
			event.player.uniqueId,
			multiblock
		).runTaskTimer(Ion, delay, delay)
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
}