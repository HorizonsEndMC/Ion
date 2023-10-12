package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

/** A controller where the objective changes */
interface VariableObjectiveController {
	fun getObjective(): Vec3i?
}
