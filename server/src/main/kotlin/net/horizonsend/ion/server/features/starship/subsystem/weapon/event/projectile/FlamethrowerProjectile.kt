package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.starship.FlamethrowerCannonBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.FlamethrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArcedParticleProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector

class FlamethrowerProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ArcedParticleProjectile<FlamethrowerCannonBalancing.FlamethrowerCannonProjectileBalancing>(source, name, loc, dir, shooter, FlamethrowerStarshipWeaponMultiblock.damageType) {

	companion object {
		val fire = Material.FIRE.createBlockData()
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		location.world.spawnParticle(Particle.FLAME, x, y, z, 10, 0.25, 0.25, 0.25, 0.0, null, force)
	}
}
