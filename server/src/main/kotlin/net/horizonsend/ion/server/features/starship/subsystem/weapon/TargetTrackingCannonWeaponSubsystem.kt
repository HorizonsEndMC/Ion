package net.horizonsend.ion.server.features.starship.subsystem.weapon

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingWeaponBalancing
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.function.Supplier

abstract class TargetTrackingCannonWeaponSubsystem<T : StarshipTrackingWeaponBalancing<*>>(
    starship: Starship,
    pos: Vec3i,
    face: BlockFace,
	balancingSupplier: Supplier<T>
) : CannonWeaponSubsystem<T>(starship, pos, face, balancingSupplier) {
	override val convergeDist: Double = 0.0
	override val angleRadiansHorizontal: Double = 180.0
	override val angleRadiansVertical: Double = 180.0

	protected open val aimDistance: Int = balancing.aimDistance

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !isTargetObstructed(target) && isPathObstructed(dir)
	}

	private fun isPathObstructed(dir: Vector) = !starship.isInternallyObstructed(getFirePos(), dir, aimDistance)

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
