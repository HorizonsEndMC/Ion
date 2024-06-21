package net.horizonsend.ion.server.features.multiblock.type.checklist

import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side

object BattleCruiserReactorMultiblock : Multiblock() {
	override val name: String = "bcreactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Battlecruiser",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)

	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.getSide(Side.FRONT).line(0).plainText().equals("[reactor]", ignoreCase = true)
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anySlab()
				x(+0).anyStairs()
				x(+1).anySlab()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).redstoneBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+1) {
				x(-2).ironBlock()
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
				x(+2).ironBlock()
			}
			y(+2) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).redstoneBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+3) {
				x(-1).anySlab()
				x(+0).anyStairs()
				x(+1).anySlab()
			}
		}
		z(+1) {
			y(-1) {
				x(-2).anySlab()
				x(-1).titaniumBlock()
				x(+0).anyStairs()
				x(+1).titaniumBlock()
				x(+2).anySlab()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).seaLantern()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}
			y(+1) {
				x(-2).anyGlassPane()
				x(-1).seaLantern()
				x(+0).endRod()
				x(+1).seaLantern()
				x(+2).anyGlassPane()
			}
			y(+2) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).seaLantern()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}
			y(+3) {
				x(-2).anySlab()
				x(-1).titaniumBlock()
				x(+0).anyStairs()
				x(+1).titaniumBlock()
				x(+2).anySlab()
			}
		}
		z(+2) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).seaLantern()
				x(+0).type(Material.GRINDSTONE)
				x(+1).seaLantern()
				x(+2).ironBlock()
			}
			y(+1) {
				x(-2).anyGlass()
				x(-1).endRod()
				x(+0).bcReactorCore()
				x(+1).endRod()
				x(+2).anyGlass()
			}
			y(+2) {
				x(-2).ironBlock()
				x(-1).seaLantern()
				x(+0).type(Material.GRINDSTONE)
				x(+1).seaLantern()
				x(+2).ironBlock()
			}
			y(+3) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}
		}
		z(+3) {
			y(-1) {
				x(-2).anySlab()
				x(-1).titaniumBlock()
				x(+0).anyStairs()
				x(+1).titaniumBlock()
				x(+2).anySlab()
			}
			y(+0) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).seaLantern()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}
			y(+1) {
				x(-2).anyGlassPane()
				x(-1).seaLantern()
				x(+0).endRod()
				x(+1).seaLantern()
				x(+2).anyGlassPane()
			}
			y(+2) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).seaLantern()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}
			y(+3) {
				x(-2).anySlab()
				x(-1).titaniumBlock()
				x(+0).anyStairs()
				x(+1).titaniumBlock()
				x(+2).anySlab()
			}
		}
		z(+4) {
			y(-1) {
				x(-1).anySlab()
				x(+0).anyStairs()
				x(+1).anySlab()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).redstoneBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+1) {
				x(-2).ironBlock()
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
				x(+2).ironBlock()
			}
			y(+2) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).redstoneBlock()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}
			y(+3) {
				x(-1).anySlab()
				x(+0).anyStairs()
				x(+1).anySlab()
			}
		}
	}
}

