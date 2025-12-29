package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.HeavyLogisticsCannonBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LogisticsCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class HeavyLogisticsCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
) : CannonWeaponSubsystem<HeavyLogisticsCannonBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(HeavyLogisticsCannonWeaponSubsystem::class)) {
    override var fireCooldownNanos: Long = balancing.fireCooldownNanos
	override val length: Int = 3
	override fun getMaxPerShot(): Int = balancing.maxPerShot


	override fun getName(): Component {
		return Component.text("Heavy Logistics Cannon")
	}

	override fun fire(
		loc: Location,
		dir: Vector,
		shooter: Damager,
		target: Vector
	) {
		LogisticsCannonProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter.color, balancing.projectile, shooter, DamageType.GENERIC)
	}
}

