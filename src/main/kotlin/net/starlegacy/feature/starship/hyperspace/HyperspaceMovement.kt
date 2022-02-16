package net.starlegacy.feature.starship.hyperspace

import kotlin.math.roundToInt
import net.starlegacy.PLUGIN
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.distance
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class HyperspaceMovement(val ship: ActiveStarship, val speed: Int, val dest: Location) : BukkitRunnable() {
	var x = ship.centerOfMass.x.toDouble()
	var z = ship.centerOfMass.z.toDouble()
	private val direction = dest.toVector().subtract(ship.centerOfMass.toVector()).normalize()
	private val totalDistance = remainingDistance()
	private var travelled = 0.0

	init {
		runTaskTimer(PLUGIN, 20, 20)
	}

	private fun remainingDistance() = distance(x, 0.0, z, dest.x, 0.0, dest.z)

	override fun run() {
		if (!ActiveStarships.isActive(ship)) {
			cancel()
			return
		}

		x += direction.x * speed
		z += direction.z * speed
		travelled += speed

		val shadow: MassShadows.MassShadowInfo? = MassShadows.find(dest.world, x, z)
		if (shadow != null) {
			ship.sendMessage("&cShip caught by a mass shadow! Mass shadow info: $shadow")
			cancel()
			return
		}

		if (travelled < totalDistance) {
			val percent = (travelled / totalDistance * 100).roundToInt()
			ship.sendActionBar("&bHyperspace Progress: ${travelled.roundToInt()}/${totalDistance.roundToInt()} ($percent%)")
			return
		}

		ship.sendActionBar("&aJump complete")
		Hyperspace.completeJumpMovement(this)
	}

	/** If the ship is still active, this will pull it out */
	override fun cancel() {
		super.cancel()

		if (!Hyperspace.isMoving(ship)) {
			return
		}

		Hyperspace.cancelJumpMovement(this)
	}
}
