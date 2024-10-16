package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.nations.gui.skullItem
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
import java.util.UUID

class AbyssalGazeProjectile(
	starship: Starship,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile(starship, name, loc, dir, shooter, originalTarget, baseAimDistance) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship.balancing.weapons.abyssalGaze
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
		2.5F,
		loc.toVector(),
		dir,
		skullItem("skull", UUID.fromString("bf8c1907-b235-4152-84bb-a5f28b58f89c"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFmMGJjNGEyMzdiZDIwMTZjZDdlOWZhMGExNWM5ZWY3MjJlMDc5OTcwODU0NTJkNjVmM2U4ZmFkODZjM2JkNSJ9fX0=")
	)

	override fun onDespawn() {
		container.remove()
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		loc.world.spawnParticle(Particle.SOUL_FIRE_FLAME, newLocation.x, newLocation.y, newLocation.z, 3, 0.25, 0.25, 0.25, 0.05, null, true)

		container.position = loc.toVector()
		container.heading = dir.clone().multiply(-1)
		container.update()
	}
}

