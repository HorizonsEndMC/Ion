package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.HeavyLaserBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class HeavyLaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : TargetTrackingCannonWeaponSubsystem<HeavyLaserBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(HeavyLaserWeaponSubsystem::class)), HeavyWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val length: Int = 8

	override val boostChargeNanos: Long get() = balancing.boostChargeNanos
	override val aimDistance: Int get() = balancing.aimDistance

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		HeavyLaserProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter, target, aimDistance).fire()
	}

	override fun getName(): Component {
		return Component.text("Heavy Laser")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireMaterial(item, Material.REDSTONE, 2)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 2)
	}
}
