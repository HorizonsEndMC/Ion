package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.kyori.adventure.text.Component
import org.bukkit.util.Vector

class AutoCruiseAIController(
	starship: ActiveStarship,
	endPoint: Vector,
	displayName: Component,
) : AIController(starship, "autoCruise") {
	var ticks = 0

	override var pilotName: Component = displayName

	var destination = endPoint
		set(value) {
			direction = value.clone().subtract(getCenter().toVector())
			field = value
		}

	var direction: Vector = endPoint.clone().subtract(getCenter().toVector())


	override fun tick() {
		Tasks.async {
			ticks++

			if ((ticks % 20) != 0) return@async // Only tick evey second
			val controlledShip = starship as? ActiveControlledStarship ?: return@async

			val origin = getCenter().toVector()

			Tasks.sync {
				AIControlUtils.faceDirection(this, vectorToBlockFace(direction))
				StarshipCruising.startCruising(this, controlledShip, direction)
			}

			if (distanceSquared(origin, destination) <= 10000) {
				Tasks.sync {
					StarshipCruising.stopCruising(this, controlledShip)
				}
				// Once it reaches its destination, wait 30 seconds then vanish, if not damaged.

				//TODO do all of that
				StarshipDestruction.vanish(starship)
			}

			super.tick()
		}
	}
}
