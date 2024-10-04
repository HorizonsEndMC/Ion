package net.horizonsend.ion.server.features.multiblock.type.mininglasers

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier1 : MiningLaserMultiblock() {
	override val signText: Array<Component?> = arrayOf(
		Component.text("Mining ").color(NamedTextColor.DARK_GRAY)
			.append(Component.text("Laser").color(NamedTextColor.GREEN)),
		Component.text("Tier 1").color(NamedTextColor.AQUA),
		Component.text(""),
		Component.text("")
	)

	override val maxPower: Int = 100000
	override val beamOrigin = Triple(0, 3, 1)
	override val range: Double = 75.0
	override val mineRadius = 4
	override val beamCount: Int = 1
	override val maxBroken: Int = 6
	override val sound: String = "horizonsend:starship.weapon.mining_laser.t1_loop"

	override val tier: Int = 1
}

object MiningLaserMultiblockTier1Top : MiningLaserMultiblockTier1() {
	override val side = BlockFace.UP
	override val mirrored = false

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +3, -2)

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
				x(+0).terracotta()
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
				x(-1).terracotta()
				x(+0).emeraldBlock()
				x(+1).terracotta()
			}
			y(+2) {
				x(-1).anyGlassPane()
				x(+0).ironBlock()
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
				x(+0).terracotta()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1TopMirrored : MiningLaserMultiblockTier1() {
	override val side = BlockFace.UP
	override val mirrored = true

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +3, -2)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).noteBlock()
				x(+1).anyPipedInventory()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).terracotta()
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
				x(-1).terracotta()
				x(+0).emeraldBlock()
				x(+1).terracotta()
			}
			y(+2) {
				x(-1).anyGlassPane()
				x(+0).ironBlock()
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
				x(+0).terracotta()
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
	override val mirrored = false

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -3, -2)

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
				x(+0).terracotta()
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
				x(-1).terracotta()
				x(+0).emeraldBlock()
				x(+1).terracotta()
			}
			y(-2) {
				x(-1).anyGlassPane()
				x(+0).ironBlock()
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
				x(+0).terracotta()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1BottomMirrored : MiningLaserMultiblockTier1() {
	override val side = BlockFace.DOWN
	override val mirrored = true

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -3, -2)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+1) {
				x(-1).ironBlock()
				x(+0).noteBlock()
				x(+1).anyPipedInventory()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).terracotta()
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
				x(-1).terracotta()
				x(+0).emeraldBlock()
				x(+1).terracotta()
			}
			y(-2) {
				x(-1).anyGlassPane()
				x(+0).ironBlock()
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
				x(+0).terracotta()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1Side : MiningLaserMultiblockTier1() {
	override val side = BlockFace.UP
	override val mirrored = false

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +0, -5)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(+1).ironBlock()
				x(+0).noteBlock()
				x(-1).anyPipedInventory()
			}

			y(+0) {
				x(+1).anyStairs()
				x(+0).emeraldBlock()
				x(-1).anyStairs()
			}

			y(+1) {
				x(+1).ironBlock()
				x(+0).ironBlock()
				x(-1).ironBlock()
			}

		}

		z(+1) {
			y(-1) {
				x(+1).anyStairs()
				x(+0).titaniumBlock()
				x(-1).anyStairs()
			}

			y(+0) {
				x(+1).anyGlass()
				x(+0).emeraldBlock()
				x(-1).anyGlass()
			}
			y(+1) {
				x(+1).anyStairs()
				x(+0).titaniumBlock()
				x(-1).anyStairs()
			}
		}

		z(+2) {
			y(-1) {
				x(+1).anyStairs()
				x(+0).terracotta()
				x(-1).anyStairs()
			}

			y(+0) {
				x(+1).terracotta()
				x(+0).emeraldBlock()
				x(-1).terracotta()
			}

			y(+1) {
				x(+1).anyStairs()
				x(+0).terracotta()
				x(-1).anyStairs()
			}
		}
		z(+3) {
			y(-1) {
				x(+0).anyGlassPane()
			}

			y(+0) {
				x(+1).anyGlassPane()
				x(+0).ironBlock()
				x(-1).anyGlassPane()
			}

			y(+1) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1SideMirrored : MiningLaserMultiblockTier1() {
	override val side = BlockFace.UP
	override val mirrored = true

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +0, -5)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(+1).anyPipedInventory()
				x(+0).noteBlock()
				x(-1).ironBlock()
			}

			y(+0) {
				x(+1).anyStairs()
				x(+0).emeraldBlock()
				x(-1).anyStairs()
			}

			y(+1) {
				x(+1).ironBlock()
				x(+0).ironBlock()
				x(-1).ironBlock()
			}

		}

		z(+1) {
			y(-1) {
				x(+1).anyStairs()
				x(+0).titaniumBlock()
				x(-1).anyStairs()
			}

			y(+0) {
				x(+1).anyGlass()
				x(+0).emeraldBlock()
				x(-1).anyGlass()
			}
			y(+1) {
				x(+1).anyStairs()
				x(+0).titaniumBlock()
				x(-1).anyStairs()
			}
		}

		z(+2) {
			y(-1) {
				x(+1).anyStairs()
				x(+0).terracotta()
				x(-1).anyStairs()
			}

			y(+0) {
				x(+1).terracotta()
				x(+0).emeraldBlock()
				x(-1).terracotta()
			}

			y(+1) {
				x(+1).anyStairs()
				x(+0).terracotta()
				x(-1).anyStairs()
			}
		}
		z(+3) {
			y(-1) {
				x(+0).anyGlassPane()
			}

			y(+0) {
				x(+1).anyGlassPane()
				x(+0).ironBlock()
				x(-1).anyGlassPane()
			}

			y(+1) {
				x(+0).anyGlassPane()
			}
		}
	}
}