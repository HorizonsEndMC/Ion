package net.starlegacy.feature.multiblock.dockingtube

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.Vec3i
import net.starlegacy.util.action
import net.starlegacy.util.colorize
import net.starlegacy.util.getFacing
import net.starlegacy.util.getRelativeIfLoaded
import net.starlegacy.util.isDoor
import net.starlegacy.util.isGlass
import net.starlegacy.util.msg
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.Player

object ConnectedDockingTubeMultiblock : DockingTubeMultiblock("&2[Connected]".colorize()) {
	override fun MultiblockShape.RequirementBuilder.tubeStateExtension() = anyGlass()

	override fun toggle(sign: Sign, player: Player) {
		if (ActiveStarships.findByBlock(sign.block) != null) {
			player msg "&cCannot toggle tube in an active ship"
			return
		}

		val direction = sign.getFacing().oppositeFace
		val doorTop = sign.block.getRelative(direction)

		val buttons: List<Block> = getButtons(sign.location, direction)

		for (distance in 1..100) {
			if (distance == 100) {
				player msg "&cOther end not found!"
				return
			}

			val block = doorTop.getRelative(direction, distance + 2)

			// if it's not air, it is either the destination or an obstruction
			val blockType = block.type
			if (blockType == Material.AIR) {
				val buttonRelatives = buttons.map { it.getRelative(direction, distance) }

				for (buttonRelative in buttonRelatives) {
					val type = buttonRelative.type
					if (type == Material.AIR || type.isGlass) continue

					player msg "Blocked at ${Vec3i(buttonRelative.location)}"
					return
				}
				continue
			}

			// if it's not a door it's an obstruction
			if (!blockType.isDoor) {
				player msg "&cDocking tube is blocked or the other end is missing/misaligned. Distance: $distance"
				return
			}

			// get the location the other side would have a sign at
			// doesn't actually have to be a sign block
			// if it's unloaded then tell them too move closer
			val otherSignLocation = block.getRelativeIfLoaded(direction)?.location ?: run {
				player msg "&cDoor on other end is too far."
				return
			}

			// if the other side's sign is not a valid docking tube then we can't dock
			if (!signMatchesStructure(otherSignLocation, direction.oppositeFace)) {
				player msg "&cDocking tube on the other end is not valid or is not aligned correctly."
				return
			}

			// set all the blocks in between to air
			for (i in 0..distance) {
				buttons.forEach { button ->
					button.getRelative(direction, i).setType(Material.AIR, false)
				}
			}

			fun setButtons(buttons: List<Block>, facing: BlockFace) {
				val button = Material.STONE_BUTTON.createBlockData {
					(it as Switch).facing = facing
				}
				buttons.forEach {
					it.setBlockData(button, false)
				}
			}

			val otherButtons = getButtons(otherSignLocation, direction.oppositeFace)

			setButtons(buttons, direction)
			setButtons(otherButtons, direction.oppositeFace)

			player action "&2Docking tube disconnected."
			sign.setLine(3, DisconnectedDockingTubeMultiblock.stateText)
			sign.update(false, false)

			sign.world.playSound(sign.location, Sound.BLOCK_PISTON_CONTRACT, 1.0f, 1.5f)
			return
		}
	}
}