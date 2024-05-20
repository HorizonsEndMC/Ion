package net.horizonsend.ion.server.features.multiblock.mininglasers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier2 : MiningLaserMultiblock() {
	override val signText: Array<Component?> = arrayOf(
		Component.text("Mining ").color(NamedTextColor.DARK_GRAY)
			.append(Component.text("Laser").color(NamedTextColor.GREEN)),
		Component.text("Tier 2").color(NamedTextColor.AQUA),
		Component.text(""),
		Component.text("")
	)

	override val maxPower: Int = 300000
	override val beamOrigin = Triple(0, 3, 1)
	override val range: Double = 110.0
	override val mineRadius = 5
	override val beamCount: Int = 3
	override val maxBroken: Int = 7
	override val sound: String = "horizonsend:starship.weapon.mining_laser.t2_loop"

	override val tier: Int = 2
}

object MiningLaserMultiblockTier2Top : MiningLaserMultiblockTier2() {
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +5, -2)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyPipedInventory()
				x(+0).noteBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).ironBlock()
			}

			y(+3) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}

			y(+4) {
				x(+0).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			y(+1) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}

			y(+2) {
				x(-1).ironBlock()
				x(+0).emeraldBlock()
				x(+1).ironBlock()
			}

			y(+3) {
				x(-1).terracotta()
				x(+0).emeraldBlock()
				x(+1).terracotta()
			}

			y(+4) {
				x(-1).anyGlassPane()
				x(+0).lodestone()
				x(+1).anyGlassPane()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).titaniumBlock()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).ironBlock()
			}

			y(+3) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}

			y(+4) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier2Bottom : MiningLaserMultiblockTier2() {
	override val side = BlockFace.DOWN

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -5, -2)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+1) {
				x(-1).anyPipedInventory()
				x(+0).noteBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).ironBlock()
			}

			y(-3) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}

			y(-4) {
				x(+0).anyGlassPane()
			}
		}

		z(+1) {
			y(+1) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			y(-1) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}

			y(-2) {
				x(-1).ironBlock()
				x(+0).emeraldBlock()
				x(+1).ironBlock()
			}

			y(-3) {
				x(-1).terracotta()
				x(+0).emeraldBlock()
				x(+1).terracotta()
			}

			y(-4) {
				x(-1).anyGlassPane()
				x(+0).lodestone()
				x(+1).anyGlassPane()
			}
		}

		z(+2) {
			y(+1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(-0) {
				x(-1).anyGlassPane()
				x(+0).titaniumBlock()
				x(+1).anyGlassPane()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).ironBlock()
			}

			y(-3) {
				x(-1).anyStairs()
				x(+0).terracotta()
				x(+1).anyStairs()
			}

			y(-4) {
				x(+0).anyGlassPane()
			}
		}
	}
}
