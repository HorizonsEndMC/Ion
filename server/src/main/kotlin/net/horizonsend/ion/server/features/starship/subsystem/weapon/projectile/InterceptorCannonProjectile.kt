package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon.InterceptorCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class InterceptorCannonProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile(starship, name, loc, dir, shooter, InterceptorCannonStarshipWeaponMultiblock.damageType) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.interceptorCannon ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.interceptorCannon
	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val color: Color = Color.ORANGE
	override val particleThickness: Double = balancing.particleThickness
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName

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
