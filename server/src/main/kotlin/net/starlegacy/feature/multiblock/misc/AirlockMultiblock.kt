package net.starlegacy.feature.multiblock.misc

import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.feature.multiblock.InteractableMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.util.axis
import net.starlegacy.util.getFacing
import net.starlegacy.util.leftFace
import net.starlegacy.util.rightFace
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
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

	override fun LegacyMultiblockShape.buildStructure() {
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

	override fun onTransformSign(player: Player, sign: Sign) = sign.setLine(1, OFF)

	const val OFF = "<red>-[OFF]-"
	const val ON = "<green>-[ON]-"

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

		val component =
			if (enabled) {
				MiniMessage.miniMessage().deserialize(ON)
			} else {
				MiniMessage.miniMessage()
					.deserialize(OFF)
			}

		sign.line(1, component)
		sign.update()
	}
}
