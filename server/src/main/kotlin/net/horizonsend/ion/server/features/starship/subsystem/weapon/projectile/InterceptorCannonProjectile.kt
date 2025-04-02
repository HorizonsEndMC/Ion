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
import kotlin.math.roundToInt

class InterceptorCannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile<IncterceptorCannonProjectileBalancing>(source, name, loc, dir, shooter, InterceptorCannonStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.ORANGE

	private val explosionSize = 4.0f

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
		val calcExplosionSize = if (starship.initialBlockCount < 700) explosionSize
		else if (starship.initialBlockCount < 1000) explosionSize * explosionCalc(starship.initialBlockCount)
		else 0.0f

		if (calcExplosionSize > 0) {
			impactLocation.createExplosion(calcExplosionSize)

			// explosionOccurred only controls the hull hitmarker sound; just use this to increase damager points on the target
			addToDamagers(impactLocation.world, impactLocation.block, shooter, calcExplosionSize.roundToInt(), false)
		}
	}

	fun explosionCalc(blockCount: Int): Float {
		// 1.0f for 700, 0.75f for 850, 0.0f for 1000
		return ((-0.0000111111 * blockCount * blockCount) + (0.0155556 * blockCount) - 4.44444).toFloat()
	}
}
