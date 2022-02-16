package net.starlegacy.feature.multiblock.gravitywell

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.progression.advancement.SLAdvancement
import org.bukkit.Material

object AmplifiedGravityWellMultiblock : GravityWellMultiblock() {
	override val advancement: SLAdvancement = SLAdvancement.GRAVITY_WELL

	override val name: String = "amplifiedwell"
	override val signText = createSignText(
		line1 = "&2Gravity",
		line2 = "&8Generator",
		line3 = null,
		line4 = "&8&o[&d&lAmplified&8&o]"
	)

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
				x(-1).glassPane()
				x(+0).glassPane()
				x(+1).glassPane()
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
				x(-2).glass()
				x(-1).anyGlassPane()
				x(+0).emeraldBlock()
				x(+1).anyGlassPane()
				x(+2).glass()
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
				x(-1).glassPane()
				x(+0).glassPane()
				x(+1).glassPane()
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
