package net.starlegacy.feature.starship.hyperspace

import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.distance
import net.starlegacy.util.toVector
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.roundToInt

class HyperspaceMovement(val ship: ActiveStarship, val speed: Int, val dest: Location) : BukkitRunnable() {
	var x = ship.centerOfMass.x.toDouble()
	var z = ship.centerOfMass.z.toDouble()
	private val direction = dest.toVector().subtract(ship.centerOfMass.toVector()).normalize()
	private val totalDistance = remainingDistance()
	private var travelled = 0.0

	init {
		runTaskTimer(IonServer, 2, 2)
	}

	private fun remainingDistance() = distance(x, 0.0, z, dest.x, 0.0, dest.z)

	override fun run() {
		if (!ActiveStarships.isActive(ship)) {
			cancel()
			return
		}
		(ship as? ActivePlayerStarship)?.pilot?.rewardAchievement(Achievement.USE_HYPERSPACE)

		x += direction.x * speed
		z += direction.z * speed
		travelled += speed

		val shadow: MassShadows.MassShadowInfo? = MassShadows.find(dest.world, x, z)
		if (shadow != null) {
			ship.onlinePassengers.forEach { player ->
				player.alertAction(
					"Ship caught by a mass shadow! Mass Shadow: ${shadow.description} at ${shadow.x}, ${shadow.z} " +
						"with radius ${shadow.radius} (${shadow.distance} blocks away)"
				)
			}

			cancel()
			return
		}
		if (travelled < totalDistance) {
			val percent = (travelled / totalDistance * 100).roundToInt()
			ship.onlinePassengers.forEach { player ->
				player.informationAction(
					"Hyperspace Progress: ${travelled.roundToInt()}/${totalDistance.roundToInt()} ($percent%)"
				)
			}
			return
		}

		ship.onlinePassengers.forEach { player -> player.informationAction("Jump complete") }
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
