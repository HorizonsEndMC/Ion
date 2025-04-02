package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.TorpedoBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TorpedoProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class TorpedoWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
) : TargetTrackingCannonWeaponSubsystem<TorpedoBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier()), HeavyWeaponSubsystem {
	override val boostChargeNanos: Long get() = balancing.boostChargeNanos
	override val length = 3

	override fun getMaxPerShot() = 2

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return this.face == starship.forward
	}

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		TorpedoProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter, target, aimDistance).fire()
	}

	override fun getName(): Component {
		return Component.text("Proton Torpedo")
	}
}
