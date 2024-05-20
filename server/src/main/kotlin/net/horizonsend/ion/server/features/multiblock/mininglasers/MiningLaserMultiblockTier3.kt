package net.horizonsend.ion.server.features.multiblock.mininglasers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier3 : MiningLaserMultiblock() {
	override val signText: Array<Component?> = arrayOf(
		Component.text("Mining ").color(NamedTextColor.DARK_GRAY)
			.append(Component.text("Laser").color(NamedTextColor.GREEN)),
		Component.text("Tier 3").color(NamedTextColor.AQUA),
		Component.text(""),
		Component.text("")
	)

	override val maxPower: Int = 500000
	override val beamOrigin = Triple(0, 3, 1)
	override val range: Double = 170.0
	override val mineRadius = 7
	override val beamCount: Int = 6
	override val maxBroken: Int = 9
	override val sound: String = "horizonsend:starship.weapon.mining_laser.t3_loop"

	override val tier: Int = 3
}

object MiningLaserMultiblockTier3Top : MiningLaserMultiblockTier3() {
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +6, -3)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyPipedInventory()
				x(+0).noteBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
			}

			y(+2) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(+3) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(+4) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyStairs()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).anyStairs()
			}

			y(+2) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(+3) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(+4) {
				x(-2).anyStairs()
				x(-1).terracotta()
				x(+0).anyStairs()
				x(+1).terracotta()
				x(+2).anyStairs()
			}

			y(+5) {
				x(+0).anyGlassPane()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			y(+1) {
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}

			y(+2) {
				x(-2).aluminumBlock()
				x(-1).sponge()
				x(+0).emeraldBlock()
				x(+1).sponge()
				x(+2).aluminumBlock()
			}

			y(+3) {
				x(-2).aluminumBlock()
				x(-1).sponge()
				x(+0).emeraldBlock()
				x(+1).sponge()
				x(+2).aluminumBlock()
			}

			y(+4) {
				x(-2).terracotta()
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
				x(+2).terracotta()
			}

			y(+5) {
				x(-1).anyGlassPane()
				x(+0).lodestone()
				x(+1).anyGlassPane()
			}
		}

		z(+3) {
			y(-1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyStairs()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).anyStairs()
			}

			y(+2) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(+3) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(+4) {
				x(-2).anyStairs()
				x(-1).terracotta()
				x(+0).anyStairs()
				x(+1).terracotta()
				x(+2).anyStairs()
			}

			y(+5) {
				x(+0).anyGlassPane()
			}
		}

		z(+4) {
			y(-1) {
				x(-1).anyStairs()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+1).ironBlock()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
			}

			y(+2) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(+3) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(+4) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}
		}
	}
}

object MiningLaserMultiblockTier3Bottom : MiningLaserMultiblockTier3() {
	override val side = BlockFace.DOWN

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -6, -3)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+1) {
				x(-1).anyPipedInventory()
				x(+0).noteBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
			}

			y(-2) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(-3) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(-4) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(+1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}

			y(-1) {
				x(-2).anyStairs()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).anyStairs()
			}

			y(-2) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(-3) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(-4) {
				x(-2).anyStairs()
				x(-1).terracotta()
				x(+0).anyStairs()
				x(+1).terracotta()
				x(+2).anyStairs()
			}

			y(-5) {
				x(+0).anyGlassPane()
			}
		}

		z(+2) {
			y(+1) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			y(-1) {
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}

			y(-2) {
				x(-2).aluminumBlock()
				x(-1).sponge()
				x(+0).emeraldBlock()
				x(+1).sponge()
				x(+2).aluminumBlock()
			}

			y(-3) {
				x(-2).aluminumBlock()
				x(-1).sponge()
				x(+0).emeraldBlock()
				x(+1).sponge()
				x(+2).aluminumBlock()
			}

			y(-4) {
				x(-2).terracotta()
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
				x(+2).terracotta()
			}

			y(-5) {
				x(-1).anyGlassPane()
				x(+0).lodestone()
				x(+1).anyGlassPane()
			}
		}

		z(+3) {
			y(+1) {
				x(-2).anyStairs()
				x(-1).ironBlock()
				x(+0).anyGlass()
				x(+1).ironBlock()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).ironBlock()
			}

			y(-1) {
				x(-2).anyStairs()
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
				x(+2).anyStairs()
			}

			y(-2) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(-3) {
				x(-2).titaniumBlock()
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
				x(+2).titaniumBlock()
			}

			y(-4) {
				x(-2).anyStairs()
				x(-1).terracotta()
				x(+0).anyStairs()
				x(+1).terracotta()
				x(+2).anyStairs()
			}

			y(-5) {
				x(+0).anyGlassPane()
			}
		}

		z(+4) {
			y(+1) {
				x(-1).anyStairs()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+1).ironBlock()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
			}

			y(-2) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(-3) {
				x(-1).titaniumBlock()
				x(+0).aluminumBlock()
				x(+1).titaniumBlock()
			}

			y(-4) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}
		}
	}
}
