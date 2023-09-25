package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.util.Vector

class AutoCruiseAIController(
	starship: ActiveStarship,
	endPoint: Vector,
	maxSpeed: Int,
	val aggressivenessLevel: AggressivenessLevel
) : AIController(starship, "autoCruise") {
	var ticks = 0

	override val pilotName: Component = starship.getDisplayNameComponent().append(Component.text(" [NEUTRAL]", NamedTextColor.YELLOW))

	var destination = endPoint
	val direction: Vector get() = destination.clone().subtract(getCenter().toVector())

	private var speedLimit = maxSpeed

	override fun tick() {
		Tasks.async {
			ticks++

			// Only tick evey second
			if ((ticks % 20) != 0) return@async

			var shouldDestroy = false

			val nearbyShip = getNearbyShips(0.0, aggressivenessLevel.engagementDistance) { starship, _ ->
				starship.controller !is AIController
			}.firstOrNull()

			nearbyShip?.let {
				starship.controller = StarfighterCombatController(
					starship,
					it,
					this,
					aggressivenessLevel
				)
			}

			val origin = getCenter().toVector()
			if (distanceSquared(origin, destination) <= 100000) shouldDestroy = true

			// TODO vanish if near world border

			if (shouldDestroy) {
				despawn()
				return@async
			}

			val controlledShip = starship as? ActiveControlledStarship ?: return@async
			starship.speedLimit = speedLimit

			Tasks.sync {
				AIControlUtils.faceDirection(this, vectorToBlockFace(direction))
				StarshipCruising.startCruising(this, controlledShip, direction)
			}

			super.tick()
		}
	}
}
