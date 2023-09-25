package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.scheduler.BukkitRunnable

abstract class AIController(
	starship: ActiveStarship,
	name: String
) : Controller(starship, name) {
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
	fun despawn() = Tasks.sync {
		val controlledShip = starship as? ActiveControlledStarship ?: return@sync

		// Once it reaches its destination, wait 30 seconds then vanish, if not damaged.
		StarshipCruising.stopCruising(this, controlledShip)

		// If it was damaged in the last 5 minutes don't do anything
		if (this.starship.lastDamaged() <= System.currentTimeMillis() - (1000 * 60 * 5)) return@sync

		// Wait 30 seconds to vanish
		Tasks.syncDelay(20 * 30) {
			StarshipDestruction.vanish(starship)
		}
	}

	class DespawnTask(controller: AIController) : BukkitRunnable() { //TODO
		val startTime = System.currentTimeMillis()

		override fun run() {
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
