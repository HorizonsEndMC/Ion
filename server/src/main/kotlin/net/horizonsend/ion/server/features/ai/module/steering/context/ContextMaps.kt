package net.horizonsend.ion.server.features.ai.module.steering.context

import SteeringModule
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.steering.AIContextConfiguration
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.GoalTarget
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.fleet.FleetMember
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementForecast.forecast
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToPitchYaw
import org.bukkit.FluidCollisionMode
import org.bukkit.util.Vector
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

class MovementInterestContext() : ContextMap(linearbins = true) {
	override fun populateContext() {
		clearContext() //no interia
		lincontext!!.bins[0] = 1e-5//small correction so that it doest freak out
	}
}

class BlankContext() : ContextMap() {
	override fun populateContext() {
		clearContext() //not intertia
	}
}

/**
 * Uses simplex noise to generate a random direction to follow, the direction oscillates based
 * on time. Uses an agents offset to give a different wanting per agent. Combining this
 * behavior with others gives a random organic look to agents.
 *
 * This Context is meant for **`movementInterest`**
 *
 *  * **`weight`** controls how strong this behavior is
 *  * **`dotShift`** shifts the dot product, allowing for backward directions
 * to be considered
 *  * **`jitterRate`** controls how frequently the wandering angle oscillates
 *
 */
class WanderContext(
	val ship: Starship,
	val offset: Double,//offset doesnt change so okay to just copy it
	val configSupplier: Supplier<AIContextConfiguration.WanderContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultWanderContext }
) : ContextMap(linearbins = true) {
	val config get() = configSupplier.get()
	val generator = SimplexOctaveGenerator(1, 1)
	val upVector = Vector(0.0, 1.0, 0.0)

	override fun populateContext() {
		clearContext()

		val finalRate = config.jitterRate * (ship.initialBlockCount.toDouble() / config.sizeFactor).pow(1 / 3.0)
		val timeoffset = offset * finalRate

		// --- Altitude Wander Component ---
		val worldMinY = 0.0
		val worldMaxY = 384.0
		val heightRange = worldMaxY - worldMinY

		val time = System.currentTimeMillis()
		val wanderAltitudeNoise = generator.noise(
			0.0,
			((time / finalRate) % finalRate + timeoffset) / config.verticalJitterMod,
			0.0,
			0.5, 0.5
		)

		// Scale noise [-1,1] → target Y within range
		val preferredAltitude = ((wanderAltitudeNoise + 1.0) / 2.0) * heightRange * 0.8 + heightRange * 0.1
		val currentAltitude = ship.centerOfMass.y.toDouble()

		// How far off are we from desired height
		val verticalBias = (preferredAltitude - currentAltitude) / heightRange * config.verticalWeight

		// Create a vertical vector pointing up or down
		val altitudeVector = upVector.clone().multiply(verticalBias)
		if (altitudeVector.lengthSquared() > 1e-4) {
			altitudeVector.normalize()
			// This pulls the ship toward its current "preferred altitude"
			dotContext(altitudeVector, 0.0, config.weight * abs(verticalBias), clipZero = false)
		}

		for (i in 0 until NUMBINS) {
			val dir = bindir[i]
			val response = generator.noise(
				dir.x,
				((time / finalRate) % finalRate + timeoffset),
				dir.z, 0.5, 0.5
			) + 1
			bins[i] += response * config.weight * upVector.clone().crossProduct(dir).length()
		}

		lincontext!!.apply(lincontext!!.populatePeak(1.0, config.weight))
		checkContext()
		clipZero()
	}
}

/**
 * Whenever an agent changes direction this context map will make the agent commit by
 * discounting previous directions, greatly reduces jittering
 * * **`weight`** controls how strong this context is**
 * * **`hist`** controls how much memory the system has (ie how long to discount)
 */
class CommitmentContext(
	val ship: Starship,
	val configSupplier: Supplier<AIContextConfiguration.CommitmentContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultCommitmentContext }
) : ContextMap() {
	private val config get() = configSupplier.get()
	private var headingHist: ContextMap = object : ContextMap() {}

	override fun populateContext() {
		clearContext()
		headingHist.multScalar(config.hist)
		val velNorm = ship.forward.direction
		headingHist.dotContext(velNorm, 1.0, (1 - config.hist))
		dotContext(velNorm.multiply(-1.0), config.dotShift, config.weight, clipZero = true)
		//multScalar(-1.0)
		for (i in 0 until NUMBINS) {
			bins[i] *= headingHist.bins[i]
		}
		//normalize
		val maxWeight = bins.max()
		multScalar(config.weight / (1 + maxWeight))
	}
}

/**
 * Makes tha agent favor directions with same heading, magnitude increases with lower speed
 * * **`weight`** controls how strong this context is
 * * **`falloff`** affects how it responds to speed (via power), higher means that its less responsive over wider range
 * * **`dotshift`** shifts the dot product (less slack on going backwards with lower values)
 * * **`hist`** controls how much memory the system has (ie how long to discount)
 */
class MomentumContext(
	val ship: Starship,
	val configSupplier: Supplier<AIContextConfiguration.MomentumContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultMomentumContextConfiguration },
	private val maxSpeedSupplier: Supplier<Double>
) : ContextMap() {
	private val config get() = configSupplier.get()
	private val maxSpeed get() = maxSpeedSupplier.get()
	override fun populateContext() {
		clearContext()
		multScalar(config.hist)
		val velNorm = ship.getTargetForward().direction
		val mag = ship.velocity.length() / maxSpeed
		//velNorm = if (mag > 1e-4) velNorm.normal() else
		// Vector2D(1.0, Math.random() * Math.PI * 2).toCartesian()
		dotContext(
			velNorm, config.dotShift,
			(1 - mag).pow(config.falloff) * config.weight * (1 - config.hist), clipZero =
				false
		)
		checkContext()
	}
}

/**
 * Makes the agent follow an orbit around the target. this does it by generating a seek target that
 * is offset by the orbit distance from the actual target and following that point. In order for the
 * agent to orbit properly once the distance is reached, the target point is calculated using
 * a frontal tether of the agent instead of itself. When the agent is at the orbit distance
 * it will follow this tether, and when its far it will move towards orbit.
 *
 * This Context is meant for **`movementInterest`**
 *
 *  * **`weight`** controls how strong this behavior is
 *  * **`dotShift`** shifts the dot product, allowing for backward directions
 * to be considered
 *  * **`offsetDist`** controls the orbiting distance
 *
 */
class OffsetSeekContext(
	val ship: Starship,
	private val generalTarget: Supplier<AITarget?>,
	val module: SteeringModule,
	val configSupplier: Supplier<AIContextConfiguration.OffsetSeekContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultOffsetSeekContextConfiguration },
	private val offsetSupplier: Supplier<Double> = Supplier { configSupplier.get().defaultOffsetDist }
) : ContextMap() {
	private val config get() = configSupplier.get()
	private val offsetDist get() = offsetSupplier.get()
	override fun populateContext() {
		clearContext()
		val aiTarget = generalTarget.get() ?: return
		val seekPos = aiTarget.getLocation().toVector()
		val shipPos = ship.centerOfMass.toVector()
		val center = seekPos.clone()

		// Clamp height difference
		val yDiff = shipPos.y - center.y
		center.y += sign(yDiff) * min(abs(yDiff), config.maxHeightDiff)

		// Compute vector from center to ship (flattened)
		val toShip = shipPos.clone().subtract(center)
		toShip.y = 0.0
		if (toShip.lengthSquared() < 1e-5) return
		toShip.normalize()

		// Tangential orbit direction (perpendicular)
		var orbitDir = Vector(0.0, 1.0, 0.0).crossProduct(toShip).normalize()

		// Current horizontal velocity direction
		val shipVel = ship.velocity.clone()
		shipVel.y = 0.0

		if (shipVel.lengthSquared() > 1e-5) {
			val velocityDir = shipVel.normalize()
			// Flip orbit direction if against current motion
			if (orbitDir.dot(velocityDir) < 0) {
				orbitDir.multiply(-1)
			}
		}

		// Move a bit ahead in orbit direction
		val leadDistance = offsetDist * 2 * PI * 0.05
		val leadPoint = shipPos.clone().add(orbitDir.multiply(leadDistance))

		// From center to leadPoint = orbit offset
		val orbitOffset = leadPoint.clone().subtract(center)
		orbitOffset.y = 0.0
		if (orbitOffset.lengthSquared() < 1e-5) return
		orbitOffset.normalize()

		val target = center.clone().add(orbitOffset.multiply(offsetDist))
		module.orbitTarget = target.clone()

		val targetOffset = target.clone().subtract(shipPos)
		targetOffset.normalize()

		val weight = if (aiTarget is GoalTarget) config.weight * aiTarget.weight else config.weight

		dotContext(targetOffset, config.dotShift, weight)
		checkContext()
	}

}

/**
 * Makes the agent face the target
 *
 * This Context is meant for **`rotationInterest`**
 *
 *  * **`weight`** controls how strong this behavior is
 *  * **`faceWeight`** controls how strongly to pull the facing vector relative to current velocity
 *  * **`maxWeight`** caps the maximum weight for a particular direction
 *  * **`falloff`** controls how this behavior scales with distance, higher
 * values will cause a lower weight (ie agent will only look at target from further away)
 *
 */
class FaceSeekContext(
	val ship: Starship,
	private val generalTarget: Supplier<AITarget?>,
	val difficulty: DifficultyModule,
	val configSupplier: Supplier<AIContextConfiguration.FaceSeekContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultFaceSeekContextConfiguration },
	private val offsetSupplier: Supplier<Double> = Supplier { configSupplier.get().falloff }
) : ContextMap() {
	private val config get() = configSupplier.get()
	override fun populateContext() {
		clearContext()
		val seekPos = generalTarget.get()?.getLocation()?.toVector()
		seekPos ?: return
		if (generalTarget.get() is GoalTarget) return
		val target = seekPos.clone()
		val shipPos = ship.centerOfMass.toVector()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		val distWeight = dist / offsetSupplier.get()
		if (offset.lengthSquared() >= 1e-3) offset.normalize()
		val shipVelocity = ship.velocity.clone()
		if (shipVelocity.lengthSquared() >= 1e-3) shipVelocity.normalize()
		offset.multiply(config.faceWeight).add(shipVelocity).normalize()
		dotContext(offset, -0.2, distWeight * config.weight * difficulty.faceModifier)
		for (i in 0 until NUMBINS) {
			bins[i] = min(bins[i], config.maxWeight * difficulty.faceModifier)
		}
		checkContext()
	}
}

/**
 * Makes the agent face the target
 *
 * This Context is meant for **`rotationInterest`**
 *
 *  * **`weight`** controls how strong this behavior is
 *  * **`faceWeight`** controls how strongly to pull the facing vector relative to current velocity
 *  * **`maxWeight`** caps the maximum weight for a particular direction
 *  * **`falloff`** controls how this behavior scales with distance, higher
 * values will cause a lower weight (ie agent will only look at target from further away)
 *
 */
class GoalSeekContext(
	val ship: Starship,
	private val goalPoint: Vec3i,
	val configSupplier: Supplier<AIContextConfiguration.GoalSeekContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultGoalSeekContextConfiguration }
) : ContextMap() {
	private val config get() = configSupplier.get()
	private var reached = false
	override fun populateContext() {
		clearContext()
		if (reached) return
		val seekPos = goalPoint.toVector()
		val target = seekPos.clone()
		val shipPos = ship.centerOfMass.toVector()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		if (dist < 100.0) {
			reached = true
		}
		offset.normalize()
		dotContext(offset, 0.0, dist / config.falloff * config.weight)
		for (i in 0 until NUMBINS) {
			bins[i] = min(bins[i], config.maxWeight)
		}
		checkContext()
	}
}

/** makes ships spawned in the same fleet stay close to the center of mass modulated by the sum of cube root block counts
 * This Context is meant for **`movementInterest`**
 *
 *  * **`weight`** controls how strong this behavior is
 *  * **`falloffMod`** controls how this behavior scales with distance, higher
 * values will cause a lower weight (ie agent will only move towards fleet from further away)*/
class FleetGravityContext(
	val ship: Starship,
	val configSupplier: Supplier<AIContextConfiguration.FleetGravityContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultFleetGravityContextConfiguration }
) : ContextMap() {
	private val config get() = configSupplier.get()
	override fun populateContext() {
		clearContext()
		val fleet = (ship.controller as AIController) //get ships in the same fleet in the same world
			.getUtilModule(AIFleetManageModule::class.java)?.fleet?.members
			?.filter {
				(it as? FleetMember.AIShipMember)?.shipRef?.get()?.world == ship.world
			}?.mapNotNull { (it as FleetMember.AIShipMember).shipRef.get() }
		fleet ?: return
		if (fleet.size <= 1) return
		val com = Vector()
		fleet.forEach { com.add(it.centerOfMass.toVector()) }
		com.multiply(1 / fleet.size.toDouble())

		val shipPos = ship.centerOfMass.toVector()
		val target = com.clone()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		offset.normalize()

		var R3BlockCount = 0.0
		fleet.forEach { R3BlockCount += it.initialBlockCount.toDouble() }
		R3BlockCount = R3BlockCount.pow(1 / 3.0) + 1e-4
		//R3BlockCount /= fleet.size.toDouble()

		val falloff = R3BlockCount * config.falloffMod

		dotContext(offset, 0.0, dist / falloff * config.weight)
	}
}

class SquadFormationContext(

) : ContextMap(linearbins = true) {
	val weight = 2.0
	val maxWeight = 5.0
	val falloff = 100.0
	val dotShift = 0.0
	val throttleFalloff = 100.0
	override fun populateContext() {
		clearContext()
		return
		//TODO reimplement this
		/**
		var target = this@BasicSteeringModule.ship.squad?.getFromationPos(this@BasicSteeringModule.ship)?: return
		val squadlead = ship.squad?.ships?.get(0) ?: return
		target = target.add(lookAhead(squadlead, pos = target, futuremod = 2.0))
		var targetOffset = target.add(ship.pos.mult(-1.0))
		val dist = targetOffset.mag()
		if (dist/falloff < 1e-2) return
		squadTarget = target
		targetOffset = targetOffset.normal()
		dotContext(targetOffset, dotShift, min(dist/ falloff * weight, maxWeight))
		val throttleWeight = min(throttleFalloff*weight / dist, maxWeight)
		lincontext!!.apply(lincontext!!.populatePeak(dist/throttleFalloff, throttleWeight))
		 */
	}
}

class AvoidIlliusContext(val ship: Starship) : ContextMap() {
	override fun populateContext() {
		clearContext()
		if (ship.world.name != "Asteri") return
		val loc = Space.getPlanet("Illius")?.location?.toVector() ?: return
		val shipPos = ship.centerOfMass.toVector()
		val target = loc.clone()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		offset.normalize()

		val falloff = 400.0

		dotContext(offset, 0.0, -1.0 * falloff / dist * 1.0)
	}
}

/**
 * Maps the danger of facing a particular direction for each direction
 * Such that the ship will favor rotating ouf of the way if one side gets low.
 *
 * It does this by keeping a history of incoming damage, and then artificially rotating the ship to see how it responds
 * to the incoming damage. For a particular orientation, if a damaged shield is aligned with the incoming damage, then
 * the danger for that orientation is high. The danger is low for high shields or for shields facing away from the
 * incoming damage.
 *
 *  * **`weight`** controls how strong this behavior is
 *  * **`criticalPoint`** pivot point for danger, higher values makes the ship more responsive to damage
 *  * **`power`** controls the response around the critical point via power. For higher powers, the response will be
 *  minimal below the critical point, and higher above the critical point
 *  * **`histDecay`** controls how much memory incoming fire has (between 0 and 1). Higher values will cause higher
 *  memory of past incoming damage, while lower values would make it more responsive to rapid changes.
 *  * **`verticalDamp`** damps the response to vertical directions, no damping may result in ships moving up or down
 *  when low on shields, and high values may make it insensitive to off-plane shields.
 *	* **`damageSensitivity`** controls how sensitive recent damage is to incoming fire memory
 *
 */
class ShieldAwarenessContext(
	val ship: Starship,
	val difficulty: DifficultyModule,
	val configSupplier: Supplier<AIContextConfiguration.ShieldAwarenessContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultShieldAwarenessContextConfiguration }
) : ContextMap() {
	private val config get() = configSupplier.get()
	val incomingFire: ContextMap = object : ContextMap() {}
	private val verticalDamp: ContextMap = object : ContextMap() {
		override fun populateContext() {
			dotContext(Vector(0.0, 1.0, 0.0), 0.0, 1.0, clipZero = false)
			for (i in 0 until NUMBINS) {
				bins[i] = 1 - abs(bins[i]).coerceAtMost(config.verticalDamp * 0.3 + 0.7)
				bins[i] = bins[i].pow(config.verticalDamp)
			}
		}
	}

	private data class ShieldInfo(val shield : ShieldSubsystem, val localVector : Vector) {
		val maxPower get() = shield.maxPower
		val power get() = shield.power
	}

	private val shieldsInfo = ship.shields.map {
		// vector from COM to shield in WORLD space
		val worldVec = it.pos.toVector().subtract(ship.centerOfMass.toVector())

		// remove current ship yaw → ship-local
		val shipYaw  = vectorToPitchYaw(ship.forward.direction, true).second.toDouble()
		val localVec = worldVec.rotateAroundY(-shipYaw).normalize()
		ShieldInfo(it, localVec)
	}

	private fun transformCords(info: ShieldInfo, heading: Vector): Vector {
		//take those relative cords and rotate then towards the heading direction
		val headingPlane = heading.clone()
		headingPlane.y = 0.0
		val (_, targetYaw) = vectorToPitchYaw(headingPlane, radians = true)
		return info.localVector.clone().rotateAroundY(-targetYaw.toDouble())
	}

	init {
		verticalDamp.populateContext()
	}

	override fun populateContext() {
		clearContext()
		if (!difficulty.isShieldAware) return
		val shipPos = ship.centerOfMass.toVector()
		incomingFire.multScalar(config.histDecay)
		if (ship.shields.size <= 1) return
		for (shield in ship.shields) {
			val center = shield.pos.toVector()
			val offset = center.add(shipPos.clone().multiply(-1.0)).normalize()
			val whitenedOffset = whitenOffset(offset,ship.min.toVector(),ship.max.toVector(),config.geomWhitening)
			incomingFire.dotContext(whitenedOffset, -0.5, shield.recentDamage * config.damageSensitivity)
			val maxFire = incomingFire.bins.max()
			if (maxFire > config.incomingFireWeight && maxFire > 0.0) {
				incomingFire.multScalar(config.incomingFireWeight / maxFire)   // scale *down* to the cap
			}
			incomingFire.checkContext()
		}

		for (i in 0 until NUMBINS) {
			val dir = bindir[i]
			val rotatedCenters = shieldsInfo.map { transformCords(it, dir) }
			val response = object : ContextMap() {}
			for (j in 0 until ship.shields.size) {
				val shield = ship.shields[j]
				val offset = rotatedCenters[j]

				val damage: Double = ((shield.maxPower - shield.power) / (shield.maxPower.toDouble() * (1 - config.criticalPoint))).pow(config.power)

				if (damage.isNaN()) continue

				response.dotContext(direction = offset, shift = -0.3, scale = damage)
			}
			for (k in 0 until NUMBINS) {
				response.bins[k] *= incomingFire.bins[k]
			}
			bins[i] += response.bins.sum()
			bins[i] *= verticalDamp.bins[i]
		}
		//normalize
		val minDanger = bins.min()
		addScalar(-minDanger)
		val maxDanger = bins.max()
		multScalar(config.weight / (1 + maxDanger))
		checkContext()
	}
}

/** Whitens `v` according to the ship's bounding box and α ∈ [0,1] */
private fun whitenOffset(v: Vector, min: Vector, max: Vector, alpha: Double): Vector {
	if (alpha == 0.0) return v                       // fast path
	val dx = max.x - min.x
	val dy = max.y - min.y
	val dz = max.z - min.z
	val mean = (dx + dy + dz) / 3.0

	// scale factors that would make the hull isotropic …
	val sx = mean / dx
	val sy = mean / dy
	val sz = mean / dz

	// … and blend them with identity according to α
	return Vector(
		v.x * (1 - alpha + alpha * sx),
		v.y * (1 - alpha + alpha * sy),
		v.z * (1 - alpha + alpha * sz)
	)
}

/**
 * Makes the ship avoid other ships, the smaller the distance between ships the higher
 * the danger. Uses forecasting so that it calculates danger based on a future collision instead of current position
 * allowing ships to fly side by side or pass by with minimal reaction
 *
 * The bigger the other ship is the more danger it contributes maintains a bigger distance, the faster the current
 * ship, the more dangerous other ships are so maintains a bigger distance against other ships. Ie a super capital will
 * not react to an incoming starfighter until it's very close (it moves slowly, and the obstical is small) while the
 * same starfighter will react from much further away (it moves quickly and the obstical is large).
 *
 * This Context is meant for **`danger`**
 *
 *  * **`falloff`** controls how this behavior scales with distance, higher
 * values will cause higher danger and agents will react faster
 *  * **`dotShift`** shifts the dot product, giving danger to orthogonal
 * directions
 *  * **`shipWeightSize`** modulates the size factor of danger, larger values makes size less important
 *  * **`shipWeightSpeed`** modulates the speed factor of danger, larger values makes speed less important
 */
class ShipDangerContext(
	val ship: Starship,
	private val maxSpeedSupplier: Supplier<Double>,
	val module: SteeringModule,
	val configSupplier: Supplier<AIContextConfiguration.ShipDangerContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultShipDangerContextConfiguration }
) : ContextMap() {
	private val config get() = configSupplier.get()
	private val maxSpeed get() = maxSpeedSupplier.get()

	private var tick = 0

	override fun populateContext() {
		if (tick % INTERVAL != 0) return
		tick++

		clearContext()
		var mindist = 1e10
		val shipPos = ship.centerOfMass.toVector()
		for (otherShip in ActiveStarships.getInWorld(ship.world)) {
			if (otherShip == ship) continue
			otherShip.centerOfMass.toVector()
			val target = lookAhead(ship, otherShip, futuremod = 1.0, maxSpeed = maxSpeed)
			val targetOffset = target.add(shipPos.clone().multiply(-1.0))
			val targetDist = targetOffset.length() + 1e-4
			if (targetDist < mindist) {
				mindist = targetDist
				module.dangerTarget = target
			}
			targetOffset.normalize()
			val dangerWeight = (((otherShip.initialBlockCount.toDouble()).pow(1.0 / 3.0) / config.shipWeightSize)
				* (maxSpeed / config.shipWeightSpeed))
			dotContext(targetOffset, config.dotShift, (config.falloff * dangerWeight) / targetDist, power = 1.0, true)
			checkContext()
		}
	}

	companion object {
		private const val INTERVAL = 10
	}
}

/**
 * Makes the agent avoid particles, the smaller the distance between a ship and particle the higher
 * the danger, ignores particles originating from the ship
 *
 * This Context is meant for **`danger`**
 *
 *  * **`falloff`** controls how this behavior scales with distance, higher
 * values will cause higher danger and agents will react faster
 *  * **`dotShift`** shifts the dot product, giving danger to orthogonal
 * directions
 */
private var particleDanger: ContextMap = object : ContextMap() {
	val falloff = 20.0
	val dotShift = -0.0
	val shipWeightSize = 10.0
	val shipWeightSpeed = 20.0
	val particleWeight = 10.0
	val maxWeight = 3.0

	override fun populateContext() {
		clearContext()
		return
		//TODO reimplement this
		/*
		for (particle in Simulation.particles) {
			if (particle.source == this@BasicSteeringModule.ship) continue
			//first calculate if particle is moving towards the target
			// do not use forecast
			val offset = particle.pos.add(ship.pos.mult(-1.0))
			val relativeVelocity = particle.velocity.add(ship.velocity.mult(-1.0))
			if (relativeVelocity.dot(offset) > 0) continue
			val targets = this@BasicSteeringModule.ship.shields.map {it.transformCords(true)[0]}
			for (target in targets) {
				var targetOffset = particle.pos.add(target.mult(-1.0))
				val targetDist = targetOffset.mag() + 1e-4
				targetOffset = targetOffset.normal()
				val dangerWeight = ((ship.size / shipWeightSize)
						* (ship.MAXSPEED /shipWeightSpeed)
						*(particle.damage/particleWeight)
						/this@BasicSteeringModule.ship.shields.size.toDouble())
				dotContext(targetOffset,dotShift,min((falloff*dangerWeight)/targetDist,maxWeight), power = 1.0)
			}
			//forecast the particles position and calculate danger as normal
			//val target = offset//particle.pos.add(lookAhead(particle.pos,particle.velocity, futuremod = 2.0))

		}
		*/
	}
}

/**
 * Makes the agent avoid the borders, the smaller the distance between the agent and
 * boundaries the higher the danger
 *
 * This Context is meant for **`danger`**
 *
 *  * **`falloff`** controls how this behavior scales with distance, higher
 * values will cause higher danger and agents will react faster
 *  * **`verticalFalloff`**  same thing, for y axis
 *  * **`dotShift`** shifts the dot product, giving danger to orthogonal
 * directions
 *
 */
class BorderDangerContext(
	val ship: Starship,
	val configSupplier: Supplier<AIContextConfiguration.BorderDangerContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultBorderDangerContextConfiguration }
) : ContextMap() {
	private val config get() = configSupplier.get()
	override fun populateContext() {
		clearContext()
		val worldborder = ship.world.worldBorder
		val borderCenter = worldborder.center.toVector()
		val radius = worldborder.size / 2
		for (i in 0 until NUMBINS) {
			val horizontalFalloff = config.falloff * ship.initialBlockCount.toDouble().pow(1 / 3.0)
			//north border
			var dir = Vector(0.0, 0.0, -1.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.z.toDouble(), borderCenter.z - radius, horizontalFalloff)
			//south border
			dir = Vector(0.0, 0.0, 1.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.z.toDouble(), borderCenter.z + radius, horizontalFalloff)
			//west border
			dir = Vector(-1.0, 0.0, 0.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.x.toDouble(), borderCenter.x - radius, horizontalFalloff)
			//east border
			dir = Vector(1.0, 0.0, 0.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.x.toDouble(), borderCenter.x + radius, horizontalFalloff)

			//up border
			dir = Vector(0.0, 1.0, 0.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.y.toDouble(), 383.0, config.verticalFalloff)

			//down border
			dir = Vector(0.0, -1.0, 0.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.y.toDouble(), 0.0, config.verticalFalloff)
		}
		checkContext()
	}

	private fun calcDanger(bindir: Vector, dir: Vector, val1: Double, val2: Double, falloff: Double): Double {
		val proximity = (abs(val1 - val2) + 1e-4)
		var mag = (bindir.dot(dir) + config.dotShift) * falloff / proximity
		mag = if (mag < 0) 0.0 else mag
		return mag
	}
}

/**
 * Makes the agent avoid blocks in the world by sending raycasts
 *
 * This Context is meant for **`danger`**
 *
 *  * **`falloff`** controls how this behavior scales with distance, higher
 * values will cause higher danger and agents will react faster
 *  * **`dotShift`** shifts the dot product, giving danger to orthogonal
 * directions
 *  * **`maxDist`** how far are rayCasts
 */
class WorldBlockDangerContext(
	val ship: Starship,
	val configSupplier: Supplier<AIContextConfiguration.WorldBlockDangerContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultWorldBlockDangerContextConfiguration }
) : ContextMap() {
	private val config get() = configSupplier.get()

	private var tick = 0

	override fun populateContext() {
		if (tick % INTERVAL != 0) return
		tick++

		val world = ship.world
		clearContext()
		val shipPos = ship.centerOfMass.toLocation(world)

		for (dir in bindir) {
			val result = world.rayTraceBlocks(shipPos, dir, config.maxDist, FluidCollisionMode.ALWAYS, false) { block -> !ship.contains(block.x, block.y, block.z) } ?: continue
			val dist = result.hitPosition.add(shipPos.toVector().multiply(-1.0)).length()
			val shipVelocity = ship.velocity.clone()
			val velocityWeight: Double

			if (shipVelocity.lengthSquared() < 1e-3) {
				velocityWeight = 1.0
			} else {
				val velocityMag = shipVelocity.length() + 1.0
				shipVelocity.normalize()
				velocityWeight = (shipVelocity.dot(dir).coerceAtLeast(0.0) * velocityMag).pow(0.5)
			}

			val falloff = config.falloff * (ship.currentBlockCount * config.sizeFactor).pow(1 / 3.0)
			val weight = falloff * velocityWeight / dist
			dotContext(dir, 0.0, weight, config.dotPower)
		}
	}

	companion object {
		private const val INTERVAL = 20
	}
}

/**
 * Makes the agent avoid directions where the ship is obstructed (failed to move)
 *
 * This Context is meant for **`danger`**
 *
 *  * **`falloff`** controls how this behavior scales with distance, higher
 * values will cause higher danger and agents will react faster
 *  * **`dotShift`** shifts the dot product, giving danger to orthogonal
 * directions
 *  * **`expireTime`** how long to remember an obstruction in miliseconds
 */
class ObstructionDangerContext(
	val ship: Starship,
	private val obstructions: ConcurrentHashMap<Vec3i, Long>,
	val configSupplier: Supplier<AIContextConfiguration.ObstructionDangerContextConfiguration> =
		Supplier { ConfigurationFiles.aiContextConfiguration().defaultObstructionDangerContextConfiguration }
) : ContextMap() {
	private val config get() = configSupplier.get()
	override fun populateContext() {
		clearContext()
		val time = System.currentTimeMillis()
		val shipPos = ship.centerOfMass.toVector()
		for (obstruction in obstructions.keys()) {
			if (time - obstructions[obstruction]!! > config.expireTime) {
				obstructions.remove(obstruction)
				continue
			}
			val offset = obstruction.toVector().add(shipPos.clone().multiply(-1.0))
			val dist = offset.length()
			offset.normalize()
			val falloff = config.falloff * ship.initialBlockCount.toDouble().pow(1 / 3.0)
			dotContext(offset, config.dotShift, falloff / dist, config.dotPower)
		}
	}
}

private fun lookAhead(
	ship: Starship, other: Starship,
	pos: Vector = other.centerOfMass.toVector(),
	futuremod: Double = 1.0, maxSpeed: Double? = null
): Vector {
	val offset = pos.add(ship.centerOfMass.toVector().multiply(-1.0))
	val dist = offset.length() + 1e-4
	val vel = maxSpeed ?: (ship.velocity.length() + 1e-5)
	val t = (dist / (vel * futuremod)).coerceAtMost(2.0)
	val forecast = forecast(other, System.currentTimeMillis() + (t * 1000).toLong(), 0)
	return forecast
}
