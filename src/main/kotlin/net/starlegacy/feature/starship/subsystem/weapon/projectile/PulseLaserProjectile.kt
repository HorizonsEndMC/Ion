package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.mcName
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PulseLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = 140.0
	override val speed: Double = 170.0
	override val shieldDamageMultiplier: Int = 1
	override val thickness: Double = 0.2
	override val particleThickness: Double = .4
	override val explosionPower: Float = 2.0f
	override val volume: Int = 10
	override val pitch: Float = 1.5f
	override val soundName: String = Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR.mcName
}
