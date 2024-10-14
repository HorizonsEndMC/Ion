package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayContainer
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SimpleProjectile
import net.horizonsend.ion.server.miscellaneous.utils.average
import net.horizonsend.ion.server.miscellaneous.utils.nearestPointToVector
import net.kyori.adventure.text.Component
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class SkullTornadoProjectile(
	starship: Starship,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	private val originalTarget: Vector,
) : SimpleProjectile(starship, name, loc, dir, shooter) {
	private lateinit var getTargetOrigin: () -> Vector
	private lateinit var targetBase: Vector
	override val balancing: StarshipWeapons.ProjectileBalancing = starship.balancing.weapons.skullThrower

	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier: Double = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val soundName: String = balancing.soundName

	private fun calculateTarget() = targetBase.clone().add(getTargetOrigin())

	override fun fire() {
		processTarget()

		super.fire()
	}

	private val container = ItemDisplayContainer(
		starship.world,
		10.0F,
		loc.toVector(),
		dir,
		ItemStack(Material.SKELETON_SKULL)
	)

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		container.position = loc.toVector()
		container.heading = dir.clone().multiply(-1)
		container.update()
	}

	override fun onDespawn() {
		container.remove()
	}

	private fun processTarget() {
		val targetShip = ActiveStarships.findByBlock(originalTarget.toLocation(loc.world))
		getTargetOrigin = {
			targetShip?.centerOfMass?.toCenterVector() ?: originalTarget
		}
		targetBase = originalTarget.clone().subtract(getTargetOrigin())
	}

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds
		val adjustedDir = calculateAdjustedDirection()

		val predictedNewLoc = loc.clone().add(adjustedDir)
		if (!predictedNewLoc.isChunkLoaded) return onDespawn()

		val result: RayTraceResult? = loc.world.rayTrace(loc, adjustedDir, delta * speed, FluidCollisionMode.NEVER, true, 0.1) { it.type != EntityType.ITEM_DISPLAY }
		val newLoc = result?.hitPosition?.toLocation(loc.world) ?: predictedNewLoc
		val travel = loc.distance(newLoc)

		moveVisually(loc, newLoc, travel)

		var impacted = false

		if (result != null) {
			impacted = tryImpact(result, newLoc)
		}

		loc = newLoc

		distance += travel

		if (impacted) {
			onImpact()
			return onDespawn()
		}

		if (distance >= range) {
			return onDespawn()
		}

		lastTick = System.nanoTime()
		reschedule()
	}

	// Dynamically creates vector to orbit target
	fun calculateAdjustedDirection(): Vector {
		val targetPos = getTargetOrigin()

		val oldDir = dir.clone()
		// Get closest intersect point to move towards
		val closestIntersect = nearestPointToVector(loc.toVector(), oldDir, targetPos)

		// Ideal orbit distance based on time
		val orbitDistance = getOrbitDistance()
		// Vector to the closest intersect, but set at the correct orbit distance
		val vectorToIntersect = closestIntersect.clone().subtract(targetPos).normalize().multiply(orbitDistance)

		// Average real and ideal intersects for smooth interpolation
		val halfway = setOf(vectorToIntersect, closestIntersect).average()

		// Move towards the average at the current speed
		return halfway.subtract(loc.toVector()).multiply(speed * delta)
	}

	fun getOrbitDistance(): Double = when (TimeUnit.NANOSECONDS.toSeconds(lastTick - firedAtNanos)) {
		in 0..10 -> 70.0
		else -> 0.0
	}
}
