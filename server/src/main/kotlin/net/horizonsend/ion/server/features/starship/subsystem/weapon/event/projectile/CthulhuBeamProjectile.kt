package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import fr.skytasul.guardianbeam.Laser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipSounds
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.CthulhuBeamStarshipWeaponMultiblockTop
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HitscanProjectile
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.Vector

class CthulhuBeamProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
) : HitscanProjectile(starship, name, loc, dir, shooter, CthulhuBeamStarshipWeaponMultiblockTop.damageType) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.cthulhuBeam ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.cthulhuBeam
	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName
	override val nearSound: StarshipSounds.SoundInfo = balancing.soundFireNear
	override val farSound: StarshipSounds.SoundInfo = balancing.soundFireFar

	override fun drawBeam() {
		val laserEnd = loc.clone().add(dir.clone().multiply(range))
		Laser.CrystalLaser(loc, laserEnd, 5, -1).durationInTicks().apply { start(IonServer) }
	}
}
