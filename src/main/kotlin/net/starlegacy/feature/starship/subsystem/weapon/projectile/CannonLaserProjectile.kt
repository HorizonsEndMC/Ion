package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.mcName
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class CannonLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = 200.0
	override val speed: Double = 250.0
	override val shieldDamageMultiplier: Int = 1
	override val color: Color = Color.YELLOW
	override val thickness: Double = 0.2
	override val particleThickness: Double = 0.44
	override val explosionPower: Float = 2.0f
	override val volume: Int = 10
	override val pitch: Float = 2.0f
	override val soundName: String = Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR.mcName
}
