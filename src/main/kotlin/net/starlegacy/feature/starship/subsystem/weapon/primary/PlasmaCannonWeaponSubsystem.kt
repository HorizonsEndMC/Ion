package net.starlegacy.feature.starship.subsystem.weapon.primary

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PlasmaCannonWeaponSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face) {
	override val powerUsage: Int = 2500
	override val length: Int = 3
	override val angleRadians: Double = Math.toRadians(15.0)
	override val convergeDist: Double = 10.0
	override val extraDistance: Int = 1

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return true
	}

	override fun isForwardOnly(): Boolean = true

	override fun getMaxPerShot(): Int {
		return 1
	}

	override fun fire(
		loc: Location,
		dir: Vector,
		shooter: Player,
		target: Vector?
	) {
		PlasmaLaserProjectile(starship, loc, dir, shooter).fire()
	}
}
