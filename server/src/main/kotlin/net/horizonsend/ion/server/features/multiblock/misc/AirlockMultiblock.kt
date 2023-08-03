package net.horizonsend.ion.server.features.multiblock.misc

import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object AirlockMultiblock : Multiblock(), InteractableMultiblock {
	override val name: String = "airlock"

	override val signText = createSignText(
		line1 = "&7Airlock",
		line2 = null,
		line3 = "&bRayshielding",
		line4 = "&bSolutions, Inc."
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

	private val OFF = text("-[OFF]-", NamedTextColor.RED)
	private val ON = text("-[ON]-", NamedTextColor.GREEN)

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return

		val direction = sign.getFacing().oppositeFace
		val right = direction.rightFace
		val topPortal = sign.block.getRelative(direction).getRelative(right)
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

		val component = if (enabled) { ON } else { OFF }

		sign.line(1, component)
		sign.update()
	}
}
