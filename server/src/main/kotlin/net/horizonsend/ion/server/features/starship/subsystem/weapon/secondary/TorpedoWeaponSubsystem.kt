package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
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
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.protonTorpedo.boostChargeNanos)

	override fun isForwardOnly(): Boolean = IonServer.balancing.starshipWeapons.protonTorpedo.fowardOnly

	override val length: Int = IonServer.balancing.starshipWeapons.protonTorpedo.length
	override val powerUsage: Int get() = IonServer.balancing.starshipWeapons.protonTorpedo.powerUsage
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.protonTorpedo.extraDistance
	override val aimDistance: Int = IonServer.balancing.starshipWeapons.protonTorpedo.aimDistance

	override fun getMaxPerShot() = 2

	override fun fire(loc: Location, dir: Vector, shooter: Controller, target: Vector?) {
		TorpedoProjectile(starship, loc, dir, shooter, checkNotNull(target), aimDistance).fire()
	}
}
