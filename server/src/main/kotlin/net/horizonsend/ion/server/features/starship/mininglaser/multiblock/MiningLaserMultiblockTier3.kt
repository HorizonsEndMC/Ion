package net.horizonsend.ion.server.features.starship.mininglaser.multiblock

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.leftFace
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

sealed class MiningLaserMultiblockTier3 : MiningLaserMultiblock() {
	override val signText: Array<Component?> = arrayOf(
		Component.text("Mining ").color(NamedTextColor.DARK_GRAY)
			.append(Component.text("Laser").color(NamedTextColor.GREEN)),
		Component.text("Tier 3").color(NamedTextColor.AQUA),
		Component.text(""),
		Component.text("")
	)

	override val inputComputerOffset = Vec3i(0, -1, 0)
	override val maxPower: Int = 500000
	override val beamOrigin = Triple(0, 3, 1)
	override val range: Double = 150.0
	override val mineRadius = 7
	override val beamCount: Int = 6
	override val maxBroken: Int = 10
}

object MiningLaserMultiblockTier3Top : MiningLaserMultiblockTier3() {
	override fun upDownFace(): BlockFace = BlockFace.UP

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, +6, -3)

	override fun getOutput(sign: Sign): Inventory {
		val direction = sign.getFacing().oppositeFace
		return (
			sign.block.getRelative(direction)
				.getRelative(BlockFace.DOWN)
				.getRelative(direction.leftFace)
				.getState(false) as InventoryHolder
			).inventory
	}

	override fun LegacyMultiblockShape.buildStructure() {
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
				x(+0).stainedTerracotta()
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
				x(-1).stainedTerracotta()
				x(+0).anyStairs()
				x(+1).stainedTerracotta()
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
				x(-2).stainedTerracotta()
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
				x(+2).stainedTerracotta()
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
				x(-1).stainedTerracotta()
				x(+0).anyStairs()
				x(+1).stainedTerracotta()
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
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}
		}
	}
}

object MiningLaserMultiblockTier3Bottom : MiningLaserMultiblockTier3() {
	override fun upDownFace(): BlockFace = BlockFace.DOWN

	override fun getFirePointOffset(): Vec3i = Vec3i(+0, -6, -3)

	override fun getOutput(sign: Sign): Inventory {
		val direction = sign.getFacing().oppositeFace
		return (
			sign.block.getRelative(direction)
				.getRelative(BlockFace.UP)
				.getRelative(direction.leftFace)
				.getState(false) as InventoryHolder
			).inventory
	}

	override fun LegacyMultiblockShape.buildStructure() {
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
				x(+0).stainedTerracotta()
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
				x(-1).stainedTerracotta()
				x(+0).anyStairs()
				x(+1).stainedTerracotta()
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
				x(-2).stainedTerracotta()
				x(-1).anyStairs()
				x(+0).emeraldBlock()
				x(+1).anyStairs()
				x(+2).stainedTerracotta()
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
				x(-1).stainedTerracotta()
				x(+0).anyStairs()
				x(+1).stainedTerracotta()
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
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}
		}
	}
}
