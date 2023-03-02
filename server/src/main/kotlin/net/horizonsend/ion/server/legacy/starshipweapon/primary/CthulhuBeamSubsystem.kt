package net.horizonsend.ion.server.legacy.starshipweapon.primary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.legacy.starshipweapon.projectile.CthulhuBeamProjectile
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.randomDouble
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.sqrt

class CthulhuBeamSubsystem(starship: ActiveStarship, pos: Vec3i, override var face: BlockFace) :
	WeaponSubsystem(starship, pos), DirectionalSubsystem, AutoWeaponSubsystem {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.cthulhuBeam.powerUsage
	override val range: Double = IonServer.balancing.starshipWeapons.cthulhuBeam.range

	override fun getMaxPerShot(): Int {
		return (sqrt(starship.initialBlockCount.toDouble()) / 32).toInt()
	}

	private fun getFirePos() = Vec3i(pos.x + face.modX * 5, pos.y + face.modY * 5, pos.z + face.modZ * 5)

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
			if (starship.serverLevel.world.getBlockAt(x, y, z).type.isAir) {
				return false
			}
		}
		return true
	}

	override fun autoFire(target: Player, dir: Vector) {
		lastFire = System.nanoTime()

		val shooter = (starship as? ActivePlayerStarship)?.pilot
		val loc = getFirePos().toCenterVector().toLocation(target.world)
		CthulhuBeamProjectile(starship, loc, dir, shooter).fire()
	}

	override fun shouldTargetRandomBlock(target: Player): Boolean {
		// TODO: only return false if there's a clear path
		return true
	}
}
