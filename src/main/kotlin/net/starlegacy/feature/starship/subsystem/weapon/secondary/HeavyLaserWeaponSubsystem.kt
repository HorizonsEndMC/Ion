package net.starlegacy.feature.starship.subsystem.weapon.secondary

import java.util.concurrent.TimeUnit
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

class HeavyLaserWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : TargetTrackingCannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem,
	AmmoConsumingWeaponSubsystem {
	private val sound = "starship.weapon.heavy_laser.single.shoot"

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5L)

	override val length: Int = 8
	override val powerUsage: Int = 30000
	override val extraDistance: Int = 1
	override val aimDistance: Int = 10

	override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
		checkNotNull(target)
		HeavyLaserProjectile(starship, loc, dir, shooter, target, aimDistance, sound).fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return ItemStack(Material.REDSTONE, 2)
	}
}
