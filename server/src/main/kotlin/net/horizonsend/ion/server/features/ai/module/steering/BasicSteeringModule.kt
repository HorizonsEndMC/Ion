/*
Context steering based control for AI
See https://andrewfray.wordpress.com/2013/03/26/context-behaviours-know-how-to-share/
https://www.gameaipro.com/GameAIPro2/GameAIPro2_Chapter18_Context_Steering_Behavior-Driven_Steering_at_the_Macro_Scale.pdf
and https://alliekeats.com/portfolio/contextbhvr.html for more in depth descriptions.
Essentially agents determine where to go by using a set of arrays called context maps, these maps
can either hold "interest": ie how much the ai wants to move in a particular direction, or
"danger": how much an ai should avoid a particular direction. Once the ai has the context of what
 to do, it can make a final decision on its heading.
Each of these maps can be combined like image filters, leading to simple and stateless
AI that is still amendmedable to emergent behaviors and avoids deadlock.

The strategy for implementing context steering is as follows:
1. identity the necessary behaviors (seek, avoid, face target, wander ect) and populate their context maps
2. combine behaviors together using simple arithmetic where possible like filters
3. Make a decision based on the final interest and danger context maps, importantly decision-making
should be the LAST step.
 */
import net.horizonsend.ion.server.features.ai.module.steering.SimplexNoise
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Standard AI for controling a ship, uses context steering for determing movement, the goal of
 * the Agent class is to hold the current state of the agent, it context maps and output its
 * descion based on its surroundings. <br></br>
 * Notably the Agent class does nto direlty determine the movement on screen, it only sends
 * thurst and heading information to the physis engine. This makes it usefull in any context
 * that has a proper physics engine. <br></br>
 * The Agent class stores the following information:
 *
 *  * The current position and velocity
 *  * The current thrust and heading of the agent as determined by steering
 *  * A random [0,1] offset to be used for different behaviours
 *  * The master steering function that determines thrust and heading
 *  * #TODO A default list of tunable parameters that influence steering, affecting which
 * behaviors are on and their weight
 *  * A default mass and default max speed
 *
 */
class BasicSteeringModule(
	controller: AIController,
	var generalTarget : Supplier<AITarget?>) : SteeringModule(controller) {

    var squadTarget : Vector? = null
    var orbitTarget : Vector? = null
    var dangerTarget : Vector? = null
	val ship : ActiveStarship get() = controler.starship
	val MAXSPEED : Double = 20.0
	val offset = Math.random()

	val seekPos = generalTarget.get()?.getLocation()?.toVector()

	var thrustOut = Vector(0.0,0.0,1.0)
	var headingOut =  Vector(0.0,0.0,1.0)

    /**
     * Master steering function
     *
     *
     * Takes the current simulation sate and updates an agents heading and thrust using context
     * maps
     */
    override fun steer() : SteeringOutput{
        movementInterest.clearContext()
        rotationInterest.clearContext()
        danger.clearContext()

        wander.populateContext()

        offsetSeek.populateContext()

        faceSeek.populateContext()
        shieldAwareness.populateContext()

        //momentum.clearContext()
        squadFormation.populateContext()
        commitment.populateContext()

        borderDanger.clearContext()
        borderDanger.populateContext()

        shipDanger.clearContext()
        shipDanger.populateContext()

        particleDanger.populateContext()

        movementInterest.addContext(wander)
        movementInterest.addContext(offsetSeek)
        movementInterest.addContext(squadFormation)
        movementInterest.addContext(commitment)
        movementInterest.clipZero() //safeguard against neg weights

        rotationInterest.addContext(movementInterest)
        rotationInterest.multScalar(0.2)
        rotationInterest.addContext(faceSeek)
        var temp : ContextMap = object : ContextMap() {}
        temp.addScalar(1.0)
        temp.maskContext(shieldAwareness, threshold = 1.0)
        //rotationInterest.addContext(temp)
        rotationInterest.softMaskContext(shieldAwareness,threshold = 1.0)
        rotationInterest.clipZero()
        //rotationInterest.addContext(ContextMap.scaled(momentum,0.8))

        danger.addContext(borderDanger)
        danger.addContext(shipDanger)
        danger.addContext(shieldAwareness, scale = 0.5)
        danger.addContext(particleDanger)

        //There are no resources that talk about steering where the heading of a ship is
        // different from where its accelerating, enabling strafing and drifting, movement and
        // rotation is a bit of a chicken and egg problem, as the ideal heading is in the
        // direction of movement and the ideal movement is where the ship is heading, regardless
        // this is my wip solution to this problem, movement and rotation interests are generated,
        // and then mixed together with high velocity favoring rotation priority (strafing) and
        // low velocity favoring movement priority (accelerating to top speed). This mixing was
        // designed with starfigters and smaller ships in mind, and as more parameters are
        // introduced this could be changed for larger ships
        // A current issue is that if the movement and rotation maps are equal and opposing
        // magnitude then it will lead to an agent jittering under a certain ship.velocity threshold.
        //mixing
        var rotationMovementPrior =
            max(min(ship.velocity.length() / MAXSPEED*2, 1.0), 0.0).pow(1.0)
        //println(rotationMovementPrior)
        //\rotationMovementPrior = 0.0;
        temp = object : ContextMap(movementInterest) {}
        val movementWeight = (1 - rotationMovementPrior).pow(0.5)
        movementInterest.multScalar(movementWeight)
        movementInterest.addContext(
            ContextMap.scaled(
                rotationInterest,
                1 - movementWeight
            )
        )
        val rotationWeight = rotationMovementPrior.pow(0.5)
        if (rotationWeight < 0) throw RuntimeException()
        rotationInterest.multScalar(rotationWeight)
        rotationInterest.addContext(ContextMap.scaled(temp, 1 - rotationWeight))

        //masking, if the danger for a certain direction is greater than the threshold then it is
        // masked out
        movementInterest.softMaskContext(danger, 1.0)
        rotationInterest.softMaskContext(danger, 1.0)


        //decision time
        val heading = ship.forward.direction.multiply(2.0).add(rotationInterest.interpolatedMaxDir())
			.normalize()
        val thrustmag = movementInterest.lincontext!!.interpolotedMax()
        val thrust = movementInterest.interpolatedMaxDir().normalize().multiply(thrustmag)
		thrustOut = thrust
		headingOut = heading
        return SteeringOutput(heading, thrust)
    }

    /**
     * Interest maps correspond to how much a ship wants to move in a particular direction
     * (orbit, wander), and give rise to proactive/planning emergent behavior.
     *
     * This map is the final context to which to determine an agents thrust/movement
     */
    var movementInterest: ContextMap = object : ContextMap(linearbins = true) {}

    /**
     * Interest maps correspond to how much a ship wants to move in a particular direction
     * (orbit, wander), and give rise to proactive/planning emergent behavior.
     *
     * This map is the final context to which to determine an agents heading
     */
    var rotationInterest: ContextMap = object : ContextMap() {}

    /** Danger maps on the other hand indicate how bad moving in a particular direction is, with
     * a high enough danger leading to an agent avoiding that direction regardless of interest
     * (obstacle avoidance). This gives rise to reactive emergent behavior.
     *
     * This context is the final danger map that masks the interest contexts
     */
    var danger: ContextMap = object : ContextMap() {}

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
    var wander: ContextMap = object : ContextMap(linearbins = true) {
        val weight = 0.5
        val dotShift = 1.5
        val jitterRate = 2e4
        override fun populateContext() {
			clearContext()
            val timeoffset = offset * jitterRate
            val theta = SimplexNoise.noise(
                System.currentTimeMillis() % 1000000 / jitterRate / (ship.currentBlockCount / 10.0) + timeoffset,
                System.currentTimeMillis() % 1000000 / jitterRate /(ship.currentBlockCount / 10.0)+ timeoffset
            ) * 2 * Math.PI
            val desiredDir = Vector(0.0,0.0,1.0).rotateAroundY(theta)
            dotContext(desiredDir, dotShift, weight)
            lincontext!!.apply(lincontext!!.populatePeak(1.0, weight))
        }
    }

    /**
     * Whenever an agent changes direction this context map will make the agent commit by
     * discounting previous directions, greatly reduces jittering
     */
    var commitment: ContextMap = object : ContextMap() {
        val weight = 1.0
        val hist = 0.95
        var headingHist: ContextMap = object : ContextMap() {}

        override fun populateContext() {
            clearContext()
            headingHist.multScalar(hist)
            val velNorm = ship.forward.direction
            headingHist.dotContext(velNorm,1.0,(1-hist))
            dotContext(velNorm.multiply(-1.0),0.0,weight, clipZero = true)
            multScalar(-1.0)
            for (i in 0 until NUMBINS) {
                bins[i] *= headingHist.bins[i]
            }
        }
    }

    /**
     * Makes tha agent favor directions with same heading, magnitude increases with lower speed
     */
    var momentum: ContextMap = object :  ContextMap() {
        val weight = 2.0
        val falloff = 1.0
        val dotshift = -0.2
        val hist = 0.8
        override fun populateContext() {
            multScalar(hist)
            var velNorm = ship.forward.direction
            val mag = ship.velocity.length() / MAXSPEED
            //velNorm = if (mag > 1e-4) velNorm.normal() else
               // Vector2D(1.0, Math.random() * Math.PI * 2).toCartesian()
            dotContext(velNorm, dotshift, (1 - mag).pow(falloff) * weight * (1- hist), clipZero =
            false)
        }
    }

    /**
     * Makes the agent follow a orbit around the mouse. this does it by generating a target that
     * is offset by the orbit distance from the mouse and following that point. In order for the
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
    var offsetSeek: ContextMap = object : ContextMap() {
        val offsetDist = 700.0
        val weight = 1.0
        val dotShift = 0.0
        override fun populateContext() {
			seekPos ?: return
			clearContext()
			val shipPos = ship.centerOfMass.toVector()
            val center = seekPos.clone()
            val offset = center.add(shipPos.clone().multiply(-1.0))
            var dist = offset.length() + 1e-4
            val tetherl = dist / (ship.velocity.length() + 1e-5) * 2
            val frowardTether = shipPos.clone().add(ship.velocity.clone().normalize().multiply(tetherl))
            val tetherOffset = frowardTether.add(center.clone().multiply(-1.0)).normalize()
            val target = center.clone().add(tetherOffset.multiply(offsetDist))
            orbitTarget = target.clone()
            val targetOffset = target.add(shipPos.clone().multiply(-1.0))
            dist = targetOffset.length()
            targetOffset.normalize()
            dotContext(targetOffset, dotShift, weight)
        }
    }

    /**
     * Makes the agent face the mouse pointer
     *
     * This Context is meant for **`rotationInterest`**
     *
     *  * **`weight`** controls how strong this behavior is
     *  * **`maxWeight`** caps the maximum weight for a particular direction
     *  * **`falloff`** controls how this behavior scales with distance, higher
     * values will cause a lower weight (ie agent will only look at mouse from further away)
     *
     */
    var faceSeek: ContextMap = object : ContextMap() {
        val weight = 10.0
        val maxWeight = 0.0
        val falloff = 1500.0
        override fun populateContext() {
			seekPos ?: return
			clearContext()
            val target= seekPos.clone()
			val shipPos = ship.centerOfMass.toVector()
            val offset = target.add(shipPos.clone().multiply(-1.0))
            val dist = offset.length() + 1e-4
			offset.normalize()
            offset.multiply(weight).add(ship.forward.direction).normalize()
            dotContext(offset,0.0, dist / falloff)
            for (i in 0 until NUMBINS) {
                bins[i] = min(bins[i], maxWeight)
            }
        }
    }

    var squadFormation: ContextMap = object : ContextMap(linearbins = true){
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

    val shieldAwareness: ContextMap = object  : ContextMap() {
        val weight = 0.4
        val power = 2.0
        val histDecay = 0.99
        val criticalPoint = 0.1
        val incomingFire : ContextMap = object : ContextMap() {}
        override fun populateContext() {
            clearContext()
			val shipPos = ship.centerOfMass.toVector()
            incomingFire.multScalar(histDecay)
            for (shield in this@BasicSteeringModule.ship.shields) {
                if (this@BasicSteeringModule.ship.shields.size <= 1) return
                val center = shield.pos.toVector()
                val offset = center.add(shipPos.clone().multiply(-1.0)).normalize()
                incomingFire.dotContext(offset,-0.6,shield.recentDamage*(1-histDecay))

            }
            for (i in 0 until NUMBINS) {
                val dir = bindir[i]
                val rotatedCenters = this@BasicSteeringModule.ship.shields.map {transformCords(it,dir)}
                for (j in 0 until this@BasicSteeringModule.ship.shields.size) {
                    val shield = this@BasicSteeringModule.ship.shields[j]
                    val offset = rotatedCenters[j].clone().normalize()
                    val damage = ((shield.maxPower - shield.power)/
						(shield.maxPower.toDouble()*(1-criticalPoint))).pow(power)
                    val response = object : ContextMap() {}
                    response.dotContext(offset,-0.3,damage*weight)
                    for (k in 0 until NUMBINS) {
                        response.bins[k] *= incomingFire.bins[k]
                    }
                    bins[i] += response.bins.sum()
                }

            }
        }
    }

	private fun transformCords(shield: ShieldSubsystem, heading: Vector): Vector {
		val shipPos = ship.centerOfMass.toVector()
		val center = shield.pos.toVector()
		center.add(shipPos.clone().multiply(-1.0))
		return center.rotateAroundY(heading.angle(Vector(0.0, 0.0, 1.0)).toDouble())
	}

    /**
     * Makes the agent avoid other agents, the smaller the distance between agents the higher
     * the danger
     *
     * This Context is meant for **`danger`**
     *
     *  * **`falloff`** controls how this behavior scales with distance, higher
     * values will cause higher danger and agents will react faster
     *  * **`dotShift`** shifts the dot product, giving danger to orthogonal
     * directions
     */
    var shipDanger: ContextMap = object : ContextMap() {
        val falloff = 50.0
        val dotShift = 0.2
        val shipWeightSize = 10.0
        val shipWeightSpeed = 20.0

        override fun populateContext() {
            var mindist = 1e10
			val shipPos = ship.centerOfMass.toVector()
            for (otherShip in ActiveStarships.getInWorld(ship.world)) {
                if (otherShip == this@BasicSteeringModule.ship) continue
				val othershipPos = otherShip.centerOfMass.toVector()
                val target = othershipPos.clone().add(lookAhead(otherShip, futuremod = 1.0, useMax = true))
                val targetOffset = target.add(shipPos.clone().multiply(-1.0))
                val targetDist = targetOffset.length() + 1e-4
                if (targetDist < mindist) {
                    mindist = targetDist
                    dangerTarget = target
                }
                targetOffset.normalize()
                val dangerWeight = (otherShip.currentBlockCount / shipWeightSize) * (MAXSPEED /shipWeightSpeed)
                dotContext(targetOffset,dotShift,(falloff*dangerWeight)/targetDist, power = 1.0, true)
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
    var particleDanger: ContextMap = object : ContextMap() {
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
    var borderDanger: ContextMap = object : ContextMap() {
        var falloff = 100
        var dotShift = 0.2
        override fun populateContext() {
			val worldborder = ship.world.worldBorder
			val borderCenter = worldborder.center.toVector()
			val radius = worldborder.size / 2
            for (i in 0 until NUMBINS) {
                //north border
                var dir = Vector(0.0,0.0, -1.0)
                bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.z.toDouble(), borderCenter.z - radius)
                //south border
                dir = Vector(0.0,0.0, 1.0)
                bins[i] += calcDanger(bindir[i],dir,ship.centerOfMass.z.toDouble(), borderCenter.z + radius)
                //west border
                dir = Vector(-1.0,0.0, 0.0)
                bins[i] += calcDanger(bindir[i], dir, ship.centerOfMass.x.toDouble(), 	borderCenter.x - radius)
                //east border
                dir = Vector(1.0,0.0, 0.0)
                bins[i] += calcDanger(bindir[i],dir,ship.centerOfMass.x.toDouble(),borderCenter.x	+ radius)
            }
        }

        private fun calcDanger(bindir: Vector, dir: Vector, val1: Double, val2: Double): Double {
            val proximity = (abs(val1 - val2) + 1e-4)
            var mag = (bindir.dot(dir) + dotShift) * falloff / proximity
            mag = if (mag < 0) 0.0 else mag
            return mag
        }
    }

    private fun lookAhead(other: ActiveStarship,
                          pos : Vector = other.centerOfMass.toVector(),
                          futuremod : Double = 1.0, useMax : Boolean = false) : Vector {
        val offset = pos.add(ship.centerOfMass.toVector().multiply(-1.0))
        val dist = offset.length() + 1e-4
        val vel = if (useMax) {MAXSPEED} else {ship.velocity.length()}
        val t = dist/(vel*futuremod)
        val lookAhead =  other.velocity.clone().multiply(t)
        return lookAhead
    }

	/*
    private fun lookAhead(pos : Vector2D, velocity : Vector2D,
                          futuremod : Double = 1.0, useMax : Boolean = false) : Vector2D {
        val offset = pos.add(ship.pos.mult(-1.0))
        val dist = offset.mag() + 1e-4
        val vel = if (useMax) {ship.MAXSPEED} else {ship.velocity.mag()}
        val t = dist/(vel*futuremod)
        val lookAhead =  velocity.mult(t)
        return lookAhead
    }
    /
	 */

}
