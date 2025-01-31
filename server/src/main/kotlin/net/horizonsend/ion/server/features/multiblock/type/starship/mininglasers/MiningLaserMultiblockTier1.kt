package net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier1 : MiningLaserMultiblock() {
	val tierText = text("Tier 1").color(NamedTextColor.AQUA)
	override val signText: Array<Component?> = arrayOf(
		text("Mining ").color(DARK_GRAY).append(text("Laser").color(GREEN)),
		tierText,
		text(""),
		text("")
	)

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +3, +1)

	override val maxPower: Int = 100000
	override val beamOrigin = Triple(0, 3, 1)
	final override val range: Double = 75.0
	final override val mineRadius = 4
	override val beamCount: Int = 1
	override val maxBroken: Int = 6
	override val sound: String = "horizonsend:starship.weapon.mining_laser.t1_loop"

	override val tier: Int = 1

	override val description: Component = text("Emits a beam $range blocks long that breaks blocks in a $mineRadius block radius.")
}

object MiningLaserMultiblockTier1Top : MiningLaserMultiblockTier1() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Top)"))
	override val side = BlockFace.UP
	override val outputOffset: Vec3i = Vec3i(-1, -1, 0)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyPipedInventory()
				x(+0).powerInput()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).anyTerracotta()
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
				x(-1).anyTerracotta()
				x(+0).emeraldBlock()
				x(+1).anyTerracotta()
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
				x(+0).anyTerracotta()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1TopMirrored : MiningLaserMultiblockTier1() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Top) (Mirrored)"))
	override val side = BlockFace.UP

	override val outputOffset: Vec3i = Vec3i(+1, -1, 0)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).powerInput()
				x(+1).anyPipedInventory()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).anyTerracotta()
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
				x(-1).anyTerracotta()
				x(+0).emeraldBlock()
				x(+1).anyTerracotta()
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
				x(+0).anyTerracotta()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1Bottom : MiningLaserMultiblockTier1() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Bottom)"))
	override val side = BlockFace.DOWN

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -3, +1)
	override val outputOffset: Vec3i = Vec3i(-1, +1, 0)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+1) {
				x(-1).anyPipedInventory()
				x(+0).powerInput()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).anyTerracotta()
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
				x(-1).anyTerracotta()
				x(+0).emeraldBlock()
				x(+1).anyTerracotta()
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
				x(+0).anyTerracotta()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1BottomMirrored : MiningLaserMultiblockTier1() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Bottom) (Mirrored)"))
	override val side = BlockFace.DOWN

	override val outputOffset: Vec3i = Vec3i(+1, +1, 0)
	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -3, -2)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+1) {
				x(-1).ironBlock()
				x(+0).powerInput()
				x(+1).anyPipedInventory()
			}

			y(+0) {
				x(-1).anyWall()
				x(+0).anyGlass()
				x(+1).anyWall()
			}

			y(-1) {
				x(-1).anyStairs()
				x(+0).anyTerracotta()
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
				x(-1).anyTerracotta()
				x(+0).emeraldBlock()
				x(+1).anyTerracotta()
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
				x(+0).anyTerracotta()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier1Side : MiningLaserMultiblockTier1() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Side)"))
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +0, +5)
	override val outputOffset: Vec3i = Vec3i(-1, -1, 0)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(+1).ironBlock()
				x(+0).powerInput()
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
				x(+0).anyTerracotta()
				x(-1).anyStairs()
			}

			y(+0) {
				x(+1).anyTerracotta()
				x(+0).emeraldBlock()
				x(-1).anyTerracotta()
			}

			y(+1) {
				x(+1).anyStairs()
				x(+0).anyTerracotta()
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
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Side) (Mirrored)"))
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +0, -5)
	override val outputOffset: Vec3i = Vec3i(+1, -1, 0)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(+1).anyPipedInventory()
				x(+0).powerInput()
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
				x(+0).anyTerracotta()
				x(-1).anyStairs()
			}

			y(+0) {
				x(+1).anyTerracotta()
				x(+0).emeraldBlock()
				x(-1).anyTerracotta()
			}

			y(+1) {
				x(+1).anyStairs()
				x(+0).anyTerracotta()
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
