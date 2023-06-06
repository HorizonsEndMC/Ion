package net.starlegacy.feature.starship.movement

import io.papermc.paper.entity.TeleportFlag
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.feature.misc.CustomBlocks
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.thruster.ThrustData
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class RotationMovement(starship: ActiveStarship, val clockwise: Boolean) : StarshipMovement(starship) {
	private val origin get() = starship.centerOfMass
	private val nmsRotation = if (clockwise) Rotation.CLOCKWISE_90 else Rotation.COUNTERCLOCKWISE_90
	private val theta: Double = if (clockwise) 90.0 else -90.0
	private val cosTheta: Double = cos(Math.toRadians(theta))
	private val sinTheta: Double = sin(Math.toRadians(theta))

	override fun blockDataTransform(blockData: BlockState): BlockState =
		if (CustomBlocks[blockData] == null) {
			blockData.rotate(nmsRotation)
		} else {
			blockData
		}

	override fun displaceX(oldX: Int, oldZ: Int): Int {
		val offsetX = oldX - origin.x
		val offsetZ = oldZ - origin.z
		return (offsetX.toDouble() * cosTheta - offsetZ.toDouble() * sinTheta).roundToInt() + origin.x
	}

	override fun displaceY(oldY: Int): Int = oldY

	override fun displaceZ(oldZ: Int, oldX: Int): Int {
		val offsetX = oldX - origin.x
		val offsetZ = oldZ - origin.z
		return (offsetX.toDouble() * sinTheta + offsetZ.toDouble() * cosTheta).roundToInt() + origin.z
	}

	override fun displaceLocation(oldLocation: Location): Location {
		val centerX = origin.x + 0.5
		val centerZ = origin.z + 0.5
		val offsetX = oldLocation.x - centerX
		val offsetZ = oldLocation.z - centerZ
		val newX = offsetX * cosTheta - offsetZ * sinTheta
		val newZ = offsetX * sinTheta + offsetZ * cosTheta
		val newLocation = Location(oldLocation.world, newX + centerX, oldLocation.y, newZ + centerZ)
		newLocation.yaw += theta.toFloat()
		newLocation.world = newWorld ?: newLocation.world
		return newLocation
	}

	override fun movePassenger(passenger: Entity) {
		val newLoc = displaceLocation(passenger.location)
		if (passenger is Player) {
			newLoc.pitch = passenger.location.pitch
			newLoc.yaw += passenger.location.yaw
			passenger.teleport(
				newLoc,
				TeleportCause.PLUGIN,
				*TeleportFlag.Relative.values(),
				TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY
			)
		} else {
			passenger.teleport(newLoc)
		}
	}

	private fun rotateBlockFace(blockFace: BlockFace): BlockFace {
		return if (clockwise) {
			when (blockFace) {
				BlockFace.NORTH -> BlockFace.EAST
				BlockFace.EAST -> BlockFace.SOUTH
				BlockFace.SOUTH -> BlockFace.WEST
				BlockFace.WEST -> BlockFace.NORTH
				else -> blockFace
			}
		} else {
			when (blockFace) {
				BlockFace.NORTH -> BlockFace.WEST
				BlockFace.WEST -> BlockFace.SOUTH
				BlockFace.SOUTH -> BlockFace.EAST
				BlockFace.EAST -> BlockFace.NORTH
				else -> blockFace
			}
		}
	}

	override fun onComplete() {
		starship.calculateHitbox()
		for (subsystem in starship.subsystems) {
			if (subsystem is DirectionalSubsystem) {
				subsystem.face = rotateBlockFace(subsystem.face)
			}
		}
		// rotate all the thruster data
		val thrusterMap: MutableMap<BlockFace, ThrustData> = starship.thrusterMap
		// creates a new map with the updated faces, then overwrites the old map
		// since the map contains every possible face, it overwrites every face
		thrusterMap.putAll(thrusterMap.mapKeys { (face: BlockFace, _) -> rotateBlockFace(face) })

		starship.forward = rotateBlockFace(starship.forward)

		if (starship is ActivePlayerStarship) {
			val dir = starship.cruiseData.targetDir
			if (dir != null) {
				val newX = dir.x * cosTheta - dir.z * sinTheta
				val newZ = dir.x * sinTheta + dir.z * cosTheta
				starship.cruiseData.targetDir = Vector(newX, dir.y, newZ)
			}
		}
	}
}
