package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.ArtilleryBalancing
import net.horizonsend.ion.server.configuration.starship.PhaserBalancing.PhaserProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon.ArtilleryStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.PhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.iterateVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.lightning
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.spherePoints
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class ArtilleryProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<ArtilleryBalancing.ArtilleryProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
	override var speed: Double = balancing.speed; get() = balancing.speed

	private val yellowParticleData = Particle.DustTransition(
		Color.YELLOW,
		Color.ORANGE,
		1.0f
	)

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val origin = Location(location.world, x, y, z)

		origin.spherePoints(0.25, 3).forEach {
			it.world.spawnParticle(
				Particle.DUST_COLOR_TRANSITION,
				it.x,
				it.y,
				it.z,
				1,
				0.0,
				0.0,
				0.0,
				0.5,
				yellowParticleData,
				force
			)
		}

		origin.spherePoints(0.5, 5).forEach {
			it.world.spawnParticle(Particle.WAX_ON, it.x, it.y, it.z, 1, 0.1, 0.1, 0.1, 0.0, null, force)
		}
	}

	override fun playCustomSound(loc: Location, nearSound: SoundInfo, farSound: SoundInfo) { /* Do nothing */ }
}
