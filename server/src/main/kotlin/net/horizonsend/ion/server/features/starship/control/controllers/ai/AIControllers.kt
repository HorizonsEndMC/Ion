package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.miscellaneous.utils.keysSortedByValue
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location

object AIControllers {
	fun dumbAI(starship: ActiveStarship): AIController = createController(
		starship,
		"dumbAI",
		displayName = text("eeeevil solesey ship", NamedTextColor.DARK_RED)
	)

	private fun createController(
		starship: ActiveStarship,
		name: String,
		displayName: Component = text("name")
	): AIController {
		return object : AIController(starship, name) {
			override val pilotName: Component = displayName
			override fun tick() {
				val location = starship.centerOfMass.toLocation(starship.world)
				val nearestPlayer = getNearestPlayer(this, location)

				val direction =
					nearestPlayer?.location?.toVector()?.subtract(starship.centerOfMass.toVector())

				direction?.let { AIControlUtils.faceDirection(this, vectorToBlockFace(direction)) }

				AIControlUtils.shiftFlyTowardsPlayer(this, nearestPlayer)

				nearestPlayer?.let {
					AIControlUtils.shootAtPlayer(this, nearestPlayer, true)
					AIControlUtils.shootAtPlayer(this, nearestPlayer, false, weaponSet = "phasers")
				}
				super.tick()
			}
		}
	}

	fun getNearestPlayer(controller: Controller, location: Location) = IonServer.server.onlinePlayers
		.filter { it.world == controller.starship.world }
		.associateWith { it.location.distance(location) }
		.filter { it.value <= 5000 }
		.filter { it.value >= 50 }
		.keysSortedByValue()
		.firstOrNull()
}
