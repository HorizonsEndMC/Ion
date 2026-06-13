package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.trigger.StarshipMovementTrigger.StarshipMovementTriggerSettings
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

object StarshipMovementTrigger : SequenceTriggerType<StarshipMovementTriggerSettings>() {
	override fun setupChecks() {
		listen<StarshipTranslateEvent> {
			val player = it.ship.playerPilot ?: return@listen
			checkAllSequences(player, it)
		}
	}

	class StarshipMovementTriggerSettings(
		vararg val predicates: StarshipMovementPredicate
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, context: TriggerContext): Boolean {
			return context.callingTrigger == StarshipMovementTrigger && predicates.all {
				predicate -> predicate.check(ActiveStarships.findByPilot(player), context)
			}
		}
	}

	 fun interface StarshipMovementPredicate {
		 fun check(starship: Starship?, context: TriggerContext): Boolean
	 }

	fun inBoundingBox(box: BoundingBox) = StarshipMovementPredicate { starship, context ->
		if (starship == null) return@StarshipMovementPredicate false

		val box = box.clone().shift(context.sequenceContext.getOrigin().toVector())
		val cube = cube(box.min, box.max)
		@Suppress("OverrideOnly")
		debugAudience.audiences().filterIsInstance<Player>().forEach { player ->
			cube.forEach { cubePoint -> player.spawnParticle(Particle.SOUL_FIRE_FLAME, cubePoint.x, cubePoint.y, cubePoint.z, 1, 0.0, 0.0, 0.0, 0.0) }
		}

		return@StarshipMovementPredicate box.contains(starship.centerOfMass.toVector())
	}

	fun withinRadius(center: Vec3i, radius: Int) = StarshipMovementPredicate { starship, context ->
		if (starship == null) return@StarshipMovementPredicate false

		val absoluteCenter = center.toVector().add(context.sequenceContext.getOrigin().toVector())
		return@StarshipMovementPredicate starship.centerOfMass.toVector().distanceSquared(absoluteCenter) <= radius * radius
	}

	fun outsideRadius(center: Vec3i, radius: Int) = StarshipMovementPredicate { starship, context ->
		if (starship == null) return@StarshipMovementPredicate false

		val absoluteCenter = center.toVector().add(context.sequenceContext.getOrigin().toVector())
		return@StarshipMovementPredicate starship.centerOfMass.toVector().distanceSquared(absoluteCenter) > radius * radius
	}

	fun aboveCruiseSpeed(targetSpeed: Double) = StarshipMovementPredicate { starship, _ ->
		if (starship == null) return@StarshipMovementPredicate false

		return@StarshipMovementPredicate starship.cruiseData.velocity.lengthSquared() >= targetSpeed * targetSpeed
	}

	fun belowCruiseSpeed(targetSpeed: Double) = StarshipMovementPredicate { starship, _ ->
		if (starship == null) return@StarshipMovementPredicate false

		return@StarshipMovementPredicate starship.cruiseData.velocity.lengthSquared() < targetSpeed * targetSpeed
	}
}
