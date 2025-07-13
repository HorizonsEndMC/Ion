package net.horizonsend.ion.server.features.sequences.phases

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.util.StaticFloatAmount
import net.horizonsend.ion.server.configuration.util.VariableFloatAmount
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.Companion.ifPreviousPhase
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.GoToPreviousPhase
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.SendMessage
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_DYNMAP
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_LOOK_OUTSIDE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_SHIP_COMPUTER
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BROKEN_ELEVATOR
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.CREW_QUARTERS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.EXIT_CRYOPOD_ROOM
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FIRE_OBSTACLE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.GET_CHETHERITE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.LOOK_AT_TRACTOR
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.RECEIVED_CHETHERITE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.SequencePhaseKey
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.TUTORIAL_START
import net.horizonsend.ion.server.features.sequences.trigger.CombinedAndTrigger
import net.horizonsend.ion.server.features.sequences.trigger.ContainsItemTrigger
import net.horizonsend.ion.server.features.sequences.trigger.DataPredicate
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.inBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.lookingAtBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.features.sequences.trigger.UsedTractorBeamTrigger.TractorBeamTriggerSettings
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Sound
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

object SequencePhases {
	fun getPhaseByKey(key: SequencePhaseKey): SequencePhase {
		return phasesByKey[key] ?: throw IllegalStateException("Unregistered phase key ${key.key}")
	}

	private val phasesByKey = mutableMapOf<SequencePhaseKey, SequencePhase>()

	private fun bootstrapPhase(phase: SequencePhase) {
		phasesByKey[phase.phaseKey] = phase
	}

	/** Builds and returns the phase key. Good utility for registering phases in the hierarchy */
	private fun bootstrapPhase(
		sequenceKey: String,
		phaseKey: SequencePhaseKey,
		trigger: SequenceTrigger<*>?,
		effects: List<SequencePhaseEffect>,
		children: List<SequencePhaseKey>
	) {
		bootstrapPhase(SequencePhase(sequenceKey, phaseKey, trigger, effects, children))
	}

	fun registerPhases() {
		registerTutorial()
	}

	val RANDOM_EXPLOSION_SOUND = SequencePhaseEffect.Chance(
		SequencePhaseEffect.PlaySound(
			RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_GENERIC_EXPLODE)!!,
			VariableFloatAmount(0.05f, 1.0f),
			StaticFloatAmount(1.0f),
			listOf(EffectTiming.TICKED)
		),
		0.02
	)
	val NEXT_PHASE_SOUND = SequencePhaseEffect.PlaySound(
		RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_ARROW_HIT_PLAYER)!!,
		StaticFloatAmount(1.0f),
		StaticFloatAmount(2.0f),
		listOf(EffectTiming.START)
	)

	private fun registerTutorial() {
		registerTutorialBranches()

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = TUTORIAL_START,
			trigger = null,
			effects = listOf(
				SendMessage(text("Welcome to Horizon's End!"), listOf(EffectTiming.START)),
				SendMessage(text("This is the start of the intro sequence."), listOf(EffectTiming.START)),
				SendMessage(text("Exit the cryopod room to begin."), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START))
			),
			children = listOf(EXIT_CRYOPOD_ROOM)
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = EXIT_CRYOPOD_ROOM,
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
				ifPreviousPhase(TUTORIAL_START, listOf(EffectTiming.START),
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), listOf()),
					SendMessage(text("The ships's communication system crackles to life:", GRAY, ITALIC), listOf()),
					SendMessage(text("This is your captain speaking, we're under attack by pirates"), listOf()),
					SendMessage(text("We must abandon ship"), listOf()),
					SendMessage(text("Proceed to the elevator down to the hangar bay!"), listOf()),
					SendMessage(Component.empty(), listOf())
				),
				SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(93, 359, 11), 10L, listOf(EffectTiming.TICKED)), 10)
			),
			children = listOf(BRANCH_LOOK_OUTSIDE, BROKEN_ELEVATOR)
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = BROKEN_ELEVATOR,
			trigger = SequenceTrigger(
				SequenceTriggerTypes.PLAYER_MOVEMENT,
				MovementTriggerSettings(listOf(
					lookingAtBoundingBox(BoundingBox.of(
						Vector(92.0, 357.0, 13.0),
						Vector(94.0, 362.0, 10.0),
					), 4.5)
				))
			),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,

				ifPreviousPhase(EXIT_CRYOPOD_ROOM, listOf(EffectTiming.START),
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), listOf()),
					SendMessage(text("Smoke bellows out of the smouldering remains of an elevator, the dorsal hull appears to have taken a direct hit from enemy fire.", GRAY, ITALIC), listOf(EffectTiming.START)),

					SendMessage(text("There is a backup crew elevator other side of the ship!"), listOf()),
					SendMessage(Component.empty(), listOf()),
				),

				SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97, 359, 63), 10L, listOf(EffectTiming.TICKED)), 10)
			),
			children = listOf(LOOK_AT_TRACTOR, BRANCH_DYNMAP, BRANCH_SHIP_COMPUTER)
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = LOOK_AT_TRACTOR,
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

				ifPreviousPhase(BROKEN_ELEVATOR,
					listOf(EffectTiming.START),
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), listOf()),
					SendMessage(text("To use the elevator, hold your controller (clock), stand on the glass block, and courch.", GRAY, ITALIC), listOf()),
					SendMessage(Component.empty(), listOf()),
				)
			),
			children = listOf(CREW_QUARTERS, BRANCH_DYNMAP, BRANCH_SHIP_COMPUTER)
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = CREW_QUARTERS,
			trigger = SequenceTrigger(SequenceTriggerTypes.USE_TRACTOR_BEAM, TractorBeamTriggerSettings()),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				ifPreviousPhase(LOOK_AT_TRACTOR,
					listOf(EffectTiming.START),
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), listOf()),
					SendMessage(text("Quick, make your way through the crew quarters and maintainance bays to the hangar bay!", GRAY, ITALIC), listOf()),
					SendMessage(Component.empty(), listOf())
				)
			),
			children = listOf(FIRE_OBSTACLE)
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = FIRE_OBSTACLE,
			trigger = SequenceTrigger(
				SequenceTriggerTypes.PLAYER_MOVEMENT,
				MovementTriggerSettings(listOf(
					inBoundingBox(BoundingBox.of(
						Vector(91.0, 351.0, 52.0),
						Vector(95.0, 355.0, 51.0),
					))
				))
			),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("The ship's gravity generators have failed in the attack! Fly over the obstacle!", GRAY, ITALIC), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
			),
			children = listOf(GET_CHETHERITE)
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = GET_CHETHERITE,
			trigger = SequenceTrigger(
				SequenceTriggerTypes.PLAYER_MOVEMENT,
				MovementTriggerSettings(listOf(
					inBoundingBox(BoundingBox.of(
						Vector(91.0, 351.0, 25.0),
						Vector(95.0, 354.0, 23.0),
					))
				))
			),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97, 352, 16), 10L, listOf(EffectTiming.TICKED)), 10),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("Quick, you'll need to grab some fuel for the escape pod. You can find some in that gargo container.", GRAY, ITALIC), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
			),
			children = listOf(RECEIVED_CHETHERITE)
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = RECEIVED_CHETHERITE,
			trigger = SequenceTrigger(
				SequenceTriggerTypes.CONTAINS_ITEM,
				ContainsItemTrigger.ContainsItemTriggerSettings { it?.customItem?.identifier == CustomItemRegistry.CHETHERITE.identifier }
			),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97, 352, 16), 10L, listOf(EffectTiming.TICKED)), 10),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("Chetherite is hyperdrive fuel, you'll need it to travel faster than light.", GRAY, ITALIC), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
			),
			children = listOf(/*TODO*/)
		)
	}

	private fun registerTutorialBranches() {
		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = BRANCH_LOOK_OUTSIDE,
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

				GoToPreviousPhase(listOf(EffectTiming.START)),

				SequencePhaseEffect.SetSequenceData("seen_pirates", true, Boolean::class, listOf(EffectTiming.END)),
			),
			children = listOf()
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = BRANCH_DYNMAP,
			trigger = SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
				// If looking out window
				SequenceTrigger(
					SequenceTriggerTypes.PLAYER_MOVEMENT,
					MovementTriggerSettings(listOf(
						lookingAtBoundingBox(BoundingBox.of(
							Vec3i(96, 358, 69).toVector(),
							Vec3i(96, 361, 72).toVector()
						), 5.0)
					))
				),
				// Only trigger this branch if first time
				SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_dynmap") { it != true })
			))),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(ofChildren(text("This server has an interactive web map. You can access it "), formatLink("here", "https://survival.horizonsend.net/")), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),

				GoToPreviousPhase(listOf(EffectTiming.START)),
				SequencePhaseEffect.SetSequenceData("seen_dynmap", true, Boolean::class, listOf(EffectTiming.END)),
			),
			children = listOf()
		)

		bootstrapPhase(
			sequenceKey = SequenceKeys.TUTORIAL,
			phaseKey = BRANCH_SHIP_COMPUTER,
			trigger = SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
				// If looking out window
				SequenceTrigger(
					SequenceTriggerTypes.PLAYER_MOVEMENT,
					MovementTriggerSettings(listOf(
						lookingAtBoundingBox(BoundingBox.of(
							Vec3i(96, 358, 78).toVector(),
							Vec3i(88, 363, 87).toVector()
						), 3.0)
					))
				),
				// Only trigger this branch if first time
				SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_ship_computer") { it != true })
			))),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("This is a starship computer. It is the primary point of interface for ships. They allow piloting, detection, and manage settings.", GRAY, ITALIC), listOf(EffectTiming.START)),
				SequencePhaseEffect.HighlightBlock(Vec3i(93, 359, 82), 60L, listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),

				GoToPreviousPhase(listOf(EffectTiming.START)),
				SequencePhaseEffect.SetSequenceData("seen_ship_computer", true, Boolean::class, listOf(EffectTiming.END)),
			),
			children = listOf()
		)
	}
}
