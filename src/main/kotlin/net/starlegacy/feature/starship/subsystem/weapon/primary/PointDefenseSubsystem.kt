package net.starlegacy.feature.starship.subsystem.weapon.primary

import kotlin.math.sqrt
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.PointDefenseLaserProjectile
import net.starlegacy.util.Vec3i
import net.starlegacy.util.randomDouble
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PointDefenseSubsystem(starship: ActiveStarship, pos: Vec3i, override var face: BlockFace) :
	WeaponSubsystem(starship, pos), DirectionalSubsystem, AutoWeaponSubsystem {
	override val powerUsage: Int = 500
	override val range: Double = 120.0

	override fun getMaxPerShot(): Int {
		return (sqrt(starship.blockCount.toDouble()) / 32).toInt()
	}

	private fun getFirePos() = Vec3i(pos.x + face.modX * 3, pos.y + face.modY * 3, pos.z + face.modZ * 3)

	override fun getAdjustedDir(dir: Vector, target: Vector?): Vector {
		if (target != null) {
			val origin = getFirePos().toCenterVector()
			val adjustedDir = target.clone().subtract(origin)

			val horizontalAxis = adjustedDir.clone()
			horizontalAxis.y = 0.0
			horizontalAxis.rotateAroundY(90.0)
			horizontalAxis.normalize()

			adjustedDir.rotateAroundAxis(horizontalAxis, Math.toRadians(randomDouble(-2.5, 2.5)))
			adjustedDir.rotateAroundY(Math.toRadians(randomDouble(-2.5, 2.5)))

			return adjustedDir.normalize()
		}

		return dir
	}

	override fun canFire(dir: Vector, target: Vector?): Boolean {
		return !starship.isInternallyObstructed(getFirePos(), dir)
	}

	override fun isIntact(): Boolean {
		for (i in 0 until 3) {
			val x = pos.x + face.modX * i
			val y = pos.y + face.modY * i
			val z = pos.z + face.modZ * i
			if (starship.world.getBlockAt(x, y, z).type.isAir) {
				return false
			}
		}
		return true
	}

	override fun autoFire(target: Player, dir: Vector) {
		lastFire = System.nanoTime()

		val shooter = (starship as? ActivePlayerStarship)?.pilot
		val loc = getFirePos().toCenterVector().toLocation(target.world)
		PointDefenseLaserProjectile(starship, loc, dir, range, shooter).fire()
	}

	override fun shouldTargetRandomBlock(target: Player): Boolean {
		// TODO: only return false if there's a clear path
		return false
	}
}
