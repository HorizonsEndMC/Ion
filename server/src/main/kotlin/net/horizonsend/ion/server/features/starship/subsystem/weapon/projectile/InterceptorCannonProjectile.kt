package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.InterceptorCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.roundToInt

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
