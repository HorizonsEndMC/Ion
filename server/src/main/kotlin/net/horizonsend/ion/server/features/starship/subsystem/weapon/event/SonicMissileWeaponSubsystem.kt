package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.starship.SonicMissileBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.SonicMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class SonicMissileWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem<SonicMissileBalancing>(starship, pos, face, starship.balancingManager.getSubsystemSupplier(SonicMissileWeaponSubsystem::class)),
	HeavyWeaponSubsystem,
	AmmoConsumingWeaponSubsystem,
	PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val length: Int = 8
	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		SonicMissileProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter).fire()
	}

	override fun getName(): Component {
		return Component.text("Foul Trumpets")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireMaterial(item, Material.ECHO_SHARD, 2)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 2)
	}
}
