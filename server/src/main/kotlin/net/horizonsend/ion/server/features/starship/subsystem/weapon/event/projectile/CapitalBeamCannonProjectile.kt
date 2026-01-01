package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.starship.CapitalCannonBalancing.CapitalCannonProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.CapitalBeamStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.helixAroundVector
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class CapitalBeamCannonProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<CapitalCannonProjectileBalancing>(source, name, loc, dir, shooter, CapitalBeamStarshipWeaponMultiblock.damageType) {
	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		val vector = direction.clone().normalize().multiply(travel)

		for (location in helixAroundVector(oldLocation, vector, 5.0, 150, wavelength = 1.0)) {
			location.world.spawnParticle(
				Particle.BLOCK_MARKER,
				location,
				0,
				0.5,
				0.5,
				0.5,
				0.0,
				FlamethrowerProjectile.fire,
				true
			)
		}
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		TODO("Not yet implemented")
	}
}
