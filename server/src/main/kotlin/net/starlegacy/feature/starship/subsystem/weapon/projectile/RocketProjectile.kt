package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class RocketProjectile(
	starship: ActiveStarship,
	loc: Location,
	face: BlockFace,
	shooter: Player?
) : BlockProjectile(starship, loc, face.direction, shooter) {
	companion object {
		private fun getBlockData(
			down: Boolean,
			east: Boolean,
			north: Boolean,
			south: Boolean,
			up: Boolean,
			west: Boolean
		): BlockData {
			return Material.BROWN_MUSHROOM_BLOCK.createBlockData { blockData ->
				check(blockData is MultipleFacing)
				blockData.setFace(BlockFace.DOWN, down)
				blockData.setFace(BlockFace.EAST, east)
				blockData.setFace(BlockFace.NORTH, north)
				blockData.setFace(BlockFace.SOUTH, south)
				blockData.setFace(BlockFace.UP, up)
				blockData.setFace(BlockFace.WEST, west)
			}
		}

		private val blockMaps = mapOf(
			BlockFace.SOUTH to mapOf(
				Vec3i(0, 0, -1) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = false,
					up = false,
					west = true
				)
			),
			BlockFace.NORTH to mapOf(
				Vec3i(0, 0, 1) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = false,
					up = true,
					west = false
				)
			),
			BlockFace.EAST to mapOf(
				Vec3i(-1, 0, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = false,
					up = true,
					west = true
				)
			),
			BlockFace.WEST to mapOf(
				Vec3i(1, 0, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = true,
					up = false,
					west = false
				)
			),
			BlockFace.DOWN to mapOf(
				Vec3i(0, 1, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = true,
					up = false,
					west = true
				)
			),
			BlockFace.UP to mapOf(
				Vec3i(0, -1, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = true,
					up = true,
					west = true
				)
			)
		)
	}

	override val blockMap: Map<Vec3i, BlockData> = blockMaps.getValue(face)

	override val range: Double = 300.0
	override var speed: Double = 5.0
	override val shieldDamageMultiplier: Int = 5
	override val thickness: Double = 1.0
	override val explosionPower: Float = 30.0f
	override val volume: Int = 10
	override val soundName: String = "starship.weapon.rocket.shoot"

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		super.impact(newLoc, block, entity)
		playCustomSound(newLoc, "starship.weapon.rocket.impact", 30)
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		super.moveVisually(oldLocation, newLocation, travel)
		speed += 5.0 * delta
	}
}
