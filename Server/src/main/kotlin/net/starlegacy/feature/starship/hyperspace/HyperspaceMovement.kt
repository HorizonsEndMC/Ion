package net.starlegacy.feature.starship.hyperspace

import net.horizonsend.ion.server.IonServer.Companion.Ion
import kotlin.math.roundToInt
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.distance
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class HyperspaceMovement(val ship: ActiveStarship, val speed: Int, val dest: Location) : BukkitRunnable() {
	var x = ship.centerOfMassVec3i.x.toDouble()
	var z = ship.centerOfMassVec3i.z.toDouble()
	private val direction = dest.toVector().subtract(ship.centerOfMassVec3i.toVector()).normalize()
	private val totalDistance = remainingDistance()
	private var travelled = 0.0

	init {
		runTaskTimer(Ion, 2, 2)
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
			ship.sendMessage("&cShip caught by a mass shadow! Mass Shadow: ${shadow.description} at ${shadow.x}, ${shadow.z} " +
					"with radius ${shadow.radius} (${shadow.distance} blocks away)")
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