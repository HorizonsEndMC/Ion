package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.TimeUnit

abstract class AIController(
	starship: ActiveStarship,
	name: String,
	val aggressivenessLevel: AggressivenessLevel
) : Controller(starship, name) {
	override val pilotName: Component get() = Component.text()
		.append(Component.text("AI Controller "))
		.append(aggressivenessLevel.displayName)
		.build()

	override var isShiftFlying: Boolean = false
	override var pitch: Float = 0f
	override var yaw: Float = 0f
	override var selectedDirectControlSpeed: Int = 0

	var lastRotation: Long = 0L

	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false

	override fun getDisplayName(): Component = pilotName

	override fun rewardXP(xp: Int) {}
	override fun rewardMoney(credits: Double) {}

	fun getCenter(): Location = starship.centerOfMass.toLocation(starship.world)

	// Begin utility functions
	fun scheduleDespawn() = Tasks.sync {
		val task = DespawnTask(this)

		task.runTaskTimerAsynchronously(IonServer, 0L, 20L)
	}

	class DespawnTask(val controller: AIController) : BukkitRunnable() {
		private val DESPAWN_TIME: Long = TimeUnit.MINUTES.toMillis(5)

		private val startTime = System.currentTimeMillis()
		private val starship = controller.starship

		private val locationVector = starship.centerOfMass.toVector()

		private fun checkCancel(): Boolean {
			starship.lastDamagedOrNull()?.let {
				if (it <= System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)) return true
			}

			if (!ActiveStarships.isActive(starship)) return true

			val nearestPlayerDistance = starship.world.players.minOfOrNull { distance(it.location.toVector(), locationVector) }
			nearestPlayerDistance?.let { if (it <= 500.0) return true }

			return false
		}

		override fun run() {
			if (checkCancel()) {
				cancel()
				return
			}

			// If it hasn't been the whole despawn time yet, return
			if (startTime + DESPAWN_TIME > System.currentTimeMillis()) return

			Tasks.sync {
				StarshipDestruction.vanish(starship)
			}
		}
	}

	fun getNearbyShips(
		minimumDistance: Double,
		maximumDistance: Double,
		additionalFilter: (ActiveStarship, Double) -> Boolean = { _, _ -> true }
	): Set<ActiveStarship> {
		val worldShips = ActiveStarships.getInWorld(starship.world)

		val byDistance = worldShips.associateWith { it.centerOfMass.toLocation(it.world).distance(getCenter()) }

		val inRange = byDistance
			.filter { it.key.controller != this }
			.filter { it.value <= maximumDistance }
			.filter { it.value >= minimumDistance }
			.filter { additionalFilter(it.key, it.value) }

		return inRange.keys
	}
}
