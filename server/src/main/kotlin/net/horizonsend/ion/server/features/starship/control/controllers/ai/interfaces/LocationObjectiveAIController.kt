package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.util.Vector

interface LocationObjectiveAIController {
	fun getObjective(): Vec3i?

	fun getDirection(origin: Vec3i, destination: Vec3i): Vector = destination.minus(origin).toVector()
}
