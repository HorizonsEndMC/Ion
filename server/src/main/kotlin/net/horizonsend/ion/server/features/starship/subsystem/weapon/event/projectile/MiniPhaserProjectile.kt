package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipSounds
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.MiniPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class MiniPhaserProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile(starship, name, loc, dir, shooter, MiniPhaserStarshipWeaponMultiblock.damageType) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.miniPhaser ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.miniPhaser
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

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		loc.world.spawnParticle(Particle.HAPPY_VILLAGER, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
