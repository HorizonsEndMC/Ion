package net.horizonsend.ion.server.features.sequences.phases

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.util.StaticFloatAmount
import net.horizonsend.ion.server.configuration.util.VariableFloatAmount
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceKeys
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

class SequencePhaseRegistry  : Registry<SequencePhase>(RegistryKeys.SEQUENCE_PHASE) {
	override fun getKeySet(): KeyRegistry<SequencePhase> = SequencePhaseKeys

	override fun boostrap() {
		registerTutorial()
	}

	private fun bootstrapPhase(
		phaseKey: IonRegistryKey<SequencePhase, SequencePhase>,
		sequenceKey: IonRegistryKey<Sequence, Sequence>,
		triggers: Collection<SequenceTrigger<*>>,
		effects: List<SequencePhaseEffect>
	) = register(phaseKey, SequencePhase(phaseKey, sequenceKey, triggers, effects))

	private val RANDOM_EXPLOSION_SOUND = SequencePhaseEffect.Chance(
		SequencePhaseEffect.PlaySound(
			RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_GENERIC_EXPLODE)!!,
			VariableFloatAmount(0.05f, 1.0f),
			StaticFloatAmount(1.0f),
			listOf(EffectTiming.TICKED)
		),
		0.02
	)
	private val NEXT_PHASE_SOUND = SequencePhaseEffect.PlaySound(
		RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_ARROW_HIT_PLAYER)!!,
		StaticFloatAmount(1.0f),
		StaticFloatAmount(2.0f),
		listOf(EffectTiming.START)
	)

	private fun registerTutorial() {
		registerTutorialBranches()

		bootstrapPhase(
			phaseKey = TUTORIAL_START,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(SequenceTrigger(
				type = SequenceTriggerTypes.PLAYER_MOVEMENT,
				settings = MovementTriggerSettings(listOf(
					inBoundingBox(BoundingBox.of(
						Vector(84.0, 358.0, 26.0),
						Vector(86.0, 360.0, 27.0),
					))
				)),
				triggerResult = SequenceTrigger.startPhase(EXIT_CRYOPOD_ROOM)
			)),
			effects = listOf(
				SendMessage(text("Welcome to Horizon's End!"), listOf(EffectTiming.START)),
				SendMessage(text("This is the start of the intro sequence."), listOf(EffectTiming.START)),
				SendMessage(text("Exit the cryopod room to begin."), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START))
			)
		)

		bootstrapPhase(
			phaseKey = EXIT_CRYOPOD_ROOM,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.PLAYER_MOVEMENT,
					MovementTriggerSettings(listOf(
						lookingAtBoundingBox(BoundingBox.of(
							Vector(92.0, 357.0, 13.0),
							Vector(94.0, 362.0, 10.0),
						), 4.5)
					)),
					triggerResult = SequenceTrigger.startPhase(BROKEN_ELEVATOR)
				),
				SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
					// If looking out window
					SequenceTrigger(
						SequenceTriggerTypes.PLAYER_MOVEMENT,
						MovementTriggerSettings(listOf(
							lookingAtBoundingBox(BoundingBox.of(
								Vec3i(-13, 358, -47).toVector(),
								Vec3i(48, 383, 75).toVector()
							), 100.0)
						)),
						triggerResult = SequenceTrigger.startPhase(BRANCH_LOOK_OUTSIDE)
					),
					// Only trigger this branch if first time
					SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_pirates") { it != true },
						triggerResult = SequenceTrigger.startPhase(BRANCH_LOOK_OUTSIDE))
				)), triggerResult = SequenceTrigger.startPhase(BRANCH_LOOK_OUTSIDE))
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
			)
		)

		bootstrapPhase(
			phaseKey = BROKEN_ELEVATOR,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.PLAYER_MOVEMENT,
					MovementTriggerSettings(listOf(
						lookingAtBoundingBox(BoundingBox.of(
							Vector(96.0, 357.0, 62.0),
							Vector(98.0, 362.0, 64.0),
						), 3.5)
					)),
					triggerResult = SequenceTrigger.startPhase(LOOK_AT_TRACTOR)
				),
				SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
					// If looking out window
					SequenceTrigger(
						SequenceTriggerTypes.PLAYER_MOVEMENT,
						MovementTriggerSettings(listOf(
							lookingAtBoundingBox(BoundingBox.of(
								Vec3i(96, 358, 69).toVector(),
								Vec3i(96, 361, 72).toVector()
							), 5.0)
						)),
						triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
					),
					// Only trigger this branch if first time
					SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_dynmap") { it != true },
						triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP))
				)),
					triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)),
				SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
					// If looking out window
					SequenceTrigger(
						SequenceTriggerTypes.PLAYER_MOVEMENT,
						MovementTriggerSettings(listOf(
							lookingAtBoundingBox(BoundingBox.of(
								Vec3i(96, 358, 78).toVector(),
								Vec3i(88, 363, 87).toVector()
							), 3.0)
						)),
						triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
					),
					// Only trigger this branch if first time
					SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_ship_computer") { it != true },
						triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER))
				)),
					triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER))
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
			)
		)

		bootstrapPhase(
			phaseKey = LOOK_AT_TRACTOR,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(SequenceTriggerTypes.USE_TRACTOR_BEAM, TractorBeamTriggerSettings(), triggerResult = SequenceTrigger.startPhase(CREW_QUARTERS)),
				SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
					// If looking out window
					SequenceTrigger(
						SequenceTriggerTypes.PLAYER_MOVEMENT,
						MovementTriggerSettings(listOf(
							lookingAtBoundingBox(BoundingBox.of(
								Vec3i(96, 358, 69).toVector(),
								Vec3i(96, 361, 72).toVector()
							), 5.0)
						)),
						triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
					),
					// Only trigger this branch if first time
					SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_dynmap") { it != true },
						triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP))
				)), triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
				),
				SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(listOf(
					// If looking out window
					SequenceTrigger(
						SequenceTriggerTypes.PLAYER_MOVEMENT,
						MovementTriggerSettings(listOf(
							lookingAtBoundingBox(BoundingBox.of(
								Vec3i(96, 358, 78).toVector(),
								Vec3i(88, 363, 87).toVector()
							), 3.0)
						)),
						triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
					),
					// Only trigger this branch if first time
					SequenceTrigger(SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_ship_computer") { it != true },
						triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER))
				)),
					triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER))
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
			)
		)

		bootstrapPhase(
			phaseKey = CREW_QUARTERS,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(SequenceTrigger(
				SequenceTriggerTypes.PLAYER_MOVEMENT,
				MovementTriggerSettings(listOf(
					inBoundingBox(BoundingBox.of(
						Vector(91.0, 351.0, 52.0),
						Vector(95.0, 355.0, 51.0),
					))
				)),
				triggerResult = SequenceTrigger.startPhase(FIRE_OBSTACLE)
			)),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				ifPreviousPhase(LOOK_AT_TRACTOR,
					listOf(EffectTiming.START),
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), listOf()),
					SendMessage(text("Quick, make your way through the crew quarters and maintainance bays to the hangar bay!", GRAY, ITALIC), listOf()),
					SendMessage(Component.empty(), listOf())
				)
			)
		)

		bootstrapPhase(
			phaseKey = FIRE_OBSTACLE,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(SequenceTrigger(
				SequenceTriggerTypes.PLAYER_MOVEMENT,
				MovementTriggerSettings(listOf(
					inBoundingBox(BoundingBox.of(
						Vector(91.0, 351.0, 25.0),
						Vector(95.0, 354.0, 23.0),
					))
				)),
				triggerResult = SequenceTrigger.startPhase(GET_CHETHERITE)
			)),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("The ship's gravity generators have failed in the attack! Fly over the obstacle!", GRAY, ITALIC), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
			)
		)

		bootstrapPhase(
			phaseKey = GET_CHETHERITE,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(SequenceTrigger(
				SequenceTriggerTypes.CONTAINS_ITEM,
				ContainsItemTrigger.ContainsItemTriggerSettings { it?.customItem?.key == CustomItemKeys.CHETHERITE },
				triggerResult = SequenceTrigger.startPhase(RECEIVED_CHETHERITE)
			)),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97, 352, 16), 10L, listOf(EffectTiming.TICKED)), 10),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("Quick, you'll need to grab some fuel for the escape pod. You can find some in that gargo container.", GRAY, ITALIC), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
			)
		)

		bootstrapPhase(
			phaseKey = RECEIVED_CHETHERITE,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(/*TODO*/),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97, 352, 16), 10L, listOf(EffectTiming.TICKED)), 10),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("Chetherite is hyperdrive fuel, you'll need it to travel faster than light.", GRAY, ITALIC), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
			)
		)
	}

	private fun registerTutorialBranches() {
		bootstrapPhase(
			phaseKey = BRANCH_LOOK_OUTSIDE,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("They look like the infamous Sky Dogs Pirates to you.", GRAY, ITALIC), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),

				GoToPreviousPhase(listOf(EffectTiming.START)),

				SequencePhaseEffect.SetSequenceData("seen_pirates", true, Boolean::class, listOf(EffectTiming.END)),
			)
		)

		bootstrapPhase(
			phaseKey = BRANCH_DYNMAP,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(ofChildren(text("This server has an interactive web map. You can access it "), formatLink("here", "https://survival.horizonsend.net/")), listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),

				GoToPreviousPhase(listOf(EffectTiming.START)),
				SequencePhaseEffect.SetSequenceData("seen_dynmap", true, Boolean::class, listOf(EffectTiming.END)),
			)
		)

		bootstrapPhase(
			phaseKey = BRANCH_SHIP_COMPUTER,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				SendMessage(Component.empty(), listOf(EffectTiming.START)),
				SendMessage(text("This is a starship computer. It is the primary point of interface for ships. They allow piloting, detection, and manage settings.", GRAY, ITALIC), listOf(EffectTiming.START)),
				SequencePhaseEffect.HighlightBlock(Vec3i(93, 359, 82), 60L, listOf(EffectTiming.START)),
				SendMessage(Component.empty(), listOf(EffectTiming.START)),

				GoToPreviousPhase(listOf(EffectTiming.START)),
				SequencePhaseEffect.SetSequenceData("seen_ship_computer", true, Boolean::class, listOf(EffectTiming.END)),
			)
		)
	}
}
