package net.starlegacy.feature.starship.hyperspace
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.PI

class HyperspaceMarker(val org: Location, var ship: ActiveStarship, val dest: Location) {

	/** How long an arrow lasts after a ship moves to hyperspace */
	private val ARROWDURATION = 5

	/** bol to hold if the marker should draw the arrow*/
	var isArrow = true

	/** bol for if ship is already moving in hyperspace*/
	var inHyperspace = false

	/** list of size 4 to hold for arrows vectors
	 * 0: origin
	 * 1: end
	 * 2: counterclockwise end
	 * 3: clockwise end */
	var arrowVects = calculateArrow()

	/** the position of the ship relative to the space world */
	var pos = org.toVector()

	/** the associated hyperspace movement */
	lateinit var movement: HyperspaceMovement

	/** Id of the marker to use in adding or removing markers in the render
	 * currently just the ship id*/
	var id = 888888

	private var seconds = 0

	/** Calculates the arrow vectors*/
	private fun calculateArrow(): MutableList<Vector> {
		val k = 1000.0
		val l = 200.0
		val theta = 2 * PI / 5 + PI / 2
		val output = mutableListOf<Vector>()
		output += org.toVector()

		val v = dest.toVector().clone().subtract(org.toVector())
		val u = v.clone().normalize()
		val dir = u.clone().multiply(minOf(v.length(), k))
		output += output[0].clone().add(dir)
		output += output[1].clone().add(u.clone().rotateAroundY(theta).multiply(l))
		output += output[1].clone().add(u.clone().rotateAroundY(-theta).multiply(l))
		return output
	}

	private fun updatePos() {
		if (inHyperspace) {
			pos = Vector(movement.x, 0.0, movement.z)
		}
	}

	/** Updates the state of the marker*/
	fun tick() {
		println("marker ticking")
		if (inHyperspace) {
			seconds++
		}
		if ((seconds >= ARROWDURATION) and isArrow) {
			isArrow = false
		}
		updatePos()
	}
}
