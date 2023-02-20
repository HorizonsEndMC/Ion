package net.starlegacy.feature.starship.hyperspace
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import org.litote.kmongo.util.idValue

class HyperspaceMarker(val org : Location, val ship : ActivePlayerStarship, val dest: Location) {
	/** How long an arrow lasts after a ship moves to hyperspace */
	private val ARROWDURATION = 30
	/** bol to hold if the marker should draw the arrow*/
	var isArrow  = true
	/** bol for if ship is already moving in hyperspace*/
	var inHyperspace = false
	/** list of size 4 to hold for arrows vectors
	  * 0: origin
	  * 1: end
	  * 2: counterclockwise end
	  * 3: clockwise end */
	var arrowVects = calculateArrow()
	/** the position of the ship relative to the space world */
	var pos  = org.toVector()
	/** the associated hyperspace movement */
	lateinit var movement : HyperspaceMovement

	/** Id of the marker to use in adding or removing markers in the render
	 * currently just the ship id*/
	var id = ship.idValue

	private var seconds = 0

	/** Calculates the arrow vectors
	 * #TODO: do the math for dymap arrows*/
	private fun calculateArrow(): MutableList<Vector> {
		var output = mutableListOf<Vector>()
		output += org.toVector()
		output += output[0].clone().add(Vector(100.0,0.0,0.0))
		output += output[1].clone().add(Vector(-30.0,0.0,-10.0))
		output += output[1].clone().add(Vector(-30.0,0.0,10.0))
		return output
	}

	fun updatePos() {
		if (inHyperspace) {
			pos = Vector(movement.x,0.0,movement.z)
		}
	}
	/** Updates the state of the marker*/
	fun tick() {
		if (inHyperspace) {
			seconds++
		}
		if ((seconds >= ARROWDURATION) and isArrow) {
			isArrow = false
		}
		updatePos()
	}



	fun isHyperspaceWorld(world: World): Boolean = world.name.endsWith("_Hyperspace")
}
