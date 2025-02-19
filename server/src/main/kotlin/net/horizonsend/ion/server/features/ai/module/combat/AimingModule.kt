package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.subsystem.misc.MiningLaserSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TargetTrackingCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.PhaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class AimingModule(
	controller: AIController,
	val difficulty : DifficultyModule,
) : AIModule(controller){
	val shotDeviation: Double get () {return difficulty.shotVariation + 0.025}

	/** given a target vector (global position of a target), and a weaponset, firing mode
	 * Guess the weapon being fired and adjust the target for a leading shot */
	fun adjustAim(targetShip : Starship, origin: Vec3i,
				  weaponSet: AIStarshipTemplate.WeaponSet?, leftClick : Boolean, manual : Boolean) : Vector {
		val shipPos = targetShip.centerOfMass.toVector()
		if (difficulty.aimAdjust < 0.1) return  shipPos

		val predicate = {it : WeaponSubsystem ->
			((it is HeavyWeaponSubsystem) xor leftClick)
			&& ((it is AutoWeaponSubsystem) xor manual)
			&& !(it is MiningLaserSubsystem) // because screw you
			&& it.isIntact()
			&& it.isCooledDown()
			&& it.canFire(getDirection(origin, targetShip.centerOfMass).normalize(), shipPos)// this is a shortcut
		} //reduce the amount of different weapon types as much as possible
		val weapons = (if (weaponSet == null) starship.weapons else starship.weaponSets[weaponSet.name.lowercase()]).shuffled(
			ThreadLocalRandom.current()).filter(predicate)
		if (weapons.isEmpty()) return shipPos //nothing to aim
		val weapon = weapons.first()

		if (weapon is TargetTrackingCannonWeaponSubsystem) return shipPos //for tracking to work cant lead

		var forecast = shipPos
		for (i in 0.until(if (difficulty.doubleEstimateAim) 2 else 1)) {
			val distance = forecast.distance(origin.toVector())
			//calculate the travel time to at most 3 seconds in the future
			val travelTime = if (weapon is PhaserWeaponSubsystem) {
				(distance / PhaserProjectile.speedUpSpeed
					+ TimeUnit.NANOSECONDS.toMillis(PhaserProjectile.speedUpTime).toDouble()/ 1000).coerceAtMost(3.0)
			} else {
				(distance / weapon.balancing.speed).coerceAtMost(3.0)
			}
			if (travelTime >= 1.0) println("travel time: $travelTime, weapon: ${weapon.name}, speed: ${weapon.balancing.speed}")
			forecast = targetShip.forecast(System.currentTimeMillis() + (travelTime * 1000).toLong(),0)
		}
		if (difficulty.aimAdjust > 0.9) return  forecast
		return forecast.clone().multiply(difficulty.aimAdjust).add(shipPos.multiply(1.0 - difficulty.aimAdjust))
	}



	fun sampleDirection(direction: Vector) : Vector{
		// Step 1: Generate a random azimuthal angle φ ∈ [0, 2π]
		val phi = randomDouble(0.0, 2 * Math.PI)

		// Step 2: Generate a random inclination angle θ' for uniform solid angle sampling
		val cosThetaPrime = 1 - randomDouble(0.0,1 - cos(shotDeviation))
		val sinThetaPrime = sqrt(1 - cosThetaPrime * cosThetaPrime)

		// Step 3: Construct the local coordinate frame (T, B, N)
		val x = direction.x
		val y = direction.y
		val z = direction.z

		// Choose a perpendicular vector T
		val T: Vector = if (abs(x) < abs(z)) {
			Vector(-z, 0.0, x).normalize()
		} else {
			Vector(0.0, z, -y).normalize()
		}

		// Compute the binormal vector B = N × T
		val B = direction.clone().crossProduct(T).normalize()

		// Step 4: Compute the new sampled vector
		val sampledVector = T.multiply(sinThetaPrime * cos(phi))
			.add(B.multiply(sinThetaPrime * sin(phi)))
			.add(direction.multiply(cosThetaPrime))

		return sampledVector.normalize() // Ensure it remains a unit vector
	}

	companion object {
		fun showAims(world : World,target : Vector, leftClick: Boolean) {
			val particle = Particle.DUST
			val size = 2.0f
			val dustOptions = if (leftClick) Particle.DustOptions(Color.LIME, size,) else Particle.DustOptions(Color.ORANGE, size,)
			world.spawnParticle(particle,target.x, target.y, target.z,1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		}
	}



}
