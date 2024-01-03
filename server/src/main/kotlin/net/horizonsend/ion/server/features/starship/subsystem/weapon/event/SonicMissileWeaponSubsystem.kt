package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.subsystem.RestrictedSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.SonicMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SonicMissileProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class SonicMissileWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
	HeavyWeaponSubsystem,
	AmmoConsumingWeaponSubsystem,
	PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val length: Int = IonServer.balancing.starshipWeapons.sonicMissile.length
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.sonicMissile.convergeDistance
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.sonicMissile.extraDistance
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.sonicMissile.angleRadians)
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.sonicMissile.powerUsage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.sonicMissile.boostChargeNanos)
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.sonicMissile.fireCooldownNanos)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun canFire(dir: Vector, target: Vector): Boolean {
		if (!canUse(starship.controller)) return false

		return super.canFire(dir, target)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector?) {
		SonicMissileProjectile(starship, loc, dir, shooter).fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return ItemStack(Material.ECHO_SHARD, 2)
	}

	override fun canUse(controller: Controller): Boolean {
		val permission = "ion.core.eventweapon.miniphaser"

		if ((controller as? PlayerController)?.player?.hasPermission(permission) == true) return  true

		return controller is AIController
	}
}
