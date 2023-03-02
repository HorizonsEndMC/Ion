package net.starlegacy.feature.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TorpedoProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class TorpedoWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : TargetTrackingCannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem {
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.ProtonTorpedo.boostChargeNanos)

	override fun isForwardOnly(): Boolean = IonServer.balancing.starshipWeapons.ProtonTorpedo.fowardOnly

	override val length: Int = IonServer.balancing.starshipWeapons.ProtonTorpedo.length
	override val powerUsage: Int get() = IonServer.balancing.starshipWeapons.ProtonTorpedo.powerusage
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.ProtonTorpedo.extraDistance
	override val aimDistance: Int = IonServer.balancing.starshipWeapons.ProtonTorpedo.aimDistance

	override fun getMaxPerShot() = 2

	override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
		TorpedoProjectile(starship, loc, dir, shooter, checkNotNull(target), aimDistance).fire()
	}
}
