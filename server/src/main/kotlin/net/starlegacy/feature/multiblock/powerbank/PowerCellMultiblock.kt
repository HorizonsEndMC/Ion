package net.starlegacy.feature.multiblock.powerbank

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.Vec3i
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object PowerCellMultiblock : Multiblock(), PowerStoringMultiblock {
	override val name = "powercell"

	override val signText = createSignText(
		line1 = "&6Power &8Cell",
		line2 = "------",
		line3 = null,
		line4 = "&cCompact Power"
	)

	override val maxPower = 50_000

	override val inputComputerOffset = Vec3i(0, 0, 0)

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)
	}

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).redstoneBlock()
				x(+1).anyGlassPane()
			}
		}
	}
}
