package net.starlegacy.feature.multiblock.misc

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object AirlockMultiblock : Multiblock() {
	override val name: String = "airlock"

	override val signText = createSignText(
		line1 = "&7Airlock",
		line2 = null,
		line3 = "&bRayshielding",
		line4 = "&bSolutions, Inc."
	)

	override fun LegacyMultiblockShape.buildStructure() {
		val xOffset = 1 // sign is a block to the left
		z(+0) {
			y(-2) {
				x(xOffset + 0).ironBlock()
			}

			y(-1) {
				x(xOffset - 1).ironBlock()
				x(xOffset + 0).anyType(Material.IRON_BARS, Material.NETHER_PORTAL)
				x(xOffset + 1).ironBlock()
			}

			y(+0) {
				x(xOffset - 1).ironBlock()
				x(xOffset + 0).anyType(Material.IRON_BARS, Material.NETHER_PORTAL)
				x(xOffset + 1).ironBlock()
			}

			y(+1) {
				x(xOffset + 0).ironBlock()
			}
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) = sign.setLine(1, OFF)

	const val OFF = "<red>-[OFF]-"
	const val ON = "<green>-[ON]-"
}
