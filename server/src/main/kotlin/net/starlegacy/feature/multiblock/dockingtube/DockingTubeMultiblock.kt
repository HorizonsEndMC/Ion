package net.starlegacy.feature.multiblock.dockingtube

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.feature.multiblock.InteractableMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.util.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.starlegacy.util.axis
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

abstract class DockingTubeMultiblock(val stateText: Component) : Multiblock(), InteractableMultiblock {
	override val name = "dockingtube"

	override val signText = arrayOf(
		MiniMessage.miniMessage().deserialize("<dark_blue>Docking"),
		MiniMessage.miniMessage().deserialize("<dark_blue>Tube"),
		null,
		stateText
	)

	override fun LegacyMultiblockShape.buildStructure() {
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

	abstract fun LegacyMultiblockShape.RequirementBuilder.tubeStateExtension()

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) = toggle(sign, player)

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

	fun relativeOffsetToCoordinate(facing: BlockFace, leftRight: Int, upDown: Int): Vec3i {
		val y = upDown
		var x = 0
		var z = 0

		when (facing.axis) {
			Axis.X -> { z = leftRight }
			Axis.Z -> { x = leftRight }
			else -> { throw NotImplementedError() }
		}

		return Vec3i(x, y, z)
	}

	fun relativeCoordinateToOffset(facing: BlockFace, offset: Vec3i): Pair<Int, Int> {
		val (x, y, z) = offset

		val leftRight: Int = when (facing.axis) {
			Axis.X -> z
			Axis.Z -> x
			else -> { throw NotImplementedError() }
		}

		return leftRight to y
	}
}
