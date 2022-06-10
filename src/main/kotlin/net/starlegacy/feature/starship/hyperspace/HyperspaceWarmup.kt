package net.starlegacy.feature.starship.hyperspace

import net.starlegacy.PLUGIN
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.HyperdriveSubsystem
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class HyperspaceWarmup(val ship: ActiveStarship, val warmup: Int, val dest: Location, val drive: HyperdriveSubsystem) :
	BukkitRunnable() {
	init {
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
