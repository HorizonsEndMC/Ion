package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.starship.MiniPhaserBalancing.MiniPhaserProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.MiniPhaserStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ParticleProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class MiniPhaserProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile<MiniPhaserProjectileBalancing>(source, name, loc, dir, shooter, MiniPhaserStarshipWeaponMultiblock.damageType) {

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val offset = 0.0
		val count = 1
		val extra = 0.0
		val data = null
		location.world.spawnParticle(Particle.HAPPY_VILLAGER, x, y, z, count, offset, offset, offset, extra, data, force)
	}
}
