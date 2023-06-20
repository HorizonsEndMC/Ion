package net.horizonsend.ion.server.features.moons.aaguns

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.block.Sign
import org.bukkit.entity.Player

class AAGunMultiblock : Multiblock(), PowerStoringMultiblock {
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
}
