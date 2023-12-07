package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class AIHeavyLaserWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : TargetTrackingCannonWeaponSubsystem(starship, pos, face), HeavyWeaponSubsystem, PermissionWeaponSubsystem {
	override val permission: String = "ion.weapon.ai"
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.heavyLaser
	val sound = balancing.soundName

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)

	override val length: Int = balancing.length
	override val powerUsage: Int = balancing.powerUsage
	override val extraDistance: Int = balancing.extraDistance
	override val aimDistance: Int = balancing.aimDistance

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector?) {
		checkNotNull(target)
		HeavyLaserProjectile(starship, loc, dir, shooter, target, aimDistance, sound).fire()
	}
}
