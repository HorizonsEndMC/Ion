package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object MiniReactorMultiblock : AbstractReactorCore({ customBlock(CustomBlockKeys.MINI_REACTOR_CORE.getValue()) }) {
	override val displayName: Component get() = text("Mini Reactor")
	override val description: Component get() = text("Reactor core critical to tech 2 Fighter and Gunship functionality.")

	override val name: String = "minireactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Mini",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
	override fun MultiblockShape.buildStructure() {
		z(-2) {
			y(0) {
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
			}
			y(1) {
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
			}
		}
		z(-1) {
			y(0) {
				x(1).titaniumBlock()
				x(0).sponge()
				x(-1).titaniumBlock()
			}
			y(1) {
				x(1).anyGlass()
				x(0).customBlock(CustomBlockKeys.MINI_REACTOR_CORE.getValue())
				x(-1).anyGlass()
			}
		}
		z(0) {
			y(0) {
				x(1).anyStairs()
				x(0).redstoneBlock()
				x(-1).anyStairs()
			}
			y(1) {
				x(1).anyStairs()
				x(0).anyGlass()
				x(-1).anyStairs()
			}
		}
	}
}

