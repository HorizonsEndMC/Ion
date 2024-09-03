package net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.ChatColor
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

abstract class GravityWellMultiblock : Multiblock(), InteractableMultiblock {
	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.getSide(Side.FRONT).line(0).plainText().equals("[gravwell]", ignoreCase = true)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val starship = ActiveStarships.findByPassenger(player) ?: return player.userError("You're not riding the starship")
		if (!starship.contains(sign.x, sign.y, sign.z)) return

		if (StarshipCruising.isCruising(starship)) return player.userError("Cannot activate while cruising")

		when (event.action) {
			Action.RIGHT_CLICK_BLOCK -> {
				Interdiction.toggleGravityWell(starship)
			}

			Action.LEFT_CLICK_BLOCK -> {
				Interdiction.pulseGravityWell(player, starship, sign)
			}

			else -> return
		}
	}

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
