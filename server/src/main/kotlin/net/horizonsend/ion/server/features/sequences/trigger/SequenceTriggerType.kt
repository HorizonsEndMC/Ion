package net.horizonsend.ion.server.features.sequences.trigger

import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.CombinedAndTrigger.CombinedAndTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.CombinedOrTrigger.CombinedOrTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.DataPredicate.DataPredicateSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerType.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.BoundingBox
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull

abstract class SequenceTriggerType<T : SequenceTriggerType.TriggerSettings> {
	open fun setup() {}

	protected fun triggerPhases(player: Player) {
		val currentPhase = SequenceManager.getCurrentPhase(player) ?: return

		for (trigger in currentPhase.danglingTriggers) {
			if (trigger.type != this) continue

			@Suppress("UNCHECKED_CAST")
			trigger as SequenceTrigger<T>

			if (!trigger.shouldProceed(player)) continue

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
				fun inBoundingBox(box: BoundingBox) = PlayerLocationPredicate {
					val cube = cube(box.min, box.max)
					debugAudience.audiences().filterIsInstance<Player>().forEach { player -> cube.forEach { cubePoint -> player.spawnParticle(Particle.SOUL_FIRE_FLAME, cubePoint.x, cubePoint.y, cubePoint.z, 1, 0.0, 0.0, 0.0, 0.0) } }

					return@PlayerLocationPredicate box.contains(it.location.toVector())
				}

				fun lookingAtBoundingBox(box: BoundingBox) = PlayerLocationPredicate { player ->
					val cube = cube(box.min, box.max)
					debugAudience.audiences().filterIsInstance<Player>().forEach { player -> cube.forEach { cubePoint -> player.spawnParticle(Particle.SOUL_FIRE_FLAME, cubePoint.x, cubePoint.y, cubePoint.z, 1, 0.0, 0.0, 0.0, 0.0) } }

					return@PlayerLocationPredicate box.rayTrace(player.eyeLocation.toVector(), player.location.direction, 10.0) != null
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

	object CombinedAndTrigger : SequenceTriggerType<CombinedAndTriggerSettings>() {
		override fun setup() {
			listen<PlayerInteractEvent> { triggerPhases(it.player) }
		}

		class CombinedAndTriggerSettings(
			val children: Collection<SequenceTrigger<*>>
		) : TriggerSettings() {
			override fun shouldProceed(player: Player): Boolean {
				return children.all { trigger -> trigger.shouldProceed(player) }
			}
		}
	}

	object CombinedOrTrigger : SequenceTriggerType<CombinedOrTriggerSettings>() {
		override fun setup() {
			listen<PlayerInteractEvent> { triggerPhases(it.player) }
		}

		class CombinedOrTriggerSettings(
			val children: Collection<SequenceTrigger<*>>
		) : TriggerSettings() {
			override fun shouldProceed(player: Player): Boolean {
				return children.any { trigger -> trigger.shouldProceed(player) }
			}
		}
	}

	object DataPredicate: SequenceTriggerType<DataPredicateSettings<*>>() {
		override fun setup() {
			listen<PlayerInteractEvent> { triggerPhases(it.player) }
		}

		class DataPredicateSettings<T : Any>(
			val dataTypeKey: String,
			val predicate: Predicate<T?>
		) : TriggerSettings() {
			override fun shouldProceed(player: Player): Boolean {
				val storedData = SequenceManager.getSequenceData(player).get<T>(dataTypeKey).getOrNull()
				return predicate.test(storedData)
			}
		}
	}
}
