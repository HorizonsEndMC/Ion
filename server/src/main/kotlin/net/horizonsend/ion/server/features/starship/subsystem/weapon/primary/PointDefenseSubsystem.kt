package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting.AutoTurretTarget
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.StarshipCooldownSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PointDefenseLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.sqrt

class PointDefenseSubsystem(starship: ActiveStarship, pos: Vec3i, override var face: BlockFace) :
	WeaponSubsystem(starship, pos), DirectionalSubsystem, AutoWeaponSubsystem, StarshipCooldownSubsystem {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.pointDefence.powerUsage
	override val range: Double = IonServer.balancing.starshipWeapons.pointDefence.range

	override fun getMaxPerShot(): Int {
		return (sqrt(starship.initialBlockCount.toDouble()) / 32).toInt()
	}

	private fun getFirePos() = Vec3i(pos.x + face.modX * 3, pos.y + face.modY * 3, pos.z + face.modZ * 3)

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		val origin = getFirePos().toCenterVector()
		val adjustedDir = target.clone().subtract(origin)

		val horizontalAxis = adjustedDir.clone()
		horizontalAxis.y = 0.0
		horizontalAxis.rotateAroundY(90.0)
		horizontalAxis.normalize()

		adjustedDir.rotateAroundAxis(horizontalAxis, Math.toRadians(randomDouble(-0.01, 0.01)))
		adjustedDir.rotateAroundY(Math.toRadians(randomDouble(-0.01, 0.01)))

		return adjustedDir.normalize()
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
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

	override fun autoFire(target: AutoTurretTarget<*>, dir: Vector) {
		lastFire = System.nanoTime()

		val loc = getFirePos().toCenterVector().toLocation(target.location()?.world ?: return)
		PointDefenseLaserProjectile(starship, loc, dir, range, starship.controller.damager).fire()
	}

	override fun shouldTargetRandomBlock(target: Player): Boolean {
		// TODO: only return false if there's a clear path
		return false
	}
}
