import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.util.Vector
import java.util.concurrent.ConcurrentHashMap

/**
Context steering based control for AI
See https://andrewfray.wordpress.com/2013/03/26/context-behaviours-know-how-to-share/
https://www.gameairo.com/GameAIPro2/GameAIPro2_Chapter18_Context_Steering_Behavior-Driven_Steering_at_the_Macro_Scale.pdf
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

/**
 * Standard AI for controling a ship, uses context steering for determing movement, the goal of
 * the `SteeringModule` class is to hold the current state of the agent, it context maps and output its
 * descion based on its surroundings. <br></br>
 * Notably the SteeringModule class does nto direlty determine the movement on a ship, it only sends
 * thurst and heading information to the movement module. This makes it usefull in any movement mode <br></br>
 * The `SteeringModule` class stores the following information:
 *
 *  * The attached ship
 *  * The current thrust and heading of the agent as determined by steering
 *  * A random [0,1] offset to be used for different behaviours
 *  * A estimated max speed
 *  * The master steering function that determines thrust and heading
 *  * Each steering module has its own configuration, each Context also has its own configuration
 */
abstract class SteeringModule(var controler: AIController) : AIModule(controler){
	val ship : Starship get() = controler.starship
	val contexts = mutableMapOf<String,ContextMap>()
	val offset = Math.random()

	var thrustOut = Vector(0.0,0.0,1.0)
	var headingOut =  Vector(0.0,0.0,1.0)
	var throttleOut = 0.0

	var orbitTarget : Vector? = null
	var dangerTarget : Vector? = null
	val obstructions = ConcurrentHashMap<Vec3i, Long>()

	open fun steer() {
		populateContexts()
	}

	fun getThrust(): Vector {
		return thrustOut
	}

	fun getHeading(): Vector {
		return headingOut
	}

	fun getThrottle(): Double {
		return throttleOut
	}

	private fun populateContexts() {
		contexts.forEach() {it.value.populateContext()}
	}

	fun decision(thrustContext : ContextMap, headingContext : ContextMap) {
		val heading = headingContext.maxDir().setY(0)
		heading.normalize()
		val thrustMag = thrustContext.lincontext!!.interpolotedMax()
		val thrust = thrustContext.maxDir().normalize()
		thrustOut = thrust
		headingOut = heading
		throttleOut = thrustMag
	}

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		val time = System.currentTimeMillis()
		location?.let {
			obstructions[location] = time
		}
	}

}
