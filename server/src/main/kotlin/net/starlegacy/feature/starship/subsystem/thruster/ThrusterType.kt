package net.starlegacy.feature.starship.subsystem.thruster

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.isGlass
import net.starlegacy.util.isGlassPane
import org.bukkit.Material
import org.bukkit.block.BlockFace

enum class ThrusterType(val accel: Double, val speed: Double) {
	PLASMA(1.0, .25) {
		override fun LegacyMultiblockShape.buildStructure() {
			at(0, 0, 0).thrusterBlock()
			at(0, 0, 1).type(Material.REDSTONE_BLOCK)
		}
	},
	ION(.25, .75) {
		override fun LegacyMultiblockShape.buildStructure() {
			at(0, 0, 0).thrusterBlock()
			at(0, 0, 1).type(Material.SPONGE)
		}
	},
	AFTERBURNER(1.0, 1.0) {
		override fun LegacyMultiblockShape.buildStructure() {
			at(0, 0, 0).thrusterBlock()
			at(0, 0, 1).type(Material.GOLD_BLOCK)
			at(0, 0, 2).type(Material.SPONGE)
			at(0, 0, 3).type(Material.IRON_BLOCK)
		}
	};

	fun matchesStructure(starship: ActiveStarship, x: Int, y: Int, z: Int, face: BlockFace): Boolean {
		val block = starship.serverLevel.world.getBlockAt(x, y, z)

		if (!shape.checkRequirements(block, face, loadChunks = true, particles = false)) {
			return false
		}

		var testX = x - face.modX
		var testY = y - face.modY
		var testZ = z - face.modZ

		// allow one block to be glass
		val firstType = starship.serverLevel.world.getBlockAt(testX, testY, testZ).type
		if (!firstType.isAir && !firstType.isGlass && !firstType.isGlassPane) {
			return false
		}

		while (starship.isInBounds(testX, testY, testZ)) {
			testX -= face.modX
			testY -= face.modY
			testZ -= face.modZ
			if (starship.contains(testX, testY, testZ) && !starship.serverLevel.world.getBlockAt(
					testX,
					testY,
					testZ
				).type.isAir
			) {
				return false
			}
		}

		return true
	}

	protected abstract fun LegacyMultiblockShape.buildStructure()
	private val shape by lazy { LegacyMultiblockShape().apply { buildStructure() } }
}
