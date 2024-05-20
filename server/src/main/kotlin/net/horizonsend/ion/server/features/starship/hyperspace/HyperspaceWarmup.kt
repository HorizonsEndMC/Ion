package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.subsystem.misc.HyperdriveSubsystem
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Vibration
import org.bukkit.Vibration.Destination.BlockDestination
import org.bukkit.scheduler.BukkitRunnable
import org.litote.kmongo.eq
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class HyperspaceWarmup(
    val ship: ActiveStarship,
    var warmup: Int,
    val dest: Location,
    val drive: HyperdriveSubsystem,
    private val useFuel: Boolean
) : BukkitRunnable() {
	init {
		if (ship is ActiveControlledStarship) {
			(ship.controller as? PlayerController)?.player?.let {
				val stationCount = CapturableStation.count(CapturableStation::nation eq PlayerCache[it].nationOid).toInt()

				warmup -= (max(min(stationCount, 6) - 2, 0) * 1.5).toInt()
			}

			warmup = max(warmup, 0)
		}

		runTaskTimer(IonServer, 20L, 20L)
	}

	private var seconds = 0

	override fun run() {
		seconds++
		ship.onlinePassengers.forEach { player ->
			player.informationAction(
				"Hyperdrive Warmup: $seconds/$warmup seconds"
			)
		}

		if (!drive.isIntact()) {
			ship.onlinePassengers.forEach { player ->
				player.alertAction(
					"Drive damaged! Jump failed!"
				)
			}
			cancel()
			return
		}

		if (MassShadows.find(ship.world, ship.centerOfMass.x.toDouble(), ship.centerOfMass.z.toDouble()) != null) {
			ship.onlinePassengers.forEach { player ->
				player.userErrorAction("Ship is within Gravity Well, jump cancelled")
			}
			cancel()
			return
		}

		if (!PilotedStarships.isPiloted(ship as ActiveControlledStarship)) {
			// Do this separate since the unpiloted controller doesn't pass through information, if it duplicates, oh well
			(ship.controller as? PlayerController)?.player?.userError("Starship became unpiloted, hyperspace warmup cancelled.")

			ship.userError("Starship became unpiloted, hyperspace warmup cancelled.")

			cancel()
		}

		displayParticles()

		if (seconds < warmup) {
			return
		}

		if (useFuel) {
			require(drive.hasFuel()) { "Hyperdrive doesn't have fuel!" }
			drive.useFuel()
		}

		ship.informationAction("Jumping")
		Hyperspace.completeJumpWarmup(this)
	}

	// 2.5 root of block count to get radius
	// 500 block starfighter would be 12 blocks
	// 12000 block destroyer would be 42
	private val particleRadius = ship.initialBlockCount.toDouble().pow(2.0/5.0)
	private val startLocation = drive.pos.toLocation(ship.world)
	private val count = maxOf(100, 50 / (seconds - warmup) + 20)

	private fun displayParticles() {
		ship.world.spawnParticle(
			Particle.VIBRATION,
			startLocation,
			count,
			particleRadius,
			particleRadius,
			particleRadius,
			Vibration(BlockDestination(startLocation),
				100
			)
		)
	}

	override fun cancel() {
		super.cancel()

		if (!Hyperspace.isWarmingUp(ship)) {
			return
		}

		Hyperspace.cancelJumpWarmup(this)
	}
}
