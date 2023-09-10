package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class HeavyLaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : TargetTrackingCannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem,
	AmmoConsumingWeaponSubsystem {
	private val sound = IonServer.balancing.starshipWeapons.heavyLaser.soundName

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.heavyLaser.boostChargeNanos)

	override val length: Int = IonServer.balancing.starshipWeapons.heavyLaser.length
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.heavyLaser.powerUsage
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.heavyLaser.extraDistance
	override val aimDistance: Int = IonServer.balancing.starshipWeapons.heavyLaser.aimDistance

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector?) {
		checkNotNull(target)
		HeavyLaserProjectile(starship, loc, dir, shooter, target, aimDistance, sound).fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return ItemStack(Material.REDSTONE, 2)
	}
}
