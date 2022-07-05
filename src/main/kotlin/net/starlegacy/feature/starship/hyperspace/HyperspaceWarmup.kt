package net.starlegacy.feature.starship.hyperspace

import kotlin.math.max
import kotlin.math.min
import net.starlegacy.PLUGIN
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.schema.nations.CapturableStation
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.litote.kmongo.eq

class HyperspaceWarmup(val ship: ActiveStarship, var warmup: Int, val dest: Location, val drive: HyperdriveSubsystem) :
	BukkitRunnable() {
	init {
		if (ship is ActivePlayerStarship) {
			warmup -= max(min(CapturableStation.count(CapturableStation::nation eq PlayerCache[ship.pilot!!].nation).toInt(), 6) - 1, 0) * 2
			warmup = max(warmup, 0)
		}

		runTaskTimer(PLUGIN, 20L, 20L)
	}

	private var seconds = 0

	override fun run() {
		seconds++
		ship.sendActionBar("&eHyperdrive Warmup&8: &c$seconds&8/&4$warmup &eseconds")
		if (!drive.isIntact()) {
			ship.sendMessage("&cDrive damaged! Jump failed!")
			cancel()
			return
		}
		if (MassShadows.find(ship.world, ship.centerOfMass.x.toDouble(), ship.centerOfMass.z.toDouble())!= null){
			ship.sendMessage("&cShip is within Gravity Well, jump cancelled")
			cancel()
			return
		}

		if (seconds < warmup) {
			return
		}
		ship.sendActionBar("&aJumping")
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
