package net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier2 : MiningLaserMultiblock() {
	val tierText = text("Tier 2").color(NamedTextColor.AQUA)
	override val signText: Array<Component?> = arrayOf(
		text("Mining ").color(NamedTextColor.DARK_GRAY)
			.append(text("Laser").color(NamedTextColor.GREEN)),
		tierText,
		text(""),
		text("")
	)

	override val maxPower: Int = 300000
	override val beamOrigin = Triple(0, 3, 1)
	final override val range: Double = 110.0
	final override val mineRadius = 5
	override val beamCount: Int = 3
	override val maxBroken: Int = 13
	override val sound: String = "horizonsend:starship.weapon.mining_laser.t2_loop"

	override val tier: Int = 2

	override val description: Component = text("Emits a beam $range blocks long that breaks blocks in a $mineRadius block radius.")
}

object MiningLaserMultiblockTier2Top : MiningLaserMultiblockTier2() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Top)"))
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +5, +1)
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
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).ironBlock()
			}

			y(+3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
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
				x(-1).terracottaOrDoubleSlab()
				x(+0).emeraldBlock()
				x(+1).terracottaOrDoubleSlab()
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
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}

			y(+4) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier2TopMirrored : MiningLaserMultiblockTier2() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Top) (Mirrored)"))
	override val side = BlockFace.UP

	override val outputOffset: Vec3i = Vec3i(+1, -1, 0)

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +5, -2)

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
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(+2) {
				x(+0).ironBlock()
			}

			y(+3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
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
				x(-1).terracottaOrDoubleSlab()
				x(+0).emeraldBlock()
				x(+1).terracottaOrDoubleSlab()
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
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}

			y(+4) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier2Bottom : MiningLaserMultiblockTier2() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Bottom)"))
	override val side = BlockFace.DOWN

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -5, +1)
	override val outputOffset: Vec3i = Vec3i(-1, +1, 0)
	override val isBottom: Boolean = true

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
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).ironBlock()
			}

			y(-3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
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
				x(-1).terracottaOrDoubleSlab()
				x(+0).emeraldBlock()
				x(+1).terracottaOrDoubleSlab()
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
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}

			y(-4) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier2BottomMirrored : MiningLaserMultiblockTier2() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Bottom) (Mirrored)"))
	override val side = BlockFace.DOWN

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -5, -2)
	override val outputOffset: Vec3i = Vec3i(+1, +1, 0)
	override val isBottom: Boolean = true

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
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(-2) {
				x(+0).ironBlock()
			}

			y(-3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
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
				x(-1).terracottaOrDoubleSlab()
				x(+0).emeraldBlock()
				x(+1).terracottaOrDoubleSlab()
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
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}

			y(-4) {
				x(+0).anyGlassPane()
			}
		}
	}
}

object MiningLaserMultiblockTier2Side : MiningLaserMultiblockTier2() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Side)"))
	override val side = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +0, +7)
	override val outputOffset: Vec3i = Vec3i(-1, -1, 0)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyPipedInventory()
				x(+0).powerInput()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).titaniumBlock()
				x(+0).titaniumBlock()
				x(+1).titaniumBlock()
			}

		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}
		}
		z(+3) {
			y(-1) {
				x(+0).ironBlock()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+0).emeraldBlock()
				x(+1).ironBlock()
			}

			y(+1) {
				x(+0).ironBlock()
			}
		}
		z(+4) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).terracottaOrDoubleSlab()
				x(+0).emeraldBlock()
				x(+1).terracottaOrDoubleSlab()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}
			z(+5) {
				y(-1) {
					x(+0).anyGlassPane()
				}

				y(+0) {
					x(-1).anyGlassPane()
					x(+0).lodestone()
					x(+1).anyGlassPane()
				}

				y(+1) {
					x(+0).anyGlassPane()
				}
			}
		}
	}
}

object MiningLaserMultiblockTier2SideMirrored : MiningLaserMultiblockTier2() {
	override val displayName: Component get() = ofChildren(tierText, text(" Mining Laser (Side) (Mirrored)"))
	override val side = BlockFace.UP

	override val outputOffset: Vec3i = Vec3i(+1, -1, 0)

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +0, +7)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).powerInput()
				x(+1).anyPipedInventory()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).titaniumBlock()
				x(+0).titaniumBlock()
				x(+1).titaniumBlock()
			}

		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).anyGlass()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).titaniumBlock()
				x(+0).emeraldBlock()
				x(+1).titaniumBlock()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).titaniumBlock()
				x(+1).anyStairs()
			}
		}
		z(+3) {
			y(-1) {
				x(+0).ironBlock()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+0).emeraldBlock()
				x(+1).ironBlock()
			}

			y(+1) {
				x(+0).ironBlock()
			}
		}
		z(+4) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).terracottaOrDoubleSlab()
				x(+0).emeraldBlock()
				x(+1).terracottaOrDoubleSlab()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}
			z(+5) {
				y(-1) {
					x(+0).anyGlassPane()
				}

				y(+0) {
					x(-1).anyGlassPane()
					x(+0).lodestone()
					x(+1).anyGlassPane()
				}

				y(+1) {
					x(+0).anyGlassPane()
				}
			}
		}
	}
}
