package net.starlegacy.feature.multiblock.particleshield

import net.starlegacy.feature.multiblock.Multiblock
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class ShieldMultiblock : Multiblock() {
	open val isReinforced: Boolean = false

	override val name: String = "shield"

	// let people use [particleshield] if they want
	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.getLine(0).equals("[particleshield]", ignoreCase = true)
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.setLine(2, sign.getLine(1))
	}

	override fun setupSign(player: Player, sign: Sign) {
		if (sign.getLine(1).isEmpty()) {
			player.sendMessage(ChatColor.RED.toString() + "The second line must be the shield's name.")
			return
		}

		super.setupSign(player, sign)
	}
}
