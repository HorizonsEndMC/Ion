package net.starlegacy.feature.multiblock.misc

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.multiblock.InteractableMultiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.util.axis
import net.starlegacy.util.getFacing
import net.starlegacy.util.leftFace
import net.starlegacy.util.rightFace
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object AirlockMultiblock : Multiblock(), InteractableMultiblock {
	override val name: String = "airlock"

	override val signText: Array<Component?> = arrayOf(
		text("Airlock", NamedTextColor.AQUA)
	)

	override fun MultiblockShape.buildStructure() {
		val xOffset = 1 // sign is a block to the left
		z(+0) {
			y(-2) {
				x(xOffset + 0).ironBlock()
			}

			y(-1) {
				x(xOffset - 1).ironBlock()
				x(xOffset + 0).anyType(Material.IRON_BARS, Material.NETHER_PORTAL)
				x(xOffset + 1).ironBlock()
			}

			y(+0) {
				x(xOffset - 1).ironBlock()
				x(xOffset + 0).anyType(Material.IRON_BARS, Material.NETHER_PORTAL)
				x(xOffset + 1).ironBlock()
			}

			y(+1) {
				x(xOffset + 0).ironBlock()
			}
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) = sign.line(1, OFF)

	val OFF = text("-[OFF]-", NamedTextColor.RED)
	val ON = text("-[ON]-", NamedTextColor.GREEN)

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return

		val block = event.clickedBlock ?: return
		if (Multiblocks[sign] !is AirlockMultiblock) return

		val direction = sign.getFacing().oppositeFace
		val right = direction.rightFace
		val topPortal = block.getRelative(direction).getRelative(right)
		val bottomPortal = topPortal.getRelative(BlockFace.DOWN)

		val enabled = topPortal.type == Material.IRON_BARS

		val newData = if (enabled) {
			Material.NETHER_PORTAL.createBlockData {
				(it as org.bukkit.block.data.Orientable).axis = direction.rightFace.axis
			}
		} else {
			Material.IRON_BARS.createBlockData {
				(it as org.bukkit.block.data.MultipleFacing).setFace(direction.rightFace, true)
				it.setFace(direction.leftFace, true)
			}
		}

		topPortal.blockData = newData
		bottomPortal.blockData = newData

		val component = if (enabled) ON else OFF

		sign.line(1, component)
		sign.update()
	}
}
