package net.horizonsend.ion.server.features.starship.mininglaser.multiblock

import net.kyori.adventure.text.Component
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import org.bukkit.block.BlockFace

sealed class MiningLaserMultiblockTier1 : MiningLaserMultiblock() {
	override val signText: Array<Component?> = arrayOf(
		Component.text("Tier 1"),
		Component.text("Mining Laser"),
		Component.text(""),
		Component.text("")
	)

	override val axis = Triple(0, 3, 1)
	override val range: Int = 30
	override val circleRadius = 3
}

object PMiningLaserMultiblockTier1Top : MiningLaserMultiblockTier1() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.UP

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
			}
			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(+0) {
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
			}

			y(+1) {
				x(-1).sponge()
				x(+0).diamondBlock()
				x(+1).sponge()
			}
			y(+2) {
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
			}
		}

		z(+2) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
			}
			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}
}

object PMiningLaserMultiblockTier1Bottom : MiningLaserMultiblockTier1() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.DOWN

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
			}
			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(+0) {
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
			}

			y(+1) {
				x(-1).sponge()
				x(+0).diamondBlock()
				x(+1).sponge()
			}
			y(+2) {
				x(-1).sponge()
				x(+0).sponge()
				x(+1).sponge()
			}
		}

		z(+2) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(-1).anyGlassPane()
				x(+0).anyGlassPane()
				x(+1).anyGlassPane()
			}
			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}
}
