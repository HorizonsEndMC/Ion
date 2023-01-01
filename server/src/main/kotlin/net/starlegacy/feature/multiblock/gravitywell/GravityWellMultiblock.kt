package net.starlegacy.feature.multiblock.gravitywell

import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.util.getFacing
import org.bukkit.ChatColor
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class GravityWellMultiblock : Multiblock() {

	companion object {

		private val DISABLED = ChatColor.RED.toString() + "[DISABLED]"
		private val ENABLED = ChatColor.GREEN.toString() + "[ENABLED]"

		@JvmStatic
		fun setEnabled(sign: Sign, enabled: Boolean) {
			sign.setLine(2, if (enabled) ENABLED else DISABLED)
			sign.update(false, false)
		}

		@JvmStatic
		fun isEnabled(sign: Sign): Boolean {
			return sign.getLine(2) == ENABLED
		}

		@JvmStatic
		fun getInput(sign: Sign): Inventory {
			return (
				sign.block.getRelative(sign.getFacing().oppositeFace)
					.getRelative(BlockFace.DOWN).state as InventoryHolder
				).inventory
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		super.onTransformSign(player, sign)
		sign.setLine(3, DISABLED)
	}
}
