package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.subsystem.RestrictedSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.MiniPhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.MiniPhaserProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class MiniPhaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
	AmmoConsumingWeaponSubsystem,
	PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val length: Int = IonServer.balancing.starshipWeapons.miniPhaser.length
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.miniPhaser.convergeDistance
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.miniPhaser.extraDistance
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.miniPhaser.angleRadians)
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.miniPhaser.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.miniPhaser.fireCooldownNanos)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun canFire(dir: Vector, target: Vector): Boolean {
		if (!canUse(starship.controller)) return false

		return super.canFire(dir, target)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector?) {
		MiniPhaserProjectile(starship, loc, dir, shooter).fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return ItemStack(Material.EMERALD, 1)
	}
}
