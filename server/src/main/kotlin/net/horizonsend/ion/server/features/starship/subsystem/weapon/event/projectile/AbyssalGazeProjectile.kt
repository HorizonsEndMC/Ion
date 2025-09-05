package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.starship.AbyssalGazeBalancing.AbyssalGazeProjectileBalancing
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.client.display.teleportDuration
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.GazeStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TrackingLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import java.util.UUID

class AbyssalGazeProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile<AbyssalGazeProjectileBalancing>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, GazeStarshipWeaponMultiblock.damageType) {
	override val color: Color = Color.RED

	private val container = ItemDisplayContainer(
		source.getWorld(),
		1.5F,
		loc.toVector(),
		dir,
		skullItem("skull", UUID.fromString("bf8c1907-b235-4152-84bb-a5f28b58f89c"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFmMGJjNGEyMzdiZDIwMTZjZDdlOWZhMGExNWM5ZWY3MjJlMDc5OTcwODU0NTJkNjVmM2U4ZmFkODZjM2JkNSJ9fX0="),
		interpolation = 2
	).apply {
		getEntity().transformationInterpolationDuration = 2
		getEntity().teleportDuration = 2
	}

	override fun onDespawn() {
		container.remove()
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		location.world.spawnParticle(Particle.SOUL_FIRE_FLAME, newLocation.x, newLocation.y, newLocation.z, 3, 0.25, 0.25, 0.25, 0.05, null, true)

		container.position = location.toVector()
		container.heading = direction.clone().multiply(-1)
		container.update()
	}
}

