package net.starlegacy.feature.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
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
	private val sound = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.soundName

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.Ion.balancing.starshipWeapons.HeavyLaser.boostChargeNanos)

	override val length: Int = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.length
	override val powerUsage: Int = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.powerusage
	override val extraDistance: Int = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.extraDistance
	override val aimDistance: Int = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.aimDistance

	override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
		checkNotNull(target)
		HeavyLaserProjectile(starship, loc, dir, shooter, target, aimDistance, sound).fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return ItemStack(Material.REDSTONE, 2)
	}
}
