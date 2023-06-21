package net.horizonsend.ion.server.features.multiblock.moonsiege

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent

class AAGunMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name: String
		get() = "aagun"

	override val signText: Array<Component?>
		get() = createSignText(
			"&6Anti-Air",
			"&bParticle Gun",
			null,
			null
		)

	override fun LegacyMultiblockShape.buildStructure() {

	}

	override val maxPower: Int get() = 1_000_000
	override fun onTransformSign(player: Player, sign: Sign) {
		//TODO: Code a remote trigger
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		TODO("Not yet implemented")
	}
}
