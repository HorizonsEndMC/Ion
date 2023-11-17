package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TorpedoProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class TorpedoWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : TargetTrackingCannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.protonTorpedo
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)

	override fun isForwardOnly(): Boolean = balancing.forwardOnly

	override val length: Int = balancing.length
	override val powerUsage: Int get() = balancing.powerUsage
	override val extraDistance: Int = balancing.extraDistance
	override val aimDistance: Int = balancing.aimDistance

	override fun getMaxPerShot() = 2

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return this.face == starship.forward
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector?) {
		TorpedoProjectile(starship, loc, dir, shooter, checkNotNull(target), aimDistance).fire()
	}
}
