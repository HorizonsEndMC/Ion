package net.starlegacy.feature.starship.hyperspace

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackAction
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.schema.nations.CapturableStation
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.litote.kmongo.eq
import kotlin.math.max
import kotlin.math.min

class HyperspaceWarmup(val ship: ActiveStarship, var warmup: Int, val dest: Location, val drive: HyperdriveSubsystem) :
	BukkitRunnable() {
	init {
		if (ship is ActivePlayerStarship) {
			warmup -= (max(
				min(
					CapturableStation.count(CapturableStation::nation eq PlayerCache[ship.pilot!!].nation).toInt(),
					6
				) - 2, 0
			) * 1.5).toInt()
			warmup = max(warmup, 0)
		}

		runTaskTimer(Ion, 20L, 20L)
	}

	private var seconds = 0

	override fun run() {
		seconds++
		ship.onlinePassengers.forEach { player ->
			player.sendFeedbackAction(
				FeedbackType.INFORMATION,
				"Hyperdrive Warmup: $seconds/$warmup seconds"
			)
		}

		if (!drive.isIntact()) {
			ship.onlinePassengers.forEach { player ->
				player.sendFeedbackAction(
					FeedbackType.ALERT,
					"Drive damaged! Jump failed!"
				)
			}
			cancel()
			return
		}

		if (MassShadows.find(ship.world, ship.centerOfMass.x.toDouble(), ship.centerOfMass.z.toDouble()) != null) {
			ship.onlinePassengers.forEach { player ->
				player.sendFeedbackAction(
					FeedbackType.USER_ERROR,
					"Ship is within Gravity Well, jump cancelled"
				)
			}
			cancel()
			return
		}

		if (seconds < warmup) {
			return
		}
		ship.onlinePassengers.forEach { player -> player.sendFeedbackAction(FeedbackType.INFORMATION, "Jumping") }
		Hyperspace.completeJumpWarmup(this)
	}

	override fun cancel() {
		super.cancel()

		if (!Hyperspace.isWarmingUp(ship)) {
			return
		}

		Hyperspace.cancelJumpWarmup(this)
	}
}