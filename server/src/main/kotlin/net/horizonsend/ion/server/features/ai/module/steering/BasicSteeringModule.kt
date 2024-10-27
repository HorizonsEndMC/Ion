import net.horizonsend.ion.server.IonServer.aiSteeringConfig
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.steering.context.BlankContext
import net.horizonsend.ion.server.features.ai.module.steering.context.BorderDangerContext
import net.horizonsend.ion.server.features.ai.module.steering.context.ContextMap
import net.horizonsend.ion.server.features.ai.module.steering.context.FaceSeekContext
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

/** Basic implementation of a Steering Module, showcasing all the different modulues for */
open class BasicSteeringModule(
	controller: AIController,
	difficulty: DifficultyModule,
	generalTarget : Supplier<AITarget?>
) : SteeringModule(controller, difficulty,) {

	open val config = aiSteeringConfig.defaultBasicSteeringConfiguration

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
		contexts["offsetSeek"] = OffsetSeekContext(ship, generalTarget,this)
		contexts["faceSeek"]= FaceSeekContext(ship, generalTarget,difficulty)
		contexts["shieldAwareness"] = ShieldAwarenessContext(ship,difficulty)
		contexts["shipDanger"] = ShipDangerContext(ship, { config.defaultMaxSpeed },this)
		contexts["borderDanger"]= BorderDangerContext(ship)
		contexts["worldBlockDanger"]= WorldBlockDangerContext(ship)
		contexts["obstructionDanger"] = ObstructionDangerContext(ship,obstructions)
	}

    /**
     * Master steering function
     *
     *
     * Takes the current simulation sate and updates an agents heading and thrust using context
     * maps
     */
    override fun steer() {
		super.steer()

		contexts["movementInterest"]!!.addAll(
			contexts["wander"]!!,
			contexts["offsetSeek"]!!
		)
		contexts["movementInterest"]!!.clipZero()//safeguard against neg weights

		contexts["rotationInterest"]!!.addContext(contexts["movementInterest"]!!, config.defaultRotationContribution)
		contexts["rotationInterest"]!!.addContext(contexts["faceSeek"]!!)

		contexts["rotationInterest"]!!.softMaskContext(contexts["shieldAwareness"]!!,threshold = 1.0)

		contexts["danger"]!!.addAll(
			contexts["borderDanger"]!!,
			contexts["shipDanger"]!!,
			contexts["worldBlockDanger"]!!,
			ContextMap.scaled(contexts["shieldAwareness"]!!, 0.5)
		)

        //There are no resources that talk about steering where the heading of a ship is
        // different from where its accelerating, enabling strafing and drifting, movement and
        // rotation is a bit of a chicken and egg problem, as the ideal heading is in the
        // direction of movement and the ideal movement is where the ship is heading, regardless
        // this is my wip solution to this problem, movement and rotation interests are generated,
        // and then mixed together with high velocity favoring rotation priority (strafing) and
        // low velocity favoring movement priority (accelerating to top speed). This mixing was
        // designed with starfigters and smaller ships in mind, and as more parameters are
        // introduced this could be changed for larger ships
        // A current issue is that if the movement and rotation maps are equal and opposing
        // magnitude then it will lead to an agent jittering under a certain ship.velocity threshold.
        //mixing
        val rotationMovementPrior = config.defaultRotationMixingRatio
            //max(min(ship.velocity.length() / MAXSPEED*2, 1.0), 0.0).pow(1.0)
        //println(rotationMovementPrior)
		ContextMap.mix(contexts["movementInterest"]!!,contexts["rotationInterest"]!!, rotationMovementPrior,config.defaultRotationMixingPower)

        //masking, if the danger for a certain direction is greater than the threshold then it is
        // masked out
		contexts["movementInterest"]!!.softMaskContext(contexts["danger"]!!, 1.0)
		contexts["movementInterest"]!!.softMaskContext(contexts["obstructionDanger"]!!,1.0)
		contexts["rotationInterest"]!!.softMaskContext(contexts["danger"]!!, 1.0)

        //decision time
        decision(contexts["movementInterest"]!!, contexts["rotationInterest"]!!)
    }
}
