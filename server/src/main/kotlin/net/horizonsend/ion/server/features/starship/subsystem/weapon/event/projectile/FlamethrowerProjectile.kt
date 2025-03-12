package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event.FlamethrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArcedParticleProjectile
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector

class FlamethrowerProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ArcedParticleProjectile(starship, name, loc, dir, shooter, FlamethrowerStarshipWeaponMultiblock.damageType) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.flameThrower ?: ConfigurationFiles.starshipBalancing().nonStarshipFired.flameThrower
	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName

	override val decelerationAmount: Double = 0.05
	override val gravityMultiplier: Double = 0.05

	companion object {
		val fire = Material.FIRE.createBlockData()
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		loc.world.spawnParticle(Particle.FLAME, x, y, z, 10, 0.25, 0.25, 0.25, 0.0, null, force)
	}
}
