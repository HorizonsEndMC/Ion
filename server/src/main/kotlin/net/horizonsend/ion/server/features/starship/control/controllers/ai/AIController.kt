package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.AggressiveLevelAIController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.utils.AggressivenessLevel
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.highlightBlock
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.TimeUnit

abstract class AIController(
	starship: ActiveStarship,
	name: String,
	damager: Damager,
	override val aggressivenessLevel: AggressivenessLevel
) : Controller(damager, starship, name),
	AggressiveLevelAIController {
	override val pilotName: Component get() = Component.text()
		.append(Component.text("AI Controller "))
		.append(aggressivenessLevel.displayName)
		.build()

	override var isShiftFlying: Boolean = false
	override var pitch: Float = 0f
	override var yaw: Float = 0f
	override var selectedDirectControlSpeed: Int = 1

	var lastRotation: Long = 0L

	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false

	/** Use the direct control center as a sort of cache to avoid the type conversion if possible */
	fun getCenter(): Location = (starship as? ActiveControlledStarship)?.directControlCenter ?: starship.centerOfMass.toLocation(starship.world)
	fun getCenterVec3i(): Vec3i = starship.centerOfMass

	fun getWorld(): World = starship.world

	override fun tick() {
		highlightComputer()
		super.tick()
	}

	private fun highlightComputer() = Tasks.sync {
		val controlledStarship = starship as? ActiveControlledStarship ?: return@sync
		val computerLoc = Vec3i(controlledStarship.data.blockKey)

		val location = getCenter()
		val players = location.getNearbyPlayers(160.0)

		for (player in players) {
			player.highlightBlock(computerLoc, 2L)
		}
	}

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

	val nonAICheck: (ActiveStarship, Double) -> Boolean = { starship, _ -> starship.controller !is AIController }

	override fun toString(): String {
		return "$name[${starship.identifier}]"
	}
}
