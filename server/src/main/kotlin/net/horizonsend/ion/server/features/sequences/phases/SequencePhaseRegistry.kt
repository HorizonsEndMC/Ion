package net.horizonsend.ion.server.features.sequences.phases

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.ConfigurationFiles
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
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_CARGO_CRATES
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_DYNMAP
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_LOOK_OUTSIDE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_MULTIBLOCKS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_NAVIGATION
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_SHIP_COMPUTER
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BROKEN_ELEVATOR
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.CREW_QUARTERS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.ENTERED_ESCAPE_POD
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
import net.horizonsend.ion.server.features.starship.dealers.NPCDealerShip
import net.horizonsend.ion.server.features.starship.dealers.StarshipDealers
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Sound
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.BoundingBox

class SequencePhaseRegistry : Registry<SequencePhase>(RegistryKeys.SEQUENCE_PHASE) {
    override fun getKeySet(): KeyRegistry<SequencePhase> = SequencePhaseKeys

    override fun boostrap() {
        registerTutorial()
    }

    private fun bootstrapPhase(
		phaseKey: IonRegistryKey<SequencePhase, SequencePhase>,
		sequenceKey: IonRegistryKey<Sequence, Sequence>,
		triggers: Collection<SequenceTrigger<*>>,
		effects: List<SequencePhaseEffect>,
	) = register(phaseKey, SequencePhase(phaseKey, sequenceKey, triggers, effects))

    private val RANDOM_EXPLOSION_SOUND = SequencePhaseEffect.Chance(
        SequencePhaseEffect.PlaySound(
            RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_GENERIC_EXPLODE)!!,
            VariableFloatAmount(0.05f, 1.0f),
            StaticFloatAmount(1.0f),
            EffectTiming.TICKED
        ),
        0.02
    )
    private val NEXT_PHASE_SOUND = SequencePhaseEffect.PlaySound(
        RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_ARROW_HIT_PLAYER)!!,
        StaticFloatAmount(1.0f),
        StaticFloatAmount(2.0f),
        EffectTiming.START
    )

    private fun registerTutorial() {
        registerTutorialBranches()

        bootstrapPhase(
            phaseKey = TUTORIAL_START,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(84, 358, 26, 85, 360, 26))),
                    triggerResult = SequenceTrigger.startPhase(EXIT_CRYOPOD_ROOM)
                )
            ),
            effects = listOf(
                SendMessage(text("Welcome to Horizon's End!"), EffectTiming.START),
                SendMessage(text("This is the start of the intro sequence."), EffectTiming.START),
                SendMessage(text("Exit the cryopod room to begin."), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START)
            )
        )

        bootstrapPhase(
            phaseKey = EXIT_CRYOPOD_ROOM,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.PLAYER_MOVEMENT,
                    MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(92, 357, 13, 94, 362, 10), distance = 4.5)),
                    triggerResult = SequenceTrigger.startPhase(BROKEN_ELEVATOR)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
                        // If looking out window
                        SequenceTrigger(
                            SequenceTriggerTypes.PLAYER_MOVEMENT,
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(-13, 358, -47, 48, 383, 75), distance = 100.0)),
                            triggerResult = SequenceTrigger.startPhase(BRANCH_LOOK_OUTSIDE)
                        ),
                        // Only trigger this branch if first time
                        SequenceTrigger(
                            SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_pirates") { it != true },
                            triggerResult = SequenceTrigger.startPhase(BRANCH_LOOK_OUTSIDE)
                        )
                    ), triggerResult = SequenceTrigger.startPhase(BRANCH_LOOK_OUTSIDE)
                )
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                ifPreviousPhase(
                    TUTORIAL_START, EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    SendMessage(Component.empty(), null),
                    SendMessage(text("The ships's communication system crackles to life:", GRAY, ITALIC), null),
                    SendMessage(text("This is your captain speaking, we're under attack by pirates!"), null),
                    SendMessage(text("They hit the main reactor! All passengers, abandon ship!"), null),
                    SendMessage(text("Proceed to the elevator down to the hangar bay!"), null),
                    SendMessage(Component.empty(), null),
                ),
                SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(93.relativeX(), 359.relativeY(), 11.relativeZ()), 10L, EffectTiming.TICKED), 10)
            )
        )

        bootstrapPhase(
            phaseKey = BROKEN_ELEVATOR,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.PLAYER_MOVEMENT,
                    MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(96, 357, 62, 98, 362, 64), distance = 3.5)),
                    triggerResult = SequenceTrigger.startPhase(LOOK_AT_TRACTOR)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
                        // If looking out window
                        SequenceTrigger(
                            SequenceTriggerTypes.PLAYER_MOVEMENT,
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(96, 358, 69, 96, 361, 72), distance = 5.0)),
                            triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
                        ),
                        // Only trigger this branch if first time
                        SequenceTrigger(
                            SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_dynmap") { it != true },
                            triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
                        )
                    ),
                    triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
                        // If looking out window
                        SequenceTrigger(
                            SequenceTriggerTypes.PLAYER_MOVEMENT,
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(96, 358, 78, 88, 363, 87), distance = 3.0)),
                            triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
                        ),
                        // Only trigger this branch if first time
                        SequenceTrigger(
                            SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_ship_computer") { it != true },
                            triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
                        )

                    ),
                    triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
                )
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,

                ifPreviousPhase(
                    EXIT_CRYOPOD_ROOM, EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    SendMessage(Component.empty(), null),
                    SendMessage(text("Smoke bellows out of the smouldering remains of an elevator, the dorsal hull appears to have taken a direct hit from enemy fire.", GRAY, ITALIC), null),

                    SendMessage(text("There is a backup crew elevator other side of the ship!"), null),
                    SendMessage(Component.empty(), null),
                ),

                SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97.relativeX(), 359.relativeY(), 63.relativeZ()), 10L, EffectTiming.TICKED), 10)
            )
        )

        bootstrapPhase(
            phaseKey = LOOK_AT_TRACTOR,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(SequenceTriggerTypes.USE_TRACTOR_BEAM, TractorBeamTriggerSettings(), triggerResult = SequenceTrigger.startPhase(CREW_QUARTERS)),
                SequenceTrigger(SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
                    // If looking out window
                    SequenceTrigger(
                        SequenceTriggerTypes.PLAYER_MOVEMENT,
                        MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(96, 358, 69, 96, 361, 72), distance = 5.0)),
                        triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
                    ),
                    // Only trigger this branch if first time
                    SequenceTrigger(
                        SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_dynmap") { it != true },
                        triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)
                    )
                ), triggerResult = SequenceTrigger.startPhase(BRANCH_DYNMAP)),
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
                        // If looking out window
                        SequenceTrigger(
                            SequenceTriggerTypes.PLAYER_MOVEMENT,
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(96, 358, 78, 88, 363, 87), distance = 3.0)),
                            triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
                        ),
                        // Only trigger this branch if first time
                        SequenceTrigger(
                            SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_ship_computer") { it != true },
                            triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
                        )
                    ),
                    triggerResult = SequenceTrigger.startPhase(BRANCH_SHIP_COMPUTER)
                )
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,

                ifPreviousPhase(
                    BROKEN_ELEVATOR,
                    EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    SendMessage(Component.empty(), null),
                    SendMessage(text("To use the elevator, hold your controller (clock), stand on the glass block, and courch.", GRAY, ITALIC), null),
                    SendMessage(Component.empty(), null),
                )
            )
        )

        bootstrapPhase(
            phaseKey = CREW_QUARTERS,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
					type = SequenceTriggerTypes.PLAYER_MOVEMENT,
					settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(94, 355, 61, 95, 352, 65))),
					triggerResult = SequenceTrigger.startPhase(FIRE_OBSTACLE)
                )
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                ifPreviousPhase(
                    LOOK_AT_TRACTOR,
                    EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    SendMessage(Component.empty(), null),
                    SendMessage(text("Quick, make your way through the crew quarters and maintainance bays to the hangar bay!", GRAY, ITALIC), null),
                    SendMessage(Component.empty(), null)
                )
            )
        )

        bootstrapPhase(
            phaseKey = FIRE_OBSTACLE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
					type = SequenceTriggerTypes.PLAYER_MOVEMENT,
					settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(91, 351, 24, 95, 354, 24))),
					triggerResult = SequenceTrigger.startPhase(GET_CHETHERITE)
                ),
				SequenceTrigger(
					SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
						// If looking out window
						SequenceTrigger(
							SequenceTriggerTypes.PLAYER_MOVEMENT,
							MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(90, 351, 48, 84, 356, 43), distance = 10.0)),
							triggerResult = SequenceTrigger.startPhase(BRANCH_NAVIGATION)
						),
						// Only trigger this branch if first time
						SequenceTrigger(
							SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_navigation") { it != true },
							triggerResult = SequenceTrigger.startPhase(BRANCH_NAVIGATION)
						)
					), triggerResult = SequenceTrigger.startPhase(BRANCH_NAVIGATION)
				),
				SequenceTrigger(
					SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
						// If looking out window
						SequenceTrigger(
							SequenceTriggerTypes.PLAYER_MOVEMENT,
							MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(96, 351, 51, 100, 356, 41), distance = 10.0)),
							triggerResult = SequenceTrigger.startPhase(BRANCH_MULTIBLOCKS)
						),
						// Only trigger this branch if first time
						SequenceTrigger(
							SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_multiblocks") { it != true },
							triggerResult = SequenceTrigger.startPhase(BRANCH_MULTIBLOCKS)
						)
					), triggerResult = SequenceTrigger.startPhase(BRANCH_MULTIBLOCKS)
				)
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,

				ifPreviousPhase(
					CREW_QUARTERS, EffectTiming.START,
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), EffectTiming.START),
					SendMessage(text("The ship's gravity generators have failed in the attack! Fly over the obstacle!", GRAY, ITALIC), EffectTiming.START),
					SendMessage(Component.empty(), EffectTiming.START),
				)
            )
        )

        bootstrapPhase(
            phaseKey = GET_CHETHERITE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
					type = SequenceTriggerTypes.CONTAINS_ITEM,
					settings = ContainsItemTrigger.ContainsItemTriggerSettings { it?.customItem?.key == CustomItemKeys.CHETHERITE },
					triggerResult = SequenceTrigger.startPhase(RECEIVED_CHETHERITE)
                )
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                NEXT_PHASE_SOUND,
                SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(97.relativeX(), 352.relativeY(), 16.relativeZ()), 10L, EffectTiming.TICKED), 10),
                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(text("Quick, you'll need to grab some fuel for the escape pod's emergancy hyperdrive. You can find some in that gargo container.", GRAY, ITALIC), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),
            )
        )

        bootstrapPhase(
			RECEIVED_CHETHERITE, SequenceKeys.TUTORIAL, listOf(
				SequenceTrigger(
					type = SequenceTriggerTypes.PLAYER_MOVEMENT,
					settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(92, 355, -9, 94, 358, -9))),
					triggerResult = SequenceTrigger.startPhase(ENTERED_ESCAPE_POD)
				),
				SequenceTrigger(
					SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
						// If looking out window
						SequenceTrigger(
							SequenceTriggerTypes.PLAYER_MOVEMENT,
							MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(79, 352, -1, 81, 355, -1), distance = 5.0)),
							triggerResult = SequenceTrigger.startPhase(BRANCH_CARGO_CRATES)
						),
						// Only trigger this branch if first time
						SequenceTrigger(
							SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_crates") { it != true },
							triggerResult = SequenceTrigger.startPhase(BRANCH_CARGO_CRATES)
						)
					), triggerResult = SequenceTrigger.startPhase(BRANCH_CARGO_CRATES)
				)
			), listOf(
				RANDOM_EXPLOSION_SOUND,
				SequencePhaseEffect.OnTickInterval(SequencePhaseEffect.HighlightBlock(Vec3i(93.relativeX(), 356.relativeY(), -16.relativeZ()), 10L, EffectTiming.TICKED), 10),

				ifPreviousPhase(
					GET_CHETHERITE,
					EffectTiming.START,
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), EffectTiming.START),
					SendMessage(text("Chetherite is hyperdrive fuel, you'll need it to travel faster than light.", GRAY, ITALIC), EffectTiming.START),
					SendMessage(text("Now quick! Make your way to the escape pod before the ship is completely lost!", GRAY, ITALIC), EffectTiming.START),
					SendMessage(Component.empty(), EffectTiming.START),
				)
			)
		)

        bootstrapPhase(
            phaseKey = ENTERED_ESCAPE_POD,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(

			),
            effects = listOf(
                NEXT_PHASE_SOUND,
				SequencePhaseEffect.RunCode({ player, _ ->
					Tasks.async {
						StarshipDealers.loadDealerShipUnchecked(player, NPCDealerShip(ConfigurationFiles.serverConfiguration().tutorialEscapePodShip))
					}
				}, EffectTiming.START),
				SequencePhaseEffect.RunCode({ player, _ ->
					player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 1))
				}, EffectTiming.START),
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
                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(text("They look like the infamous Sky Dogs Pirates to you.", GRAY, ITALIC), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_pirates", true, Boolean::class, EffectTiming.END),
            )
        )

        bootstrapPhase(
            phaseKey = BRANCH_DYNMAP,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(ofChildren(text("This server has an interactive web map. You can access it "), formatLink("here", "https://survival.horizonsend.net/")), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),

                GoToPreviousPhase(EffectTiming.START),
                SequencePhaseEffect.SetSequenceData("seen_dynmap", true, Boolean::class, EffectTiming.END),
            )
        )

        bootstrapPhase(
            phaseKey = BRANCH_SHIP_COMPUTER,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(
                    text("This is a starship computer. It is the primary point of interface for ships. They allow piloting, detection, and manage settings.", GRAY, ITALIC),
                    EffectTiming.START
                ),
                SequencePhaseEffect.HighlightBlock(Vec3i(93.relativeX(), 359.relativeY(), 82.relativeZ()), 60L, EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),

                GoToPreviousPhase(EffectTiming.START),
                SequencePhaseEffect.SetSequenceData("seen_ship_computer", true, Boolean::class, EffectTiming.END),
            )
        )

		bootstrapPhase(
			phaseKey = BRANCH_NAVIGATION,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(text(/*TODO*/ "These are navigation machines.", GRAY, ITALIC), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),

				GoToPreviousPhase(EffectTiming.START),

				SequencePhaseEffect.SetSequenceData("seen_navigation", true, Boolean::class, EffectTiming.END),
			)
		)

		bootstrapPhase(
			phaseKey = BRANCH_MULTIBLOCKS,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(text(/*TODO*/ "These are multiblocks", GRAY, ITALIC), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),

				GoToPreviousPhase(EffectTiming.START),

				SequencePhaseEffect.SetSequenceData("seen_multiblocks", true, Boolean::class, EffectTiming.END),
			)
		)

		bootstrapPhase(
			phaseKey = BRANCH_CARGO_CRATES,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(text("These cargo crates won't be making it to their destination.", GRAY, ITALIC), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),

				GoToPreviousPhase(EffectTiming.START),

				SequencePhaseEffect.SetSequenceData("seen_crates", true, Boolean::class, EffectTiming.END),
			)
		)
    }

	private fun fullBoundingBox(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): BoundingBox {
		val minX: Double = (minOf(x1.relativeX(), x2.relativeX())).toDouble()
		val minY: Double = (minOf(y1.relativeY(), y2.relativeY())).toDouble()
		val minZ: Double = (minOf(z1.relativeZ(), z2.relativeZ())).toDouble()
		val maxX: Double = (maxOf(x1.relativeX(), x2.relativeX()) + 1).toDouble()
		val maxY: Double = (maxOf(y1.relativeY(), y2.relativeY()) + 1).toDouble()
		val maxZ: Double = (maxOf(z1.relativeZ(), z2.relativeZ()) + 1).toDouble()

		return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
	}

	private fun Int.relativeX(): Int = (this - 93) + ConfigurationFiles.serverConfiguration().tutorialOrigin.x
	private fun Int.relativeY(): Int = (this - 359) + ConfigurationFiles.serverConfiguration().tutorialOrigin.y
	private fun Int.relativeZ(): Int = (this - 82) + ConfigurationFiles.serverConfiguration().tutorialOrigin.z
}
