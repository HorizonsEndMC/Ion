package net.horizonsend.ion.server.features.multiblock.moonsiege

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent

object AAGunMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name: String = "antiairgun"
	override val maxPower: Int get() = 1_000_000

	override val signText: Array<Component?> = arrayOf(
			text("Anti-Air", NamedTextColor.GOLD),
			text("Particle Gun", NamedTextColor.AQUA),
			null,
			null
		)

	override fun LegacyMultiblockShape.buildStructure() {

	}

	override fun onTransformSign(player: Player, sign: Sign) {
		//TODO: Code a remote trigger
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		TODO("Not yet implemented")
	}
}
