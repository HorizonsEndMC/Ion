package net.horizonsend.ion.server.features.starship.movement

import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.movement.TranslationAccessor.RotationTranslation
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.thruster.ThrustData
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class RotationMovement(starship: ActiveStarship, val clockwise: Boolean) : StarshipMovement(starship), TranslationAccessor by RotationTranslation(null, if (clockwise) 90.0 else 270.0, starship::centerOfMass) {
	private val theta: Double = if (clockwise) 90.0 else -90.0
	private val cosTheta: Double = cos(Math.toRadians(theta))
	private val sinTheta: Double = sin(Math.toRadians(theta))

	override fun movePassenger(passenger: Entity) {
		val newLoc = displaceLocation(passenger.location)
		if (passenger is Player) {
			newLoc.pitch = passenger.location.pitch
			newLoc.yaw += passenger.location.yaw

			passenger.teleport(
				newLoc,
				TeleportCause.PLUGIN,
				*TeleportFlag.Relative.values(),
				TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY,
				TeleportFlag.EntityState.RETAIN_VEHICLE
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
		starship.rotation += theta

		val dir = starship.cruiseData.targetDir
		if (dir != null) {
			val newX = dir.x * cosTheta - dir.z * sinTheta
			val newZ = dir.x * sinTheta + dir.z * cosTheta
			starship.cruiseData.targetDir = Vector(newX, dir.y, newZ)
		}
	}
}
