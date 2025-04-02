package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.starship.SonicMissileBalancing.SonicMissileProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.SonicMissileWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class SonicMissileProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<SonicMissileProjectileBalancing>(source, name, loc, dir, shooter, SonicMissileWeaponMultiblock.damageType) {

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		location.world.spawnParticle(Particle.SONIC_BOOM, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
