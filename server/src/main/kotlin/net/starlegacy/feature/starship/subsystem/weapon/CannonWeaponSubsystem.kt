package net.starlegacy.feature.starship.subsystem.weapon

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

abstract class CannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, override var face: BlockFace) :
	WeaponSubsystem(starship, pos), ManualWeaponSubsystem, DirectionalSubsystem {
	protected abstract val length: Int
	protected abstract val convergeDist: Double
	protected abstract val extraDistance: Int

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return this.face == face
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !starship.isInternallyObstructed(getFirePos(), dir)
	}

	protected fun getFireVec() = getFirePos().toCenterVector()

	protected fun getFirePos(): Vec3i {
		val distance = length + extraDistance
		return Vec3i(pos.x + face.modX * distance, pos.y + face.modY * distance, pos.z + face.modZ * distance)
	}

	protected abstract val angleRadians: Double

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		val fireDir = target.clone()
			.add(dir.clone().normalize().multiply(convergeDist))
			.subtract(getFireVec())
			.normalize()
		var yaw = atan2(-fireDir.x, fireDir.z)
		var pitch = atan(-fireDir.y / sqrt(fireDir.x.pow(2) + fireDir.z.pow(2)))
		val baseYaw = atan2(-face.modX.toDouble(), face.modZ.toDouble())
		val yawDiff = atan2(sin(yaw - baseYaw), cos(yaw - baseYaw))
		if (abs(yawDiff) > angleRadians) {
			yaw = baseYaw + sign(yawDiff) * angleRadians
		}
		pitch = pitch.coerceIn(-angleRadians, angleRadians)

		val xz = cos(pitch)
		val x = -xz * sin(yaw)
		val y = -sin(pitch)
		val z = xz * cos(yaw)
		return Vector(x, y, z)
	}

	override fun manualFire(shooter: Player, dir: Vector, target: Vector) {
		fire(getFireVec().toLocation(starship.serverLevel.world), dir, shooter, target)
	}

	override fun isIntact(): Boolean {
		for (i in 0 until length) {
			val x = pos.x + face.modX * i
			val y = pos.y + face.modY * i
			val z = pos.z + face.modZ * i
			if (starship.serverLevel.world.getBlockAt(x, y, z).type.isAir) {
				return false
			}
		}
		return true
	}

	protected abstract fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?)
}
