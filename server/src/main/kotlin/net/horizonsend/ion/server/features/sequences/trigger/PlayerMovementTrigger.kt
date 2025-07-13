package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceManager.getCurrentSequences
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.BoundingBox

object PlayerMovementTrigger : SequenceTriggerType<MovementTriggerSettings>() {
	override fun setupChecks() {
		listen<PlayerMoveEvent> { for (sequenceKey in getCurrentSequences(it.player)) { checkPhaseTriggers(it.player, sequenceKey) } }
	}

	class MovementTriggerSettings(
		val predicates: List<PlayerLocationPredicate>
	) : TriggerSettings() {
		override fun shouldProceed(player: Player, sequenceKey: IonRegistryKey<Sequence, out Sequence>, callingTrigger: SequenceTriggerType<*>): Boolean {
			return predicates.all { predicate -> predicate.check(player) }
		}
	}

	fun interface PlayerLocationPredicate {
		fun check(player: Player): Boolean
	}

	fun inBoundingBox(box: BoundingBox) = PlayerLocationPredicate {
		val cube = cube(box.min, box.max)
		@Suppress("OverrideOnly")
		debugAudience.audiences().filterIsInstance<Player>().forEach { player -> cube.forEach { cubePoint -> player.spawnParticle(Particle.SOUL_FIRE_FLAME, cubePoint.x, cubePoint.y, cubePoint.z, 1, 0.0, 0.0, 0.0, 0.0) } }

		return@PlayerLocationPredicate box.contains(it.location.toVector())
	}

	fun lookingAtBoundingBox(box: BoundingBox, distance: Double) = PlayerLocationPredicate { player ->
		val cube = cube(box.min, box.max)
		@Suppress("OverrideOnly")
		debugAudience.audiences().filterIsInstance<Player>().forEach { player -> cube.forEach { cubePoint -> player.spawnParticle(Particle.SOUL_FIRE_FLAME, cubePoint.x, cubePoint.y, cubePoint.z, 1, 0.0, 0.0, 0.0, 0.0) } }

		return@PlayerLocationPredicate box.rayTrace(player.eyeLocation.toVector(), player.location.direction, distance) != null
	}
}
