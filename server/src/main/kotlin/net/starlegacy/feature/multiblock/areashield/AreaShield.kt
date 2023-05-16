package net.starlegacy.feature.multiblock.areashield

import net.starlegacy.feature.machine.AreaShields
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class AreaShield(val radius: Int) : Multiblock(), PowerStoringMultiblock {
	override fun onTransformSign(player: Player, sign: Sign) {
		AreaShields.register(sign.location, radius)
		player.sendMessage(ChatColor.GREEN.toString() + "Area Shield created.")
	}

	override val name get() = "areashield"

	override val maxPower = 100_000

	override val signText = createSignText(
		"&6Area",
		"&bParticle Shield",
		null,
		"&8Radius: &a$radius"
	)
}
