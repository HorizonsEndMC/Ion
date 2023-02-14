package net.horizonsend.ion.server.legacy.starshipweapon.secondary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.legacy.starshipweapon.projectile.SonicMissileProjectile
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class SonicMissileWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem,
	AmmoConsumingWeaponSubsystem {
	override val length: Int = IonServer.Ion.balancing.starshipWeapons.SonicMissile.length
	override val convergeDist: Double = IonServer.Ion.balancing.starshipWeapons.SonicMissile.convergeDistance
	override val extraDistance: Int = IonServer.Ion.balancing.starshipWeapons.SonicMissile.extraDistance
	override val angleRadians: Double = Math.toRadians(IonServer.Ion.balancing.starshipWeapons.SonicMissile.angleRadians)
	override val powerUsage: Int = IonServer.Ion.balancing.starshipWeapons.SonicMissile.powerusage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.Ion.balancing.starshipWeapons.SonicMissile.boostChargeNanos)
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.Ion.balancing.starshipWeapons.SonicMissile.fireCooldownNanos)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun canFire(dir: Vector, target: Vector?): Boolean {
		return starship is ActivePlayerStarship && starship.pilot!!.hasPermission("ioncore.eventweapon") && super.canFire(
			dir,
			target
		)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
		SonicMissileProjectile(starship, loc, dir, shooter).fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return ItemStack(Material.ECHO_SHARD, 2)
	}
}
