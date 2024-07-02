package net.horizonsend.ion.server.features.multiblock.checklist

import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import org.bukkit.Material
import org.bukkit.block.Sign

object HeavyFrigateReactorMultiblock : Multiblock() {
	override val name: String = "frigatereactor"

	override val signText = createSignText(
			"&7-=[&c==&a==&b==&7]=-",
			"&0Heavy Frigate",
			"&7&cFusion Reactor&7",
			"&7-=[&c==&a==&b==&7]=-"
	)

	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.line(0).plainText().equals("[reactor]", ignoreCase = true)
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
			y(+1) {
				x(-1).ironBlock()
				x(+0).anyGlassPane()
				x(+1).ironBlock()
			}
			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
		z(-1) {
			y(+0) {
				x(-1).redstoneBlock()
				x(+0).anyGlass()
				x(+1).redstoneBlock()
			}
			y(+1) {
				x(-1).anyGlassPane()
				x(+0).heavyFrigateReactorCore()
				x(+1).anyGlassPane()
			}
			y(+2) {
				x(-1).titaniumBlock()
				x(+0).anyGlass()
				x(+1).titaniumBlock()
			}
		}
		z(-2) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
			y(+1) {
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
			}
			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}
}

