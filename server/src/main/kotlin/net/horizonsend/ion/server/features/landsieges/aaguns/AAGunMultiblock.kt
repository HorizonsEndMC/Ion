package net.horizonsend.ion.server.features.landsieges.aaguns

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object AAGunMultiblock : Multiblock(), PowerStoringMultiblock {
	override val name: String = "antiairgun"
	override val maxPower: Int get() = 1_000_000

	private val ACTIVE_STATE = text("Active", NamedTextColor.GREEN).apply { this.decoration(TextDecoration.BOLD) }
	private val INACTIVE_STATE = text("Inactive", NamedTextColor.RED).apply { this.decoration(TextDecoration.BOLD) }

	override val signText: Array<Component?> = arrayOf(
			text("Anti-Air", NamedTextColor.GOLD),
			text("Particle Gun", NamedTextColor.AQUA),
			null,
			INACTIVE_STATE
		)

	override fun MultiblockShape.buildStructure() {
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)

		if (sign.line(3) == INACTIVE_STATE)
			sign.line(3, ACTIVE_STATE)
		else sign.line(3, INACTIVE_STATE)

		sign.update(true, false)
	}
}
