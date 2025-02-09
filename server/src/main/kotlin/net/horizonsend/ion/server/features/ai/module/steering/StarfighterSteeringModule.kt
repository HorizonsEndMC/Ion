package net.horizonsend.ion.server.features.ai.module.steering

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.steering.AISteeringConfiguration
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.steering.context.AvoidIlliusContext
import net.horizonsend.ion.server.features.ai.module.steering.context.BlankContext
import net.horizonsend.ion.server.features.ai.module.steering.context.BorderDangerContext
import net.horizonsend.ion.server.features.ai.module.steering.context.FaceSeekContext
import net.horizonsend.ion.server.features.ai.module.steering.context.FleetGravityContext
import net.horizonsend.ion.server.features.ai.module.steering.context.MovementInterestContext
import net.horizonsend.ion.server.features.ai.module.steering.context.ObstructionDangerContext
import net.horizonsend.ion.server.features.ai.module.steering.context.OffsetSeekContext
import net.horizonsend.ion.server.features.ai.module.steering.context.ShieldAwarenessContext
import net.horizonsend.ion.server.features.ai.module.steering.context.ShipDangerContext
import net.horizonsend.ion.server.features.ai.module.steering.context.WanderContext
import net.horizonsend.ion.server.features.ai.module.steering.context.WorldBlockDangerContext
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.util.function.Supplier

class StarfighterSteeringModule(
	controller: AIController,
	difficulty : DifficultyModule,
	generalTarget: Supplier<AITarget?>,
	orbitDist : Supplier<Double>,
	override val configSupplier: Supplier<AISteeringConfiguration.BasicSteeringConfiguration> = Supplier(ConfigurationFiles.aiSteeringConfiguration()::starfighterBasicSteeringConfiguration)
) : BasicSteeringModule(controller, difficulty ,generalTarget){


	init {
		/**
		 * Interest maps correspond to how much a ship wants to move in a particular direction
		 * (orbit, wander), and give rise to proactive/planning emergent behavior.
		 *
		 * This map is the final context to which to determine an agents thrust/movement
		 */
		contexts["movementInterest"] = MovementInterestContext()
		/**
		 * Interest maps correspond to how much a ship wants to move in a particular direction
		 * (orbit, wander), and give rise to proactive/planning emergent behavior.
		 *
		 * This map is the final context to which to determine an agents heading
		 */
		contexts["rotationInterest"]= BlankContext()
		/** Danger maps on the other hand indicate how bad moving in a particular direction is, with
		 * a high enough danger leading to an agent avoiding that direction regardless of interest
		 * (obstacle avoidance). This gives rise to reactive emergent behavior.
		 *
		 * This context is the final danger map that masks the interest contexts
		 */
		contexts["danger"]= BlankContext()
		contexts["wander"] = WanderContext(ship,offset)
		contexts["offsetSeek"] = OffsetSeekContext(ship, generalTarget,this,
			Supplier(ConfigurationFiles.aiContextConfiguration()::starfighterOffsetSeekContextConfiguration), offsetSupplier = orbitDist)
		contexts["faceSeek"]= FaceSeekContext(ship,generalTarget,difficulty,
			Supplier(ConfigurationFiles.aiContextConfiguration()::starfighterFaceSeekContextConfiguration))
		contexts["fleetGravity"] = FleetGravityContext(ship)
		contexts["avoidIllius"] = AvoidIlliusContext(ship)
		contexts["shieldAwareness"] = ShieldAwarenessContext(ship,difficulty)
		contexts["shipDanger"] = ShipDangerContext(ship, { config.defaultMaxSpeed },this)
		contexts["borderDanger"]= BorderDangerContext(ship)
		contexts["worldBlockDanger"]= WorldBlockDangerContext(ship)
		contexts["obstructionDanger"] = ObstructionDangerContext(ship,obstructions)
	}
}
