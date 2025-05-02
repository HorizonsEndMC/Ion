package net.horizonsend.ion.server.features.starship.subsystem.weapon

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

abstract class TargetTrackingCannonWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face) {
	override val convergeDist: Double = 0.0 // not needed since the adjusted direction is overridden
	override val angleRadiansHorizontal: Double = Math.toRadians(180.0) // unrestricted
	override val angleRadiansVertical: Double = Math.toRadians(180.0)
	protected abstract val aimDistance: Int

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return target != null &&
			!isTargetObstructed(target) &&
			isPathObstructed(dir)
	}

	private fun isPathObstructed(dir: Vector) =
		!starship.isInternallyObstructed(getFirePos(), dir, aimDistance)

	private fun isTargetObstructed(target: Vector): Boolean {
		val firePos = super.getFirePos()
		val origin = firePos + Vec3i(face.modX * aimDistance, face.modY * aimDistance, face.modZ * aimDistance)
		val direction = target.clone().subtract(origin.toVector()).normalize()
		return starship.isInternallyObstructed(origin, direction)
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return this.face.direction
	}
}
