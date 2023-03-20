package net.horizonsend.ion.server.features.starship.mininglaser.multiblock

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier1 : MiningLaserMultiblock() {
	override val signText: Array<Component?> = arrayOf(
		Component.text("Mining ").color(NamedTextColor.DARK_GRAY)
			.append(Component.text("Laser").color(NamedTextColor.GREEN)),
		Component.text("Tier 1").color(NamedTextColor.AQUA),
		Component.text(""),
		Component.text("")
	)

	override val inputComputerOffset = Vec3i(0, -1, 0)
	override val maxPower: Int = 100000
	override val beamOrigin = Triple(0, 3, 1)
	override val range: Double = 60.0
	override val mineRadius = 3
	override val beamCount: Int = 1
	override val maxBroken: Int = 5
	override val sound: String = "starship.weapon.mining_laser.t1_loop"

	override val tier: Int = 1
}

object MiningLaserMultiblockTier1Top : MiningLaserMultiblockTier1() {
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +3, -2)

	override fun LegacyMultiblockShape.buildStructure() {
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
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).emeraldBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}
			y(+1) {
				x(-1).stainedTerracotta()
				x(+0).emeraldBlock()
				x(+1).stainedTerracotta()
			}
			y(+2) {
				x(-1).anyGlassPane()
				x(+0).lodestone()
				x(+1).anyGlassPane()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1Bottom : MiningLaserMultiblockTier1() {
	override val side = BlockFace.DOWN

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -3, -2)

	override fun LegacyMultiblockShape.buildStructure() {
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
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).anyGlassPane()
			}
		}

		z(+1) {
			y(+1) {
				x(-1).ironBlock()
				x(+0).emeraldBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}
			y(-1) {
				x(-1).stainedTerracotta()
				x(+0).emeraldBlock()
				x(+1).stainedTerracotta()
			}
			y(-2) {
				x(-1).anyGlassPane()
				x(+0).lodestone()
				x(+1).anyGlassPane()
			}
		}

		z(+2) {
			y(+1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).anyGlassPane()
			}
		}
	}
}
