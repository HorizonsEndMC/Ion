package net.horizonsend.ion.server.features.multiblock.type.starship.gravitywell

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material

object AmplifiedGravityWellMultiblock : GravityWellMultiblock() {
	override val name: String = "amplifiedwell"
	override val signText = createSignText(
		line1 = "&2Gravity",
		line2 = "&8Generator",
		line3 = null,
		line4 = "&8&o[&d&lAmplified&8&o]"
	)

	override val displayName: Component get() = text("Amplified Gravity Well")
	override val description: Component get() = text("Generates a spherical region around the starship that inhibits hyperspace travel.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).anyPipedInventory()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(-1).type(Material.IRON_BARS)
				x(+0).type(Material.GLASS)
				x(+1).type(Material.IRON_BARS)
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}

			y(+0) {
				x(-2).type(Material.IRON_BARS)
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
				x(+2).type(Material.IRON_BARS)
			}

			y(+1) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}
		}

		z(+2) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}

			y(+0) {
				x(-2).anyGlass()
				x(-1).anyGlassPane()
				x(+0).emeraldBlock()
				x(+1).anyGlassPane()
				x(+2).anyGlass()
			}

			y(+1) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}
		}

		z(+3) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}

			y(+0) {
				x(-2).type(Material.IRON_BARS)
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
				x(+2).type(Material.IRON_BARS)
			}

			y(+1) {
				x(-2).ironBlock()
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
				x(+2).ironBlock()
			}
		}

		z(+4) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(-1).type(Material.IRON_BARS)
				x(+0).type(Material.GLASS)
				x(+1).type(Material.IRON_BARS)
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}
		}
	}
}
