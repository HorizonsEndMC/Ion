package net.horizonsend.ion.server.features.multiblock.supercapreactor

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.Material

object CruiserReactorMultiblock : Multiblock() {
	override val name: String = "cruiserreactor"
	override val signText = createSignText(
			"&7-=[&c==&a==&b==&7]=-",
			"&0Cruiser",
			"&7&cFusion Reactor&7",
			"&7-=[&c==&a==&b==&7]=-"
	)

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
				x(+0).cruiserReactorCore()
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

