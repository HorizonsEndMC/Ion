package net.horizonsend.ion.server.features.ai.module.steering.context

import SteeringModule
import net.horizonsend.ion.server.IonServer.aiContextConfig
import net.horizonsend.ion.server.features.ai.configuration.steering.AIContextConfiguration
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.vectorToPitchYaw
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
	val ship : Starship,
	val offset : Double,//offset doesnt change so okay to just copy it
	val config : AIContextConfiguration.WanderContextConfiguration = aiContextConfig.defaultWanderContext,
) : ContextMap(linearbins = true) {
	val generator = SimplexOctaveGenerator(1, 1)

	override fun populateContext() {
		clearContext()

		val finalRate = config.jitterRate * (ship.currentBlockCount.toDouble().pow(1/3.0) / config.sizeFactor)
		val timeoffset = offset * finalRate

		for (i in 0 until NUMBINS) {
			val dir = bindir[i]
			val response = generator.noise(dir.x,
										dir.y + ((System.currentTimeMillis() / finalRate) % finalRate + timeoffset),
										   dir.z, 0.5, 0.5) + 1
			bins[i] += response*config.weight
		}

		lincontext!!.apply(lincontext!!.populatePeak(1.0, config.weight))
		checkContext()
	}
}

/**
 * Whenever an agent changes direction this context map will make the agent commit by
 * discounting previous directions, greatly reduces jittering
 * * **`weight`** controls how strong this context is**
 * * **`hist`** controls how much memory the system has (ie how long to discount)
 */
class CommitmentContext(
	val ship : Starship,
	val config : AIContextConfiguration.CommitmentContextConfiguration = aiContextConfig.defaultCommitmentContext,
) : ContextMap() {
	private var headingHist: ContextMap = object : ContextMap() {}

	override fun populateContext() {
		clearContext()
		headingHist.multScalar(config.hist)
		val velNorm = ship.getTargetForward().direction
		headingHist.dotContext(velNorm,1.0,(1-config.hist))
		dotContext(velNorm.multiply(-1.0),0.0,config.weight, clipZero = true)
		multScalar(-1.0)
		for (i in 0 until NUMBINS) {
			bins[i] *= headingHist.bins[i]
		}
		checkContext()
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
	val ship : Starship,
	val config : AIContextConfiguration.MomentumContextConfiguration = aiContextConfig.defaultMomentumContextConfiguration,
	private val maxSpeedSupplier: Supplier<Double>,
) :  ContextMap() {
	private val maxSpeed get() = maxSpeedSupplier.get()
	override fun populateContext() {
		clearContext()
		multScalar(config.hist)
		val velNorm = ship.getTargetForward().direction
		val mag = ship.velocity.length() / maxSpeed
		//velNorm = if (mag > 1e-4) velNorm.normal() else
		// Vector2D(1.0, Math.random() * Math.PI * 2).toCartesian()
		dotContext(velNorm, config.dotShift,
			(1 - mag).pow(config.falloff) * config.weight * (1- config.hist), clipZero =
		false)
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
	val ship : Starship,
	private val generalTarget : Supplier<AITarget?>,
	val module : SteeringModule,
	val config : AIContextConfiguration.OffsetSeekContextConfiguration = aiContextConfig.defaultOffsetSeekContextConfiguration,
	private val offsetSupplier: Supplier<Double> = Supplier<Double> { config.defaultOffsetDist },
): ContextMap() {
	private val offsetDist get() =  offsetSupplier.get()
	override fun populateContext() {
		clearContext()
		val seekPos =  generalTarget.get()?.getLocation()?.toVector()
		seekPos ?: return
		var finalDist = offsetDist
		if (generalTarget.get() is StarshipTarget) {
			finalDist =  offsetDist * min((generalTarget.get() as StarshipTarget).ship.currentBlockCount
											/ship.currentBlockCount.toDouble(), 1.0)
		}
		val shipPos = ship.centerOfMass.toVector()
		val center = seekPos.clone()
		val yDiff = shipPos.clone().add(center.clone().multiply(-1.0)).y
		center.y += sign(yDiff) * min(abs(yDiff),config.maxHeightDiff)//adjust center to account for height diff
		val tetherl = finalDist * PI * 2 * 0.1
		val shipvel = ship.velocity.clone()
		shipvel.y = 0.0
		if (shipvel.length() > 1e-5) shipvel.normalize()
		val frowardTether = shipPos.clone().add(shipvel.multiply(tetherl))
		val tetherOffset = frowardTether.add(center.clone().multiply(-1.0))
		tetherOffset.y = 0.0
		tetherOffset.normalize()
		val target = center.clone().add(tetherOffset.multiply(finalDist))
		module.orbitTarget = target.clone()
		val targetOffset = target.clone().add(shipPos.clone().multiply(-1.0))
		val dist = targetOffset.length()
		targetOffset.normalize()
		dotContext(targetOffset, config.dotShift, config.weight)
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
	val ship : Starship,
	private val generalTarget : Supplier<AITarget?>,
	val difficulty: DifficultyModule,
	val config : AIContextConfiguration.FaceSeekContextConfiguration = aiContextConfig.defaultFaceSeekContextConfiguration,
) : ContextMap() {
	override fun populateContext() {
		clearContext()
		val seekPos =  generalTarget.get()?.getLocation()?.toVector()
		seekPos ?: return
		val target= seekPos.clone()
		val shipPos = ship.centerOfMass.toVector()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		offset.normalize()
		offset.multiply(config.faceWeight).add(ship.getTargetForward().direction).normalize()
		dotContext(offset,-0.2, dist / config.falloff * config.weight * difficulty.faceModifier)
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
	val ship : Starship,
	private val goalPoint : Vec3i,
	val config : AIContextConfiguration.GoalSeekContextConfiguration = aiContextConfig.defaultGoalSeekContextConfiguration,
) : ContextMap() {
	private var reached = false
	override fun populateContext() {
		clearContext()
		if (reached) return
		val seekPos =  goalPoint.toVector()
		val target= seekPos.clone()
		val shipPos = ship.centerOfMass.toVector()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		if (dist < 100.0) {
			reached = true
		}
		offset.normalize()
		dotContext(offset,0.0, dist / config.falloff * config.weight)
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
	val ship : Starship,
	val config : AIContextConfiguration.FleetGravityContextConfiguration = aiContextConfig.defaultFleetGravityContextConfiguration,
) : ContextMap(){
	override fun populateContext() {
		clearContext()
		val fleet = (ship.controller as AIController) //get ships in the same fleet in the same world
			.getModuleByType<AIFleetManageModule>()?.fleet?.members?.filter {it.get()?.world == ship.world}
		fleet ?: return
		if (fleet.size <= 1) return
		val com = Vector()
		fleet.forEach{ it.get()?.centerOfMass?.let { it1 -> com.add(it1.toVector()) } }
		com.multiply(1/fleet.size.toDouble())

		val shipPos = ship.centerOfMass.toVector()
		val target= com.clone()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		offset.normalize()

		var R3BlockCount = 0.0
		fleet.forEach {it.get()?.let { other -> R3BlockCount += other.currentBlockCount.toDouble().pow(1/3.0)}}
		//R3BlockCount /= fleet.size.toDouble()

		val falloff = R3BlockCount * config.falloffMod

		dotContext(offset,0.0, dist / falloff * config.weight )
	}
}

class SquadFormationContext(

) : ContextMap(linearbins = true){
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

class AvoidIlliusContext(val ship : Starship) : ContextMap() {
	override fun populateContext() {
		clearContext()
		if (ship.world.name != "Asteri") return
		val loc = Space.getPlanet("Illius")?.location?.toVector() ?: return
		val shipPos = ship.centerOfMass.toVector()
		val target= loc.clone()
		val offset = target.add(shipPos.clone().multiply(-1.0))
		val dist = offset.length() + 1e-4
		offset.normalize()

		val falloff = 400.0

		dotContext(offset,0.0, -1.0*falloff/dist * 1.0)
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
 *
 */
class ShieldAwarenessContext(
	val ship : Starship,
	val difficulty: DifficultyModule,
	val config : AIContextConfiguration.ShieldAwarenessContextConfiguration = aiContextConfig.defaultShieldAwarenessContextConfiguration,
)  : ContextMap() {
	private val incomingFire : ContextMap = object : ContextMap() {}
	private val verticalDamp : ContextMap = object : ContextMap() {
		override fun populateContext() {
			dotContext(Vector(0.0,1.0,0.0),0.0,1.0, clipZero = false)
			for (i in 0 until NUMBINS) {
				bins[i] = 1 - abs(bins[i])
				bins[i] = bins[i].pow(config.verticalDamp)
			}
		}
	}
	init {
	    verticalDamp.populateContext()
		//val previousCenters = ship.shields.map {it.pos.toVector().add(ship.centerOfMass.toVector().multiply(-1.0))}
		//var rotatedCenters = ship.shields.map {transformCords(ship,it,Vector(0.0,0.0,1.0))}
		//println("previous centers: $previousCenters")
		//println("rotated centers around ${Vector(0.0,0.0,1.0)} : $rotatedCenters")
		//rotatedCenters = ship.shields.map {transformCords(ship,it,Vector(1.0,0.0,0.0))}
		//("rotated centers around ${Vector(1.0,0.0,0.0)} : $rotatedCenters")
	}
	override fun populateContext() {
		clearContext()
		if (!difficulty.isShieldAware) return
		val shipPos = ship.centerOfMass.toVector()
		incomingFire.multScalar(config.histDecay)
		for (shield in ship.shields) {
			if (ship.shields.size <= 1) return
			val center = shield.pos.toVector()
			val offset = center.add(shipPos.clone().multiply(-1.0)).normalize()
			incomingFire.dotContext(offset,-0.6,shield.recentDamage * config.damageSensitivity)
			incomingFire.checkContext()
		}
		for (i in 0 until NUMBINS) {
			val dir = bindir[i]
			if (abs(dir.dot(Vector(0.0,1.0,0.0))) >= 0.9 ) continue //skip vertical directions
			val rotatedCenters = ship.shields.map { transformCords(ship,it,dir) }
			val response = object : ContextMap() {}
			for (j in 0 until ship.shields.size) {
				val shield = ship.shields[j]
				val offset = rotatedCenters[j].clone().normalize()
				val damage = ((shield.maxPower - shield.power)/
					(shield.maxPower.toDouble()*(1-config.criticalPoint))).pow(config.power)
				response.dotContext(offset,-0.3,damage*config.weight)
			}
			for (k in 0 until NUMBINS) {
				response.bins[k] *= incomingFire.bins[k]
			}
			bins[i] += response.bins.sum()
			bins[i] *= verticalDamp.bins[i]
		}
		checkContext()
	}
}

private fun transformCords(ship : Starship,shield: ShieldSubsystem, heading: Vector): Vector {
	val shipPos = ship.centerOfMass.toVector()
	val center = shield.pos.toVector()
	//first transform centers from absoluate cords to relative cords (including from ships orientation)
	center.add(shipPos.clone().multiply(-1.0))
	val shipHeading = ship.forward.direction
	val (_,shipYaw) = vectorToPitchYaw(shipHeading, radians = true)
	center.rotateAroundY(-shipYaw.toDouble())
	//then take those relative cords and rotate then towards the heading direction
	val headingPlane = heading.clone()
	headingPlane.y = 0.0
	val (_,yaw) = vectorToPitchYaw(headingPlane, radians = true)
	return center.rotateAroundY(yaw.toDouble())
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
	val ship : Starship,
	private val maxSpeedSupplier: Supplier<Double>,
	val module : SteeringModule,
	val config : AIContextConfiguration.ShipDangerContextConfiguration = aiContextConfig.defaultShipDangerContextConfiguration,
) : ContextMap() {
	private val maxSpeed get() = maxSpeedSupplier.get()

	override fun populateContext() {
		clearContext()
		var mindist = 1e10
		val shipPos = ship.centerOfMass.toVector()
		for (otherShip in ActiveStarships.getInWorld(ship.world)) {
			if (otherShip == ship) continue
			val othershipPos = otherShip.centerOfMass.toVector()
			val target = othershipPos.clone().add(lookAhead(ship, otherShip, futuremod = 1.0, maxSpeed= maxSpeed))
			val targetOffset = target.add(shipPos.clone().multiply(-1.0))
			val targetDist = targetOffset.length() + 1e-4
			if (targetDist < mindist) {
				mindist = targetDist
				module.dangerTarget = target
			}
			targetOffset.normalize()
			val dangerWeight = (((otherShip.currentBlockCount.toDouble()).pow(1.0/3.0) / config.shipWeightSize)
				* (maxSpeed /config.shipWeightSpeed))
			dotContext(targetOffset,config.dotShift,(config.falloff*dangerWeight)/targetDist, power = 1.0, true)
			checkContext()
		}
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
 *  * **`dotShift`** shifts the dot product, giving danger to orthogonal
 * directions
 *
 */
class BorderDangerContext(
	val ship : Starship,
	val config : AIContextConfiguration.BorderDangerContextConfiguration = aiContextConfig.defaultBorderDangerContextConfiguration,
) : ContextMap() {
	override fun populateContext() {
		clearContext()
		val worldborder = ship.world.worldBorder
		val borderCenter = worldborder.center.toVector()
		val radius = worldborder.size / 2
		for (i in 0 until NUMBINS) {
			//north border
			var dir = Vector(0.0,0.0, -1.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.z.toDouble(), borderCenter.z - radius, config.falloff)
			//south border
			dir = Vector(0.0,0.0, 1.0)
			bins[i] += calcDanger(bindir[i],dir,ship.centerOfMass.z.toDouble(), borderCenter.z + radius, config.falloff)
			//west border
			dir = Vector(-1.0,0.0, 0.0)
			bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.x.toDouble(), 	borderCenter.x - radius, config.falloff)
			//east border
			dir = Vector(1.0,0.0, 0.0)
			bins[i] += calcDanger(bindir[i],dir,ship.centerOfMass.x.toDouble(),borderCenter.x	+ radius, config.falloff)

			//up border
			dir = Vector(0.0,1.0, 0.0)
			bins[i] += calcDanger(bindir[i],dir,ship.centerOfMass.y.toDouble(),383.0, config.verticalFalloff)

			//down border
			dir = Vector(0.0,-1.0, 0.0)
			bins[i] += calcDanger(bindir[i],dir,ship.centerOfMass.y.toDouble(),0.0, config.verticalFalloff)
		}
		checkContext()
	}

	private fun calcDanger(bindir: Vector, dir: Vector, val1: Double, val2: Double, falloff : Double): Double {
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
 */
class WorldBlockDangerContext(
	val ship : Starship,
	val config : AIContextConfiguration.WorldBlockDangerContextConfiguration = aiContextConfig.defaultWorldBlockDangerContextConfiguration,
) : ContextMap() {
	override fun populateContext() {
		val world = ship.world
		clearContext()
		val shipPos = ship.centerOfMass.toLocation(world)
		for (dir in bindir) {
			val result = world.rayTraceBlocks(shipPos,dir, config.maxDist, FluidCollisionMode.ALWAYS, false) {
					block -> !ship.contains(block.x, block.y, block.z)} ?: continue
			val dist = result.hitPosition.add(shipPos.toVector().multiply(-1.0)).length()
			dotContext(dir, 0.0, config.falloff/ dist, config.dotPower)
		}
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
 */
class ObstructionDangerContext(
	val ship : Starship,
	private val obstructions : ConcurrentHashMap<Vec3i, Long>,
	val config : AIContextConfiguration.ObstructionDangerContextConfiguration = aiContextConfig.defaultObstructionDangerContextConfiguration,
): ContextMap() {
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
			dotContext(offset, config.dotShift, config.falloff/ dist, config.dotPower)
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
	val t = dist / (vel * futuremod)
	return other.velocity.clone().multiply(t)
}
