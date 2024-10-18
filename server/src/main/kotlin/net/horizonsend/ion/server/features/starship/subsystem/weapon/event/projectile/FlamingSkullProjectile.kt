package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayContainer
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TrackingLaserProjectile
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class FlamingSkullProjectile(
	starship: Starship,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile(starship, name, loc, dir, shooter, originalTarget, baseAimDistance) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship.balancing.weapons.skullThrower
	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName

	override val color: Color = Color.RED
	override val maxDegrees: Double = 10.0
	override val particleThickness: Double = 0.0

	private val container = ItemDisplayContainer(
		starship.world,
		5.0F,
		loc.toVector(),
		dir,
		ItemStack(Material.SKELETON_SKULL)
	)

	override fun onDespawn() {
		container.remove()
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		loc.world.spawnParticle(Particle.FLAME, newLocation.x, newLocation.y, newLocation.z, 10, 3.25, 3.25, 3.25, 0.5, null, true)

		container.position = loc.toVector()
		container.heading = dir.clone().multiply(-1)
		container.update()
	}
}
