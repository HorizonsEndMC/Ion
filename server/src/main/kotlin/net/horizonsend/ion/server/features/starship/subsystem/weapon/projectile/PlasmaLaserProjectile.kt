package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipSounds
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.PlasmaCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.gayColors
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

// from left to right red - orange - yellow - green - blue - purple
class PlasmaLaserProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile(starship, name, loc, dir, shooter, PlasmaCannonStarshipWeaponMultiblock.damageType) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.plasmaCannon ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.plasmaCannon
	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val color: Color get() = if (starship?.rainbowToggle == true) gayColors.random() else shooter.color
	override val particleThickness: Double = balancing.particleThickness
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val soundName: String = balancing.soundName
	override val nearSound: StarshipSounds.SoundInfo = balancing.soundFireNear
	override val farSound: StarshipSounds.SoundInfo = balancing.soundFireFar
}
