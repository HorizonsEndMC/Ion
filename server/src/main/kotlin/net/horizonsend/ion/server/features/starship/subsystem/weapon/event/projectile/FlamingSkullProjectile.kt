package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons.FlamingSkullCannonBalancing.FlamingSkullCannonProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.SkullThrowerStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayContainer
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TrackingLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class FlamingSkullProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile<FlamingSkullCannonProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, SkullThrowerStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.RED

	private val container = ItemDisplayContainer(
		source.getWorld(),
		5.0F,
		loc.toVector(),
		dir,
		ItemStack(Material.SKELETON_SKULL)
	)

	override fun onDespawn() {
		container.remove()
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		location.world.spawnParticle(Particle.FLAME, newLocation.x, newLocation.y, newLocation.z, 10, 3.25, 3.25, 3.25, 0.5, null, true)

		container.position = location.toVector()
		container.heading = direction.clone().multiply(-1)
		container.update()
	}
}
