package net.starlegacy.feature.multiblock.dockingtube

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.util.rightFace
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class DockingTubeMultiblock(val stateText: String) : Multiblock() {
	override val name = "dockingtube"

	override val signText = createSignText(
		line1 = "&1Docking",
		line2 = "&1Tube",
		line3 = null,
		line4 = stateText
	)

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).anyDoor()

		z(+1) {
			y(-2) {
				x(0).tubeStateExtension()
			}

			y(-1) {
				x(-1).tubeStateExtension()
				x(+1).tubeStateExtension()
			}

			y(+0) {
				x(-1).tubeStateExtension()
				x(+1).tubeStateExtension()
			}

			y(+1) {
				x(0).tubeStateExtension()
			}
		}
	}

	abstract fun MultiblockShape.RequirementBuilder.tubeStateExtension()

	abstract fun toggle(sign: Sign, player: Player)

	fun getButtons(sign: Location, direction: BlockFace): List<Block> {
		val buttons = ArrayList<Block>()
		val right = direction.rightFace
		val left = right.oppositeFace
		val doorTopFront = sign.block.getRelative(direction).getRelative(direction)
		val doorBottomFront = doorTopFront.getRelative(BlockFace.DOWN)
		buttons.add(doorTopFront.getRelative(right))
		buttons.add(doorTopFront.getRelative(left))
		buttons.add(doorTopFront.getRelative(BlockFace.UP))
		buttons.add(doorBottomFront.getRelative(right))
		buttons.add(doorBottomFront.getRelative(left))
		buttons.add(doorBottomFront.getRelative(BlockFace.DOWN))
		return buttons
	}
}
