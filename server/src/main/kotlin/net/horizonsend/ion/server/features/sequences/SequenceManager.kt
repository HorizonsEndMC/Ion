package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.util.StaticFloatAmount
import net.horizonsend.ion.server.configuration.util.VariableFloatAmount
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.BRANCH_LOOK_OUTSIDE
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.BROKEN_ELEVATOR
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.CHERRY_TEST_BRANCH
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.EXIT_CRYOPOD_ROOM
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.LOOK_AT_TRACTOR
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.REAL_TUTORIAL_START
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.SequencePhaseKey
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_END
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.TUTORIAL_TWO
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys.USED_TRACTOR_BEAM
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.SendMessage
import net.horizonsend.ion.server.features.sequences.trigger.CombinedAndTrigger
import net.horizonsend.ion.server.features.sequences.trigger.DataPredicate
import net.horizonsend.ion.server.features.sequences.trigger.PlayerInteractTrigger.InteractTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.inBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.lookingAtBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Bukkit.getPlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.UUID

object SequenceManager : IonServerComponent() {
	private val phaseMap = mutableMapOf<UUID, SequencePhase>()
	private val sequenceData = mutableMapOf<UUID, SequenceDataStore>()

	override fun onEnable() {
		SequenceTriggerTypes.runSetup()

		Tasks.asyncRepeat(1L, 1L) {
			tickPhases()
		}
	}

	fun getSequenceData(player: Player): SequenceDataStore {
		return sequenceData.getOrPut(player.uniqueId) { SequenceDataStore() }
	}

	fun clearSequenceData(player: Player) {
		sequenceData.remove(player.uniqueId)
	}

	@EventHandler
	fun onPlayerLeave(event: PlayerQuitEvent) {
		phaseMap.remove(event.player.uniqueId)?.endPrematurely(event.player)
		sequenceData.remove(event.player.uniqueId)
	}

	fun getCurrentPhase(player: Player): SequencePhase? {
		return phaseMap[player.uniqueId]
	}

	fun tickPhases() {
		for ((playerId, phase) in phaseMap) {
			val player = getPlayer(playerId) ?: continue
			phase.tick(player)
		}
	}

	fun startPhase(player: Player, phase: SequencePhase?) {
		if (phase == null) {
			endPhase(player)

			return
		}

		setPhase(player, phase)
		phase.start(player)
	}

	fun endPhase(player: Player) {
		val existingPhase = phaseMap.remove(player.uniqueId)
		existingPhase?.end(player)
	}

	private fun setPhase(player: Player, phase: SequencePhase) {
		endPhase(player)
		phaseMap[player.uniqueId] = phase
	}

	fun getPhaseByKey(key: SequencePhaseKey): SequencePhase {
		return phasesByKey[key] ?: throw IllegalStateException("Unregistered phase key ${key.key}")
	}

	private val phasesByKey = mutableMapOf<SequencePhaseKey, SequencePhase>()

	private fun bootstrapPhase(phase: SequencePhase): SequencePhaseKey {
		phasesByKey[phase.key] = phase
		return phase.key
	}

	/** Builds and returns the phase key. Good utility for registering phases in the hierarchy */
	private fun bootstrapPhase(
		key: SequencePhaseKey,
		trigger: SequenceTrigger<*>?,
		effects: List<SequencePhaseEffect>,
		children: List<SequencePhaseKey>
	): SequencePhaseKey {
		val phase = SequencePhase(key, trigger, effects, children)

		phasesByKey[phase.key] = phase
		return phase.key
	}

	val RANDOM_EXPLOSION_SOUND = SequencePhaseEffect.Chance(SequencePhaseEffect.PlaySound(Sound.ENTITY_GENERIC_EXPLODE.key(), VariableFloatAmount(0.05f, 1.0f), StaticFloatAmount(1.0f), listOf(EffectTiming.TICKED)), 0.02)

	val REAL_TUTORIAL = bootstrapPhase(
		key = REAL_TUTORIAL_START,
		trigger = null,
		effects = listOf(
			SendMessage(text("Welcome to Horizon's End!"), listOf(EffectTiming.START)),
			SendMessage(text("This is the start of the intro sequence."), listOf(EffectTiming.START)),
			SendMessage(text("Exit the cryopod room to begin."), listOf(EffectTiming.START)),
			SendMessage(Component.empty(), listOf(EffectTiming.START))
		),
		children = listOf(
			bootstrapPhase(
				key = EXIT_CRYOPOD_ROOM,
				trigger = SequenceTrigger(
					SequenceTriggerTypes.PLAYER_MOVEMENT,
					MovementTriggerSettings(listOf(
						inBoundingBox(BoundingBox.of(
							Vector(84.0, 358.0, 26.0),
							Vector(86.0, 360.0, 27.0),
						))
					))
				),
				effects = listOf(
					RANDOM_EXPLOSION_SOUND,
					SequencePhaseEffect.DataConditionalEffects<Boolean>("seen_pirates", { it.isEmpty || !it.get() }, listOf(EffectTiming.START),
						SendMessage(Component.empty(), listOf()),
						SendMessage(text("The ships's communication system crackles to life:", GRAY, ITALIC), listOf()),
						SendMessage(text("This is your captain speaking, we're under attack by pirates"), listOf()),
						SendMessage(text("We must abandon ship"), listOf()),
						SendMessage(text("Proceed to the elevator down to the hangar bay!"), listOf()),
						SendMessage(Component.empty(), listOf())
					),
					SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(93, 359, 11), 10L, listOf(EffectTiming.TICKED)), 10)
				),
				children = listOf(
					bootstrapPhase(
						key = BRANCH_LOOK_OUTSIDE,
						trigger = SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
							// If looking out window
							SequenceTrigger(
								SequenceTriggerTypes.PLAYER_MOVEMENT,
								MovementTriggerSettings(listOf(
									lookingAtBoundingBox(BoundingBox.of(
										Vec3i(-13, 358, -47).toVector(),
										Vec3i(48, 383, 75).toVector()
									), 100.0)
								))
							),
							// Only trigger this branch if first time
							SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_pirates") { it != true })
						))),
						effects = listOf(
							RANDOM_EXPLOSION_SOUND,
							SendMessage(Component.empty(), listOf(EffectTiming.START)),
							SendMessage(text("They look like the infamous Sky Dogs Pirates to you.", GRAY, ITALIC), listOf(EffectTiming.START)),
							SendMessage(Component.empty(), listOf(EffectTiming.START)),

							SequencePhaseEffect.GoToPhase(EXIT_CRYOPOD_ROOM, listOf(EffectTiming.START)),

							SequencePhaseEffect.SetSequenceData("seen_pirates", true, listOf(EffectTiming.END)),
						),
						children = listOf()
					),
					bootstrapPhase(
						key = BROKEN_ELEVATOR,
						trigger = SequenceTrigger(
							SequenceTriggerTypes.PLAYER_MOVEMENT,
							MovementTriggerSettings(listOf(
								lookingAtBoundingBox(BoundingBox.of(
									Vector(92.0, 357.0, 13.0),
									Vector(94.0, 362.0, 10.0),
								), 7.5)
							))
						),
						effects = listOf(
							RANDOM_EXPLOSION_SOUND,
							SendMessage(Component.empty(), listOf(EffectTiming.START)),
							SendMessage(text("Smoke bellows out of the smouldering remains of an elevator, the dorsal hull appears to have taken a direct hit from enemy fire.", GRAY, ITALIC), listOf(EffectTiming.START)),

							SendMessage(text("There is a backup crew elevator other side of the ship!"), listOf(EffectTiming.START)),
							SendMessage(Component.empty(), listOf(EffectTiming.START)),

							SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97, 359, 63), 10L, listOf(EffectTiming.TICKED)), 10)
						),
						children = listOf(
							bootstrapPhase(
								key = LOOK_AT_TRACTOR,
								trigger = SequenceTrigger(
									SequenceTriggerTypes.PLAYER_MOVEMENT,
									MovementTriggerSettings(listOf(
										lookingAtBoundingBox(BoundingBox.of(
											Vector(96.0, 357.0, 62.0),
											Vector(98.0, 362.0, 64.0),
										), 3.5)
									))
								),
								effects = listOf(
									RANDOM_EXPLOSION_SOUND,
									SendMessage(Component.empty(), listOf(EffectTiming.START)),
									SendMessage(text("Text on how to use a tractor beam", GRAY, ITALIC), listOf(EffectTiming.START)),
									SendMessage(Component.empty(), listOf(EffectTiming.START)),
								),
								children = listOf(
									bootstrapPhase(
										key = USED_TRACTOR_BEAM,
										trigger = SequenceTrigger(SequenceTriggerTypes.USE_TRACTOR_BEAM, InteractTriggerSettings()),
										effects = listOf(
											RANDOM_EXPLOSION_SOUND,
											SendMessage(Component.empty(), listOf(EffectTiming.START)),
											SendMessage(text("Quick, now make your way to the escape pod!", GRAY, ITALIC), listOf(EffectTiming.START)),
											SendMessage(Component.empty(), listOf(EffectTiming.START)),
										),
										children = listOf(

										)
									)
								)
							)
						)
					)
				)
			)
		)
	)

	val TUTORIAL get() = bootstrapPhase(SequencePhase(
		key = REAL_TUTORIAL_START,
		trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings()),
		effects = mutableListOf(
			SequencePhaseEffect.DataConditionalEffect<Boolean>("seen_cherry_wood", { it.isEmpty || !it.get() }, SendMessage(text("Welcome to Horizon's End!"), listOf(EffectTiming.START))),
			SequencePhaseEffect.DataConditionalEffect<Boolean>("seen_cherry_wood", { it.isEmpty || !it.get() }, SendMessage(text("This is the start of the intro sequence."), listOf(EffectTiming.START))),
			RANDOM_EXPLOSION_SOUND
		),
		children = listOf(
			bootstrapPhase(SequencePhase(
				key = TUTORIAL_TWO,
				trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_MOVEMENT, MovementTriggerSettings(listOf(
					lookingAtBoundingBox(BoundingBox.of(Vec3i(193, 359, -121).toVector(), Vec3i(200, 365, -111).plus(Vec3i(1, 1, 1)).toVector()), 10.0)
				))),
				effects = mutableListOf(
					SendMessage(text("Punch to progress"), listOf(EffectTiming.START)),
					RANDOM_EXPLOSION_SOUND
				),
				children = listOf(bootstrapPhase(
					SequencePhase.endSequence(
						key = TUTORIAL_END,
						trigger = SequenceTrigger(SequenceTriggerTypes.PLAYER_INTERACT, InteractTriggerSettings()),
						SendMessage(text("Tutorial Completed"), listOf(EffectTiming.START))
					))
				)
			)),
			bootstrapPhase(SequencePhase(
				key = CHERRY_TEST_BRANCH,
				trigger = SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
					SequenceTrigger(SequenceTriggerTypes.PLAYER_MOVEMENT, MovementTriggerSettings(listOf(
						lookingAtBoundingBox(BoundingBox.of(Vec3i(203, 360, -126).toVector(), Vec3i(203, 360, -124).plus(Vec3i(1, 1, 1)).toVector()), 10.0),
					))),
					SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_cherry_wood") { it != true })
				))),
				effects = mutableListOf(
					SendMessage(text("That is some cherry wood"), listOf(EffectTiming.START)),
					SendMessage(text("Back to our regularly scheduled programming"), listOf(EffectTiming.END)),
					SequencePhaseEffect.SetSequenceData("seen_cherry_wood", true, listOf(EffectTiming.END)),
					SequencePhaseEffect.GoToPhase(REAL_TUTORIAL_START, listOf(EffectTiming.START)),
					RANDOM_EXPLOSION_SOUND
				),
				children = listOf()
			))
		)
	))
}
