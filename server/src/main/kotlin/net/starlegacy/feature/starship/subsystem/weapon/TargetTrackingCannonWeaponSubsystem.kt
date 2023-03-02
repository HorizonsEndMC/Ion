package net.starlegacy.feature.starship.subsystem.weapon

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

abstract class TargetTrackingCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face) {
	override val convergeDist: Double = 0.0 // not needed since the adjusted direction is overridden
	override val angleRadians: Double = Math.toRadians(180.0) // unrestricted
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
