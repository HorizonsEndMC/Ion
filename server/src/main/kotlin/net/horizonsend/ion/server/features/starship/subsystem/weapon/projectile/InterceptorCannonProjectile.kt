package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.InterceptorCannonBalancing.IncterceptorCannonProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.InterceptorCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class InterceptorCannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile<IncterceptorCannonProjectileBalancing>(source, name, loc, dir, shooter, InterceptorCannonStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.ORANGE

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		if (starship.initialBlockCount < 1000) {
			impactLocation.createExplosion(12.0f)
		}
	}
}
