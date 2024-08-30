package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.audience.Audience
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.Orientable
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object ExpandableAirlock : Multiblock(), InteractableMultiblock {
	override val name: String = "airlock"

	override val signText = createSignText(
		line1 = "&7Airlock",
		line2 = null,
		line3 = "&bRayshielding",
		line4 = "&bSolutions, Inc."
	)

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).ironBlock()
	}

	override fun onTransformSign(player: Player, sign: Sign) = sign.getSide(Side.FRONT).line(1, AirlockMultiblock.OFF)

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val doorShape = getDimensions(sign) ?: return player.userError("Invalid frame! The sides must be double slabs and the corners must be iron blocks!")

		val interiorBlocks = getInteriorBlocks(sign, doorShape) ?: return player.userError("The interior went beyond loaded chunks!")

		val on = sign.getSide(Side.FRONT).line(1).plainText() == "-[ON]-"
		val isOn = matchesOn(blocks = interiorBlocks)

		if (on && isOn) return toggleOff(sign, interiorBlocks)

		val isOff = matchesOff(blocks = interiorBlocks)

		if (!on && isOff) return toggleOn(sign, interiorBlocks)

		if (!isOn && !isOff) return player.userError("The interior is not complete! Make sure every block is iron bars.")

		toggleOff(sign, interiorBlocks)
	}

	private fun toggleOn(sign: Sign, interiorBlocks: Collection<Block>) {
		setOn(sign, interiorBlocks)
		sign.getSide(Side.FRONT).line(1, AirlockMultiblock.ON)
		sign.update()
	}

	private fun toggleOff(sign: Sign, interiorBlocks: Collection<Block>) {
		setOff(sign, interiorBlocks)
		sign.getSide(Side.FRONT).line(1, AirlockMultiblock.OFF)
		sign.update()
	}

	private fun getDimensions(sign: Sign, detector: Audience? = null): ShapeResult? {
		var frameRight = 0
		var frameUp = 0
		var verticalOffset = 0

		val signFace = sign.getFacing()
		val rightFace = signFace.oppositeFace.rightFace

		// Block the sign is on
		val starterBlock = sign.block.getRelativeIfLoaded(signFace.oppositeFace) ?: return null

		var currentBlock = starterBlock.getRelativeIfLoaded(BlockFace.DOWN, 1)

		// Go down the left side to the bottom left corner
		while (currentBlock != null && matchesSide(currentBlock)) {
			verticalOffset++
			debugAudience.highlightBlock(Vec3i(currentBlock.location), 20L)
			currentBlock = currentBlock.getRelativeIfLoaded(BlockFace.DOWN, 1)
		}

		// If it is no longer a side, check and see if it is a corner
		if (currentBlock == null || !matchesCorner(currentBlock)) return null

		// If it is a corner, progress right on the bottom side
		// Set first block
		currentBlock = currentBlock.getRelativeIfLoaded(rightFace)

		while (currentBlock != null && matchesSide(currentBlock)) {
			frameRight++

			if (frameRight > 75) {
				detector?.userError("The door is too large! The maximum width is 75 blocks.")
				return null
			}

			debugAudience.highlightBlock(Vec3i(currentBlock.location), 20L)
			currentBlock = currentBlock.getRelativeIfLoaded(rightFace, 1)
		}

		// Check corner again
		if (currentBlock == null || !matchesCorner(currentBlock)) return null

		// Start going up
		currentBlock = currentBlock.getRelativeIfLoaded(BlockFace.UP)

		// Traverse up
		while (currentBlock != null && matchesSide(currentBlock)) {
			frameUp++

			if (frameUp > 75) {
				detector?.userError("The door is too large! The maximum height is 75 blocks.")
				return null
			}

			debugAudience.highlightBlock(Vec3i(currentBlock.location), 20L)
			currentBlock = currentBlock.getRelativeIfLoaded(BlockFace.UP, 1)
		}

		// Check top right corner
		if (currentBlock == null || !matchesCorner(currentBlock)) return null

		// Start going left from the top right
		currentBlock = currentBlock.getRelativeIfLoaded(rightFace.oppositeFace)

		// Traverse left
		while (currentBlock != null && matchesSide(currentBlock)) {
			debugAudience.highlightBlock(Vec3i(currentBlock.location), 20L)
			currentBlock = currentBlock.getRelativeIfLoaded(rightFace.oppositeFace, 1)
		}

		// Check top right corner
		if (currentBlock == null || !matchesCorner(currentBlock)) return null

		return ShapeResult(
			frameRight = frameRight,
			frameUp = frameUp,
			frameVerticalOffset = verticalOffset
		)
	}

	private fun getInteriorBlocks(sign: Sign, shapeResult: ShapeResult): Collection<Block>? {
		val blocks = mutableSetOf<Block>()

		val signFace = sign.getFacing()
		val rightFace = signFace.oppositeFace.rightFace

		// Block the sign is on
		val signHolder = sign.block.getRelativeIfLoaded(signFace.oppositeFace) ?: return null
		val starterBlock = signHolder
			.getRelativeIfLoaded(rightFace)
			?.getRelativeIfLoaded(BlockFace.DOWN, shapeResult.frameVerticalOffset) ?: return null

		for (right in 0 until shapeResult.frameRight) {
			val rightBlock = starterBlock.getRelativeIfLoaded(rightFace, right) ?: return null

			for (up in 0 until shapeResult.frameUp) {
				blocks.add(rightBlock.getRelativeIfLoaded(BlockFace.UP, up) ?: return null)
			}
		}

		return blocks
	}

	private fun matchesOn(blocks: Collection<Block>): Boolean = blocks.all { it.getTypeSafe() == Material.NETHER_PORTAL }

	private fun matchesOff(blocks: Collection<Block>): Boolean = blocks.all { it.getTypeSafe() == Material.IRON_BARS }

	private fun setOn(sign: Sign, blocks: Collection<Block>) {
		val direction = sign.getFacing().oppositeFace

		val portalBlock = Material.NETHER_PORTAL.createBlockData {
			(it as Orientable).axis = direction.rightFace.axis
		}

		blocks.forEach { it.setBlockData(portalBlock, false) }
	}

	private fun setOff(sign: Sign, blocks: Collection<Block>) {
		val direction = sign.getFacing().oppositeFace

		val barsBlock = Material.IRON_BARS.createBlockData {
			(it as MultipleFacing).setFace(direction.rightFace, true)
			it.setFace(direction.leftFace, true)
		}

		blocks.forEach { it.setBlockData(barsBlock, false) }
	}

	private fun matchesCorner(block: Block): Boolean {
		return getBlockTypeSafe(block.world, block.x, block.y, block.z) == Material.IRON_BLOCK
	}

	private fun matchesSide(block: Block): Boolean {
		return getBlockTypeSafe(block.world, block.x, block.y, block.z)?.isSlab == true
	}

	private data class ShapeResult(
		val frameRight: Int,
		val frameUp: Int,
		val frameVerticalOffset: Int
	)
}
