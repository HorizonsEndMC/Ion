package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipBoidProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import kotlin.math.pow

abstract class BoidProjectile<B : StarshipBoidProjectileBalancing>(
    source: ProjectileSource,
    name: Component,
    loc: Location,
    dir: Vector,
    val target: Vector,
    shooter: Damager,
    val otherBoids: MutableList<BoidProjectile<*>>,
    damageType: DamageType
) : SimpleProjectile<B>(source, name, loc, dir, shooter, damageType) {
    val separationDistance: Double get() = balancing.separationDistance
    val separationFactor: Double get() = balancing.separationFactor
    val visibleDistance: Double get() = balancing.visibleDistance
    val alignFactor: Double get() = balancing.alignFactor
    val centerFactor: Double get() = balancing.centerFactor
    val minSpeedFactor: Double get() = balancing.minSpeedFactor
    val maxSpeedFactor: Double get() = balancing.maxSpeedFactor
    val originalDirectionFactor: Double get() = balancing.originalDirectionFactor

    val originalDir: Vector

    init {
        otherBoids.add(this)
        originalDir = dir.clone()
    }

    fun calculateBoidDirection(oldDirection: Vector): Vector {
        var neighboringBoids = 0
        val separationVector = Vector(0.0, 0.0, 0.0)
        val alignVector = Vector(0.0, 0.0, 0.0)
        val averagePosition = Vector(0.0, 0.0, 0.0)

        for (boid in otherBoids) {
            // Ensure that this boid does not try to modify itself
            if (boid === this) continue

            if (location.distanceSquared(boid.location) < separationDistance * separationDistance) {
                // Steer away from other boids
                separationVector.x += location.x - boid.location.x
                separationVector.y += location.y - boid.location.y
                separationVector.z += location.z - boid.location.z
            } else if (location.distanceSquared(boid.location) < visibleDistance * visibleDistance) {
                // Align with nearby boids
                neighboringBoids++
                alignVector.x += boid.direction.x
                alignVector.y += boid.direction.y
                alignVector.z += boid.direction.z

                // Used to get average center of nearby boids
                averagePosition.add(boid.location.toVector())
            }
        }

        if (neighboringBoids > 0) {
            // Average alignment vector and center vector
            alignVector.multiply(1 / neighboringBoids.toDouble())
            averagePosition.multiply(1 / neighboringBoids.toDouble())
        }

        val newDirection = oldDirection.clone()
            .add(separationVector.clone().multiply(separationFactor))
            .add(alignVector.clone().subtract(oldDirection).multiply(alignFactor))
            .add(averagePosition.clone().subtract(location.toVector()).multiply(centerFactor))
            .add(target.clone().subtract(location.toVector()).normalize().multiply(originalDirectionFactor))
        if (newDirection.lengthSquared() > maxSpeedFactor.pow(2)) {
            newDirection.normalize().multiply(maxSpeedFactor)
        } else if (newDirection.lengthSquared() < (minSpeedFactor).pow(2)) {
            newDirection.normalize().multiply(minSpeedFactor)
        }

        return newDirection
    }
}
