package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons.InterceptorCannonBalancing.IncterceptorCannonProjectileBalancing
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

	private val explosionSize = 12.0f

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		if (starship.initialBlockCount < 700) {
			impactLocation.createExplosion(explosionSize)
		}
		else if (starship.initialBlockCount < 1400) {
			impactLocation.createExplosion(explosionSize * explosionCalc(starship.initialBlockCount))
		}
	}

	fun explosionCalc(blockCount: Int): Float {
		// 1.0f for 700, 0.98f for 800, 0.81f for 1000, 0.49f for 1200
		return ((-0.00000204082 * blockCount * blockCount) + (0.00285714 * blockCount)).toFloat()
	}
}
