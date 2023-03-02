package net.starlegacy.feature.multiblock.cosmeticmultiblocks

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.Vec3i
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class PyrotechnicMachine : Multiblock(), PowerStoringMultiblock {

	override val inputComputerOffset = Vec3i(0, -1, 0)

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)
	}

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).ironBlock()
				x(+1).ironBlock()
			}
			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyStairs()
				x(+1).anyGlassPane()
			}
			y(+2) {
				x(+0).anyGlassPane()
			}
			y(+3) {
				x(+0).anyGlassPane()
			}
		}
		z(+1) {
			y(+0) {
				x(-1).anyGlass()
				x(+0).anyWool()
				x(+1).anyGlass()
			}
			y(+1) {
				x(-1).anyGlass()
				x(+0).anyWool()
				x(+1).anyGlass()
			}
			y(+2) {
				x(-1).anyGlassPane()
				x(+0).dispenser()
				x(+1).anyGlassPane()
			}
			y(+3) {
				x(-1).anyGlassPane()
				x(+1).anyGlassPane()
			}
		}
		z(+2) {
			y(+0) {
				x(-1).ironBlock()
				x(+0).type(Material.CHISELED_QUARTZ_BLOCK)
				x(+1).ironBlock()
			}
			y(+1) {
				x(-1).anyGlassPane()
				x(+0).type(Material.CHISELED_QUARTZ_BLOCK)
				x(+1).anyGlassPane()
			}
			y(+2) {
				x(+0).anyGlassPane()
			}
			y(+3) {
				x(+0).anyGlassPane()
			}
		}
	}

	override val maxPower = 50_000

	override val name = "pyromachine"

	override val signText = createSignText(
		line1 = "&cPyro&ftechnic",
		line2 = "&8Machine",
		line3 = null,
		line4 = null
	)
}
