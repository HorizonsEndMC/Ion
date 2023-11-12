package net.horizonsend.ion.server.features.starship.active.ai.engine.positioning

import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController

class RotatingAxisStandoffPositioningEngine(
	controller: ActiveAIController,
	target: AITarget?,
	standoffDistance: Double
) : AxisStandoffPositioningEngine(controller, target, standoffDistance) {

}
