package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.AntiAirCannonBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Location
import org.bukkit.Particle

class TestAOEProjectile(starship: Starship, shooter: Damager, firePos: Location) : AOEWave(starship, shooter, firePos) {
	override val speed: Double = 3.0
	override val separation: Double = 1.5
	override val range: Double = 100.0

	override fun handleCircumferencePosition(position: Location) {
		if (!position.isChunkLoaded) return
		position.world.spawnParticle(Particle.FLAME, position, 1, 0.0, 0.0, 0.0, 0.0)
		if (position.block.type.isAir) return
		if (starship?.contains(position.blockX, position.blockY, position.blockZ) == false) position.world.createExplosion(position, 2f)
	}

	override val balancing: StarshipWeapons.ProjectileBalancing = AntiAirCannonBalancing()
}
