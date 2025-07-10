package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.nearestPointToVector
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

abstract class SequenceTriggerType<T : SequenceTriggerType.TriggerSettings> {
	open fun setup() {}

	protected fun triggerPhases(player: Player) {
		val currentPhase = SequenceManager.getCurrentPhase(player) ?: return

		for (trigger in currentPhase.danglingTriggers) {
			if (trigger.type != this) continue

			@Suppress("UNCHECKED_CAST")
			trigger as SequenceTrigger<T>

			if (!trigger.settings.shouldProceed(player)) continue

			trigger.trigger(player)
			break
		}
	}

	abstract class TriggerSettings() {
		abstract fun shouldProceed(player: Player): Boolean
	}

	object PlayerMovementTrigger : SequenceTriggerType<MovementTriggerSettings>() {
		override fun setup() {
			listen<PlayerMoveEvent> { triggerPhases(it.player) }
		}

		class MovementTriggerSettings(
			val predicates: List<PlayerLocationPredicate>
		) : TriggerSettings() {
			override fun shouldProceed(player: Player): Boolean {
				return predicates.all { predicate -> predicate.check(player) }
			}
		}

		fun interface PlayerLocationPredicate {
			fun check(player: Player): Boolean

			companion object {
				fun inBoundingBox(minPoint: Vec3i, maxPoint: Vec3i) = PlayerLocationPredicate {
					val (x, y, z) = Vec3i(it.location)

					return@PlayerLocationPredicate x >= minPoint.x && x < maxPoint.x
						&& y >= minPoint.y && y < maxPoint.y
						&& z >= minPoint.z && z < maxPoint.z
				}

				fun lookingAtBoundingBox(minPoint: Vec3i, maxPoint: Vec3i) = PlayerLocationPredicate {
					val eyeDirection = it.location.direction

					val nearestPoint = nearestPointToVector(
						origin = it.eyeLocation.toVector(),
						direction = eyeDirection,
						point = minPoint.toVector()
					)

					val (x, y, z) = Vec3i(nearestPoint)

					return@PlayerLocationPredicate x >= minPoint.x && x < maxPoint.x
						&& y >= minPoint.y && y < maxPoint.y
						&& z >= minPoint.z && z < maxPoint.z
				}
			}
		}
	}

	object PlayerInteractTrigger : SequenceTriggerType<InteractTriggerSettings>() {
		override fun setup() {
			listen<PlayerInteractEvent> { triggerPhases(it.player) }
		}

		class InteractTriggerSettings(
		) : TriggerSettings() {
			override fun shouldProceed(player: Player): Boolean {
				return true
			}
		}
	}
}
