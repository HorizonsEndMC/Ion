package net.starlegacy.feature.multiblock.particleshield

import net.horizonsend.ion.common.extensions.userError
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object EventShieldMultiblock : SphereShieldMultiblock() {
	override val signText = createSignText(
		line1 = "&3Particle Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "&8Class &d0.8i"
	)

	override val maxRange = 60
	override val isReinforced: Boolean = true

	override fun setupSign(player: Player, sign: Sign) {
		if (player.hasPermission("ioncore.eventweapon")) return player.userError("Nuh uh")

		super.setupSign(player, sign)
	}

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).sponge()
				x(+0).chetheriteBlock()
				x(+1).sponge()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).type(Material.LAPIS_BLOCK)
				x(+1).anyGlassPane()
			}
		}
	}
}
