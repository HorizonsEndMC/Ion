package net.horizonsend.ion.server.features.multiblock.type.navigationcomputer

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class NavigationComputerMultiblock : Multiblock() {
	override val name = "navcomputer"

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.line(3, text("[Standby]", NamedTextColor.WHITE))
		sign.update()
	}

	abstract val baseRange: Int
}
