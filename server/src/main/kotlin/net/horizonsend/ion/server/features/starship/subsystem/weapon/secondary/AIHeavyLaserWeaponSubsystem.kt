package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.HeavyLaserBalancing
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class AIHeavyLaserWeaponSubsystem(
	starship: Starship,
	pos: Vec3i,
	face: BlockFace
) : TargetTrackingCannonWeaponSubsystem<HeavyLaserBalancing>(starship, pos, face, starship.balancingManager.getSubsystemSupplier(HeavyLaserWeaponSubsystem::class)), HeavyWeaponSubsystem, PermissionWeaponSubsystem {
	override val permission: String = "ion.weapon.ai"
	override val length: Int = 8

	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		HeavyLaserProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter, target, aimDistance).fire()
	}

	override fun getName(): Component {
		return Component.text("Heavy Laser")
	}
}
