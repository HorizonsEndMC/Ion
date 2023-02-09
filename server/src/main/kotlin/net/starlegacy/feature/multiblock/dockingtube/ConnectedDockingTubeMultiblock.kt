package net.starlegacy.feature.multiblock.dockingtube

import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.getRelativeIfLoaded
import net.starlegacy.util.isDoor
import net.starlegacy.util.isGlass
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object ConnectedDockingTubeMultiblock : DockingTubeMultiblock(
	MiniMessage.miniMessage().deserialize("[Connected]").color(TextColor.fromHexString("#55FF55"))
) {
	override fun LegacyMultiblockShape.RequirementBuilder.tubeStateExtension() = anyGlass()

	override fun toggle(sign: Sign, player: Player) {
		if (ActiveStarships.findByBlock(sign.block) != null) {
			player.sendFeedbackMessage(FeedbackType.USER_ERROR, "&cCannot toggle tube in an active ship")
			return
		}

		val direction = sign.getFacing().oppositeFace
		val doorTop = sign.block.getRelative(direction)

		val buttons: List<Block> = getButtons(sign.location, direction)

		for (distance in 1..100) {
			if (distance == 100) {
				player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Other end not found!")
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

					player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Blocked at ${Vec3i(buttonRelative.location)}")
					return
				}
				continue
			}

			// if it's not a door it's an obstruction
			if (!blockType.isDoor) {
				player.sendFeedbackMessage(
					FeedbackType.USER_ERROR,
					"&Docking tube is blocked or the other end is missing/misaligned. Distance: $distance"
				)
				return
			}

			// get the location the other side would have a sign at
			// doesn't actually have to be a sign block
			// if it's unloaded then tell them too move closer
			val otherSignLocation = block.getRelativeIfLoaded(direction)?.location ?: run {
				player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Door on other end is too far.")
				return
			}

			// if the other side's sign is not a valid docking tube then we can't dock
			if (!signMatchesStructure(otherSignLocation, direction.oppositeFace)) {
				player.sendFeedbackMessage(
					FeedbackType.USER_ERROR,
					"Docking tube on the other end is not valid or is not aligned correctly."
				)
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

			player.sendFeedbackActionMessage(FeedbackType.SUCCESS, "Docking tube disconnected.")

			sign.persistentDataContainer.set(
				NamespacedKeys.MULTIBLOCK,
				PersistentDataType.STRING,
				DisconnectedDockingTubeMultiblock::class.simpleName!!
			)

			sign.line(3, DisconnectedDockingTubeMultiblock.stateText)
			sign.update(false, false)

			sign.world.playSound(sign.location, Sound.BLOCK_PISTON_CONTRACT, 1.0f, 1.5f)
			return
		}
	}
}
