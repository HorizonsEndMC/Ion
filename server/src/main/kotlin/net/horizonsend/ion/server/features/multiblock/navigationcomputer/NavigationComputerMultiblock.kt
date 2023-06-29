package net.horizonsend.ion.server.features.multiblock.navigationcomputer

import net.md_5.bungee.api.ChatColor
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class NavigationComputerMultiblock : Multiblock() {
	override val name = "navcomputer"

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.setLine(3, ChatColor.WHITE.toString() + "[Standby]")
		sign.update()
	}

	abstract val baseRange: Int
}
