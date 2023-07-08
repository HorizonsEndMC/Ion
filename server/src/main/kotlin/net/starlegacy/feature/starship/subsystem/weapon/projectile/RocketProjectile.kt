package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.entity.Entity

class RocketProjectile(
	starship: ActiveStarship,
	loc: Location,
	face: BlockFace,
	shooter: Controller?
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

	override val range: Double = IonServer.balancing.starshipWeapons.rocket.range
	override var speed: Double = IonServer.balancing.starshipWeapons.rocket.speed
	override val shieldDamageMultiplier: Double = IonServer.balancing.starshipWeapons.rocket.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.rocket.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.rocket.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.rocket.volume
	override val soundName: String = IonServer.balancing.starshipWeapons.rocket.soundName

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		super.impact(newLoc, block, entity)
		playCustomSound(newLoc, "starship.weapon.rocket.impact", 30)
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		super.moveVisually(oldLocation, newLocation, travel)
		speed += 5.0 * delta
	}
}
