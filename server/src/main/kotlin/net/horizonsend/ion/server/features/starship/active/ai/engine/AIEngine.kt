package net.horizonsend.ion.server.features.starship.active.ai.engine

import co.aikar.commands.ConditionFailedException
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** An executable class containing logic for the positioning, navigation, and control of the starship */
abstract class AIEngine(val controller: AIController) {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	val starship = controller.starship
	val world get() = starship.world

	open fun tick() {}

	open fun onMove(movement: StarshipMovement) {}

	open fun onBlocked(movement: StarshipMovement, reason: ConditionFailedException) {}

	open fun onDamaged(damager: Damager) {}

	open fun shutDown() {}

	open fun getCenter(): Location = controller.getCenter()
	open fun getCenterVec3i(): Vec3i = controller.starship.centerOfMass

	fun adjust(movement: StarshipMovement, vec3i: Vec3i): Vec3i = Vec3i(
		movement.displaceX(vec3i.x, vec3i.z),
		movement.displaceY(vec3i.y),
		movement.displaceZ(vec3i.z, vec3i.x)
	)

	fun adjust(movement: StarshipMovement, location: Location): Location = movement.displaceLocation(location)
}
