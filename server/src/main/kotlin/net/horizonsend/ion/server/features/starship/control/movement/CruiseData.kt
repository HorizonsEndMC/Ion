package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.server.features.nations.DominionTerritoryBuffTypes
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.status_effects.StarshipStatusEffectTypes
import org.bukkit.util.Vector
import kotlin.math.min

class CruiseData(
    val starship: ActiveControlledStarship,
    var velocity: Vector = Vector(),
    var targetSpeed: Int = 0,
    var targetDir: Vector? = null,
    var accel: Double = 0.0
) {
    var lastBlockCount = starship.initialBlockCount

    fun accelerate(maxSpeed: Int, thrusterPower: Double) {
        val speedModifier = starship.getStrongestActiveStatusEffectFromType(StarshipStatusEffectTypes.CRUISE_SPEED)?.strength ?: 0.0
        val slowModifier = starship.getStrongestActiveStatusEffectFromType(StarshipStatusEffectTypes.CRUISE_SLOW)?.strength ?: 0.0
        /*
        val nationCruiseModifier = starship.playerPilot?.let { player ->
            val cruiseBuffActive = NationBuffTypes.isEffectActive(player, NationBuffTypes.CRUISE_SPEED)
            if (cruiseBuffActive) {
                NationBuffTypes.CRUISE_SPEED.value
            } else 0.0
        } ?: 0.0
         */

        val dominionBpsModifier = starship.playerPilot?.let { player ->
            if (DominionTerritoryBuffTypes.isEffectActive(player, DominionTerritoryBuffTypes.SPEED))
                DominionTerritoryBuffTypes.SPEED.value
            else 0.0
        } ?: 0.0

        val limitedTarget = (targetSpeed * (1 + speedModifier) * (1 - slowModifier) * starship.disabledThrusterRatio + /*nationCruiseModifier + */dominionBpsModifier * starship.directCruiseSpeedMultiplier).toInt()

        val dir = this.targetDir ?: Vector()
        val speed = if (maxSpeed <= 0) limitedTarget else min(limitedTarget, maxSpeed)
        val newVelocity = dir.clone().multiply(speed)

        moveTowards(velocity, newVelocity, getRealAccel(thrusterPower) * StarshipCruising.SECONDS_PER_CRUISE)

        if (velocity.x.isNaN()) velocity.x = 0.0
		if (velocity.y.isNaN()) velocity.y = 0.0
		if (velocity.z.isNaN()) velocity.z = 0.0
	}

    // multiplied by power percent and rounded to the nearest hundredth
    fun getRealAccel(thrusterPower: Double): Double {
        /*
        val nationAccelerationModifier = starship.playerPilot?.let { player ->
            val accelerationBuffActive = NationBuffTypes.isEffectActive(player, NationBuffTypes.ACCELERATION)
            if (accelerationBuffActive) {
                NationBuffTypes.ACCELERATION.value
            } else 0.0
        } ?: 0.0
         */

        val dominionAccelModifier = starship.playerPilot?.let { player ->
            if (DominionTerritoryBuffTypes.isEffectActive(player, DominionTerritoryBuffTypes.ACCELERATION))
                DominionTerritoryBuffTypes.ACCELERATION.value
            else 0.0
        } ?: 0.0
        return (accel * thrusterPower + /*nationAccelerationModifier + */dominionAccelModifier).roundToHundredth()
    }

    private fun moveTowards(vector: Vector, other: Vector, maxDistance: Double): Vector {
        val direction = other.clone().subtract(vector).normalize()
        val distance = min(maxDistance, other.distance(vector))
        if (distance < maxDistance) {
            vector.x = other.x
            vector.y = other.y
            vector.z = other.z
            return vector
        }
        return vector.add(direction.multiply(distance))
    }
}
