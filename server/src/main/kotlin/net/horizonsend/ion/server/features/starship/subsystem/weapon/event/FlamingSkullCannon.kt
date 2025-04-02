package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.starship.FlamingSkullCannonBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.FlamingSkullProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class FlamingSkullCannon(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem<FlamingSkullCannonBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier()), HeavyWeaponSubsystem, PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val boostChargeNanos: Long = balancing.boostChargeNanos
	override val length: Int = 4

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		FlamingSkullProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter, target, 10).fire()
	}

	override fun getName(): Component {
		return Component.text("Screaming Kin")
	}
}
