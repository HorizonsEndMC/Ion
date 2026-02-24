package net.horizonsend.ion.server.features.sequences.phases

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.QUEST_OBJECTIVE_ICON
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.util.StaticFloatAmount
import net.horizonsend.ion.server.configuration.util.VariableFloatAmount
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceKeys
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.Companion.ifPreviousPhase
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.GoToPreviousPhase
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.SendDelayedMessage
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
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CHETHERITE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CRUISE_START
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CRUISE_STOP
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CRUISE_TURN
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_HYPERSPACE_JUMP
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_INTERMISSION
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_IN_HYPERSPACE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_ROTATION_LEFT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_ROTATION_RIGHT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_SHIFT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_START
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.GET_CHETHERITE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.GO_TO_ESCAPE_POD
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.LOOK_AT_TRACTOR
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.TUTORIAL_START
import net.horizonsend.ion.server.features.sequences.trigger.CombinedAndTrigger
import net.horizonsend.ion.server.features.sequences.trigger.ContainsItemTrigger
import net.horizonsend.ion.server.features.sequences.trigger.DataPredicate
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.inBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.lookingAtBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger.Companion.handleEvent
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.features.sequences.trigger.ShipEnterHyperspaceJumpTrigger.ShipEnterHyperspaceJumpTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.ShipManualFlightTrigger
import net.horizonsend.ion.server.features.sequences.trigger.ShipPreExitHyperspaceJumpTrigger
import net.horizonsend.ion.server.features.sequences.trigger.ShipRotateTrigger.ShipRotationTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SimpleContextTriggerPredicate
import net.horizonsend.ion.server.features.sequences.trigger.StarshipCruiseStartTrigger
import net.horizonsend.ion.server.features.sequences.trigger.StarshipCruiseStopTrigger
import net.horizonsend.ion.server.features.sequences.trigger.StarshipUnpilotTrigger
import net.horizonsend.ion.server.features.sequences.trigger.UsedTractorBeamTrigger.TractorBeamTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.WaitTimeTrigger
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.dealers.NPCDealerShip
import net.horizonsend.ion.server.features.starship.dealers.StarshipDealers
import net.horizonsend.ion.server.features.starship.event.StarshipPreExitHyperspaceEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.features.starship.subsystem.misc.HyperdriveSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Color
import org.bukkit.Sound
import org.bukkit.Sound.ENTITY_BREEZE_WIND_BURST
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.BoundingBox
import java.util.concurrent.TimeUnit

class SequencePhaseRegistry : Registry<SequencePhase>(RegistryKeys.SEQUENCE_PHASE) {
    override fun getKeySet(): KeyRegistry<SequencePhase> = SequencePhaseKeys

    override fun boostrap() {
        registerTutorial()
    }

    private fun bootstrapPhase(
		phaseKey: IonRegistryKey<SequencePhase, SequencePhase>,
		sequenceKey: IonRegistryKey<Sequence, Sequence>,
		triggers: Collection<SequenceTrigger<*>>,
        description: PhaseDescription? = null,
		effects: List<SequencePhaseEffect>,
	) = register(phaseKey, SequencePhase(phaseKey, sequenceKey, triggers, description, effects))

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
                    settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(Vec3i(-9, -1, -56), Vec3i(-8, 1, -56)))),
                    triggerResult = SequenceTrigger.startPhase(EXIT_CRYOPOD_ROOM)
                )
            ),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Exit the cryopod room at: "),
                    text("{0}")
                ),
                position = Vec3i(-9, -1, -56)
            ),
            effects = listOf(
                SendMessage(text("Welcome to Horizon's End!"), EffectTiming.START),
                SendMessage(text("This is the start of the intro sequence."), EffectTiming.START),
                SendMessage(text("Exit the cryopod room to begin."), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayText(
                        position = Vec3i(-9, -1, -56),
                        text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayDistanceText(
                        position = Vec3i(-9, -1, -56),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
            )
        )

        bootstrapPhase(
            phaseKey = EXIT_CRYOPOD_ROOM,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.PLAYER_MOVEMENT,
                    MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(-1, -2, -69), Vec3i(1, 3, -72)), distance = 4.5)),
                    triggerResult = SequenceTrigger.startPhase(BROKEN_ELEVATOR)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
                        // If looking out window
                        SequenceTrigger(
                            SequenceTriggerTypes.PLAYER_MOVEMENT,
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(-106, -1, -129), Vec3i(-45, 24, -7)), distance = 100.0)),
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
            description = PhaseDescription(
                description = ofChildren(
                    text("- Access the elevator to the hangar bay at: "),
                    text("{0}")
                ),
                position = Vec3i(0, 0, -71)
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
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayText(
                        position = Vec3i(0, 0, -71),
                        text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayDistanceText(
                        position = Vec3i(0, 0, -71),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
            )
        )

        bootstrapPhase(
            phaseKey = BROKEN_ELEVATOR,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.PLAYER_MOVEMENT,
                    MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(3, -2, -20), Vec3i(5, 3, -18)), distance = 3.5)),
                    triggerResult = SequenceTrigger.startPhase(LOOK_AT_TRACTOR)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
                        // If looking out window
                        SequenceTrigger(
                            SequenceTriggerTypes.PLAYER_MOVEMENT,
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(3, -1, -13), Vec3i(3, 2, -10)), distance = 5.0)),
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
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(3, -1, -4), Vec3i(-5, 4, 5)), distance = 3.0)),
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
            description = PhaseDescription(
                description = ofChildren(
                    text("- Find the backup crew elevator at the stern of the ship, at: "),
                    text("{0}")
                ),
                position = Vec3i(4, 1, -19)
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

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayText(
                        position = Vec3i(4, 0, -19),
                        text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayDistanceText(
                        position = Vec3i(4, 0, -19),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
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
                        MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(3, -1, -13), Vec3i(3, 2, -10)), distance = 5.0)),
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
                            MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(3, -1, -4), Vec3i(-5, 4, 5)), distance = 3.0)),
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
            description = PhaseDescription(
                description = ofChildren(
                    text("- Activate the elevator by "),
                    text("holding your controller (Clock) ", AQUA),
                    text("and "),
                    text("SNEAKING ", AQUA),
                    text("while standing on the "),
                    text("Glass Block ", AQUA),
                )
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,

                ifPreviousPhase(
                    BROKEN_ELEVATOR,
                    EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    SendMessage(Component.empty(), null),
                    SendMessage(text("To use the elevator, hold your controller (clock), stand on the glass block, and sneak.", GRAY, ITALIC), null),
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
					settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(Vec3i(1, -4, -21), Vec3i(2, -7, -17)))),
					triggerResult = SequenceTrigger.startPhase(FIRE_OBSTACLE)
                )
            ),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Exit the elevator at: "),
                    text("{0}")
                ),
                position = Vec3i(1, -5, -19)
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
					settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(Vec3i(-2, -8, -58), Vec3i(2, -5, -58)))),
					triggerResult = SequenceTrigger.startPhase(GET_CHETHERITE)
                ),
				SequenceTrigger(
					SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
						// If looking out window
						SequenceTrigger(
							SequenceTriggerTypes.PLAYER_MOVEMENT,
							MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(-3, -8, -34), Vec3i(-9, -3, -39)), distance = 10.0)),
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
							MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(3, -8, -31), Vec3i(7, -3, -41)), distance = 10.0)),
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
            description = PhaseDescription(
                description = ofChildren(
                    text("- Use zero gravity to hover over the flames, and reach: "),
                    text("{0}")
                ),
                position = Vec3i(0, -6, -58)
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,

				ifPreviousPhase(
					CREW_QUARTERS, EffectTiming.START,
					NEXT_PHASE_SOUND,
					SendMessage(Component.empty(), EffectTiming.START),
					SendMessage(text("The ship's gravity generators have failed in the attack! Fly over the obstacle!", GRAY, ITALIC), EffectTiming.START),
					SendMessage(Component.empty(), EffectTiming.START),
				),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayText(
                        position = Vec3i(0, -6, -58),
                        text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayDistanceText(
                        position = Vec3i(0, -6, -58),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
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
					triggerResult = SequenceTrigger.startPhase(GO_TO_ESCAPE_POD)
                )
            ),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Gather "),
                    text("chetherite ", LIGHT_PURPLE),
                    text("hyperdrive fuel at: "),
                    text("{0}")
                ),
                position = Vec3i(4, -7, -66)
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                NEXT_PHASE_SOUND,
                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(text("Quick, you'll need to grab some fuel for the escape pod's emergancy hyperdrive. You can find some in that gargo container.", GRAY, ITALIC), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayText(
                        position = Vec3i(4, -5, -66),
                        text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayDistanceText(
                        position = Vec3i(4, -5, -66),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
            )
        )
        bootstrapPhase(
			GO_TO_ESCAPE_POD, SequenceKeys.TUTORIAL, listOf(
				SequenceTrigger(
					type = SequenceTriggerTypes.PLAYER_MOVEMENT,
					settings = MovementTriggerSettings(inBoundingBox(box = fullBoundingBox(Vec3i(-1, -4, -93), Vec3i(1, -1, -93)))),
					triggerResult = SequenceTrigger.startPhase(ENTERED_ESCAPE_POD)
				),
				SequenceTrigger(
					SequenceTriggerTypes.COMBINED_AND, CombinedAndTrigger.CombinedAndTriggerSettings(
						// If looking out window
						SequenceTrigger(
							SequenceTriggerTypes.PLAYER_MOVEMENT,
							MovementTriggerSettings(lookingAtBoundingBox(box = fullBoundingBox(Vec3i(-14, -7, -83), Vec3i(-12, -4, -83)), distance = 5.0)),
							triggerResult = SequenceTrigger.startPhase(BRANCH_CARGO_CRATES)
						),
						// Only trigger this branch if first time
						SequenceTrigger(
							SequenceTriggerTypes.DATA_PREDICATE, DataPredicate.DataPredicateSettings<Boolean>("seen_crates") { it != true },
							triggerResult = SequenceTrigger.startPhase(BRANCH_CARGO_CRATES)
						)
					), triggerResult = SequenceTrigger.startPhase(BRANCH_CARGO_CRATES)
				)
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Enter the escape pod at: "),
                    text("{0}")
                ),
                position = Vec3i(0, -2, -93)
            ),
            listOf(
				RANDOM_EXPLOSION_SOUND,
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayText(
                        position = Vec3i(0, -3, -98),
                        text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayDistanceText(
                        position = Vec3i(0, -3, -98),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

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
				SequenceTrigger(
					SequenceTriggerTypes.WAIT_TIME,
					WaitTimeTrigger.WaitTimeTriggerSettings("ENTERED_ESCAPE_POD_START", TimeUnit.SECONDS.toMillis(5)),
					triggerResult = SequenceTrigger.startPhase(FLIGHT_START)
				),
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				)
			),
            description = PhaseDescription(
                description = text("Pilot the escape pod")
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,
				SequencePhaseEffect.RunCode({ player, _ ->
					Tasks.async {
						StarshipDealers.loadDealerShipUnchecked(player, NPCDealerShip(ConfigurationFiles.serverConfiguration().tutorialEscapePodShip), silent = true)
					}
				}, EffectTiming.START),
				SequencePhaseEffect.RunCode({ player, _ -> player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 1)) }, EffectTiming.START),
				SequencePhaseEffect.PlaySound(
					org.bukkit.Registry.SOUNDS.getKeyOrThrow(ENTITY_BREEZE_WIND_BURST).key(),
					StaticFloatAmount(1.0f),
					StaticFloatAmount(0.0f),
					EffectTiming.START
				),

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(text("You are now piloting the escape pod!", YELLOW, BOLD), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),
				//SendMessage(text("Through the speaker in our shuttle, you hear the panicked voice of the captain once again.", GRAY, ITALIC), EffectTiming.START),
				//SendMessage(text("Attention all escape pods, the Horizon’s End Transit Hub is within range! Go *TODO* and fly through the asteroid belt!", GRAY, ITALIC), EffectTiming.START), //TODO - finalize direction
				//SendMessage(Component.empty(), EffectTiming.START),

				SequencePhaseEffect.SuppliedSetSequenceData("ENTERED_ESCAPE_POD_START", { System.currentTimeMillis() }, EffectTiming.START),
            )
        )

		val janeColor = TextColor.color(45, 45, 170)
		val janeTitle = text("J.A.N.E.", janeColor)
		val janePrefix = ofChildren(janeTitle, text(" » ", HEColorScheme.HE_DARK_GRAY))

        bootstrapPhase(
            phaseKey = FLIGHT_START,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_START_DELAY", TimeUnit.SECONDS.toMillis(5)),
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_SHIFT)
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,
                SequencePhaseEffect.SuppliedSetSequenceData("FLIGHT_START_DELAY", { System.currentTimeMillis() }, EffectTiming.START),

                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(text("A light starts flickering on the control panel.", GRAY, ITALIC), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(text("A robot voice starts to speak.", GRAY, ITALIC), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),
                SendDelayedMessage(ofChildren(janePrefix, text("Hello! I am the Journey Assistive Navigational Educator, or "), janeTitle), 40L, EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),
                SendDelayedMessage(ofChildren(janePrefix, text("I am here to assist you and teach you how to pilot this spacecraft.")), 80L, EffectTiming.START),
            )
        )
		bootstrapPhase(
			phaseKey = FLIGHT_SHIFT,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
                // data predicate MUST come before movement detection, as these are only checked on movement and break if the first trigger result is fulfilled
                SequenceTrigger(
                    SequenceTriggerTypes.DATA_PREDICATE,
                    DataPredicate.DataPredicateSettings<Int>("flight_shift_count") { it != null && it >= 5 },
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_ROTATION_LEFT)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_MANUAL_FLIGHT,
                    ShipManualFlightTrigger.ShiftFlightTriggerSettings(),
                    triggerResult = { player, context ->
                        val currentMovementsData = SequenceManager.getSequenceData(player, context.sequence).get<Int>("flight_shift_count")
                        val currentMovements = if (currentMovementsData.isPresent) currentMovementsData.get() else 0
                        SequenceManager.getSequenceData(player, context.sequence).set("flight_shift_count", currentMovements + 1)
                    }
                ),
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Test ship movement by holding your controller and pressing your "),
                    text("SNEAK ", AQUA),
                    text("key")
                )
            ),
			effects = listOf(
                SendMessage(
					ofChildren(
						janePrefix,
						ofChildren(
                            text("Move the spacecraft ", LIGHT_PURPLE),
                            text("by pressing your "),
                            text("SNEAK ", AQUA),
                            text("key ("),
                            Component.keybind("key.sneak", YELLOW),
                            text(").")
                        ),
                        newline(),
					), EffectTiming.START
				),
                SendDelayedMessage(
                    ofChildren(
                        janePrefix,
                        text("The ship will move in the direction you are looking. Try it out!"),
                        newline(),
                    ), 40L, EffectTiming.START
                ),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Press your "),
                            Component.keybind("key.sneak", YELLOW),
                            text(" key "),
                            text("while holding your controller ", GREEN),
                            text("to "),
                            text("move in the direction you are looking", LIGHT_PURPLE)
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_ROTATION_LEFT,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_ROTATE,
					ShipRotationTriggerSettings { player, movement -> if (!movement.clockwise) true else {
						player.userError("Not quite! Try the other direction.")
						false
					} },
					triggerResult = SequenceTrigger.startPhase(FLIGHT_ROTATION_RIGHT)
				),
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Turn the escape pod left by pressing your "),
                    text("DROP ITEM ", AQUA),
                    text("key")
                )
            ),
			effects = listOf(
				NEXT_PHASE_SOUND,

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(
                    ofChildren(
                        janePrefix,
                        text("Very well! You can "),
                        text("turn your ship 90° left ", LIGHT_PURPLE),
                        text("by pressing your "),
                        text("DROP ITEM ", AQUA),
                        text("key ("),
                        Component.keybind("key.drop", YELLOW),
                        text(")."),
                    ),
                    EffectTiming.START
                ),
				SendDelayedMessage(
                    ofChildren(
                        janePrefix,
                        text("Now give it a try! Press your " ),
                        text("DROP ITEM ", AQUA),
                        text("key ("),
                        Component.keybind("key.drop", YELLOW),
                        text(") to "),
                        text("turn left.", LIGHT_PURPLE)
                    ),
                    40L, EffectTiming.START
                ),
				SendMessage(Component.empty(), EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Press your "),
                            Component.keybind("key.drop", YELLOW),
                            text(" key to "),
                            text("turn left", LIGHT_PURPLE)
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_ROTATION_RIGHT,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_ROTATE,
					ShipRotationTriggerSettings { player, movement -> if (movement.clockwise) true else {
						player.userError("Not quite! Try the other direction.")
						false
					} },
					triggerResult = SequenceTrigger.startPhase(FLIGHT_INTERMISSION)
				),
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				)
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Turn the escape pod right by pressing your "),
                    text("SWAP ITEM TO OFFHAND ", AQUA),
                    text("key"),
                )
            ),
			effects = listOf(
				NEXT_PHASE_SOUND,

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(
                    ofChildren(
                        janePrefix,
                        text("Now press your " ),
                        text("SWAP OFF HAND ", AQUA),
                        text("key ("),
                        Component.keybind("key.swapOffhand", YELLOW),
                        text(") to "),
                        text("turn right.", LIGHT_PURPLE)
                    ),
                    EffectTiming.START
                ),
				SendMessage(Component.empty(), EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Press your "),
                            Component.keybind("key.swapOffhand", YELLOW),
                            text(" key to "),
                            text("turn right", LIGHT_PURPLE)
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_INTERMISSION,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
				SequenceTrigger(
					SequenceTriggerTypes.WAIT_TIME,
					WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_INTERMISSION_START", TimeUnit.SECONDS.toMillis(5)),
					triggerResult = SequenceTrigger.startPhase(FLIGHT_CRUISE_START)
				) // TODO replace
			),
            description = PhaseDescription(text("Move to the objective")),
			effects = listOf(
				NEXT_PHASE_SOUND,
				SequencePhaseEffect.SuppliedSetSequenceData("FLIGHT_INTERMISSION_START", { System.currentTimeMillis() }, EffectTiming.START),

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("Now you know the basics, it is time for you to start moving towards your destination.\nMove upwards and fly over the cruiser, heading *insert wind direction*" )), EffectTiming.START), //TODO - finalize direction
				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(text("The comms crackle to life and you hear the voice of the captain", GRAY, ITALIC), EffectTiming.START),
				SendMessage(text("The pirates are too busy shooting the cruiser, go now!", GRAY, ITALIC), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),
				SendDelayedMessage(ofChildren(janePrefix, text("I've marked an escape route on the starship's HUD, move there and we can engage cruise to escape!")), 40L, EffectTiming.START),
				// TODO some kind of objective marker
				SendMessage(Component.empty(), EffectTiming.START),
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_CRUISE_START,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_CRUISE_START,
					StarshipCruiseStartTrigger.StartCruseTriggerSettings(),
					triggerResult = SequenceTrigger.startPhase(FLIGHT_CRUISE_TURN)
				),
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Activate cruise mode by "),
                    text("RIGHT CLICKING/USING ", AQUA),
                    text("the Cruise sign, or by running the /cruise command"),
                )
            ),
			effects = listOf(
				NEXT_PHASE_SOUND,

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("In order to arrive safely at the Transit Hub, you must fly through the asteroid field, but we must move quickly.")), EffectTiming.START),
				SendDelayedMessage(ofChildren(janePrefix, text("Cruising lets your ship move faster than manual flight, you can engage it by "), Component.keybind("key.use"), text("ing the cruise control sign, or using the /cruise command.")), 40L, EffectTiming.START),
				SendDelayedMessage(ofChildren(janePrefix, text("When cruise is engaged, your ship will start to accelerate up to its cruise speed, in the direction you were looking when it started. You can cruise diagonally at full speed, but you " +
					"need thrusters to cruise in other directions.")), 80L, EffectTiming.START),
				SendDelayedMessage(ofChildren(janePrefix, text("You can stop cruising by "), Component.keybind("key.attack"), text("ing the cruise control sign, or repeating the /cruise command.")), 80L, EffectTiming.START),
				SendDelayedMessage(Component.empty(), 80L, EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Right click the \"cruise sign\" to enable cruise mode"),
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_CRUISE_TURN,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
				SequenceTrigger(
					SequenceTriggerTypes.WAIT_TIME,
					WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_CRUISE_TURN_START", TimeUnit.SECONDS.toMillis(5)),
					triggerResult = SequenceTrigger.startPhase(FLIGHT_CRUISE_STOP)
				)
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Turn the escape pod while cruising by pressing your "),
                    text("DROP ITEM ", AQUA),
                    text("or "),
                    text("SWAP ITEM TO OFFHAND ", AQUA),
                    text("key"),
                )
            ),
			effects = listOf(
				NEXT_PHASE_SOUND,
				SequencePhaseEffect.SuppliedSetSequenceData("FLIGHT_CRUISE_TURN_START", { System.currentTimeMillis() }, EffectTiming.START),

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, template(text("To steer your ship while cruising, turning using {0} or {1} will cause the ship to start to accelerate in the new forward direction."), Component.keybind("key.drop"), Component.keybind("key.swapOffhand"))), EffectTiming.START),
				SendDelayedMessage(ofChildren(janePrefix, text("Manual flight is also possible during cruise, and can be used to make small adjustments.")), 0L, EffectTiming.START), //TODO - redo messages
				SendDelayedMessage(ofChildren(janePrefix, text("Now make your way through the asteroid belt.")), 0L, EffectTiming.START), //TODO - redo messages
				SendDelayedMessage(Component.empty(), 0L, EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Turn left or right while cruising"),
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_CRUISE_STOP,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
				SequenceTrigger( // TODO - location predicate
					SequenceTriggerTypes.STARSHIP_CRUISE_STOP,
					StarshipCruiseStopTrigger.StopCruseTriggerSettings(),
					triggerResult = SequenceTrigger.startPhase(FLIGHT_CHETHERITE)
				)
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Deactivate cruise mode by "),
                    text("LEFT CLICKING/ATTACKING ", AQUA),
                    text("the Cruise sign, or by running the /cruise command"),
                )
            ),
			effects = listOf(
				NEXT_PHASE_SOUND,

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("You have cleared the asteroid field, congratulations! You can now stop cruising and prepare to jump to hyperspace.")), EffectTiming.START), //TODO - better messages
				SendMessage(ofChildren(janePrefix, text("You can stop cruising by "), Component.keybind("key.attack"), text("ing the cruise control sign, or repeating the /cruise command.")), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Left click the \"cruise sign\" to disable cruise mode"),
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_CHETHERITE,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
				SequenceTrigger(
					SequenceTriggerTypes.HYPERDRIVE_HAS_FUEL,
					SimpleContextTriggerPredicate(),
					triggerResult = SequenceTrigger.startPhase(FLIGHT_HYPERSPACE_JUMP)
				),
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Load the hyperdrive hoppers with the "),
                    text("chetherite ", LIGHT_PURPLE),
                    text("you obtained earlier (at the back of the ship)")
                )
            ),
			effects = listOf(
				NEXT_PHASE_SOUND,

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("You're going to need to load the Chetherite you grabbed earlier into the hyperdrive. Each hopper needs at least 2 to make a jump.")), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("I've highlighted the hyperdrive, its in the back of the ship, above the door.")), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.RunCode({ player, _ ->
                        Tasks.sync {
                            val starship = PilotedStarships[player] ?: return@sync
                            val hyperdrive: HyperdriveSubsystem = starship.hyperdrives.firstOrNull() ?: return@sync
                            hyperdrive.getHoppers().forEach { //
                                player.sendText(
                                    location = it.location.toCenterLocation(),
                                    text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                                    durationTicks = 2L + 1L,
                                    scale = 1.0f,
                                    seeThrough = true,
                                )
                            }
                        }
                    }, EffectTiming.TICKED),
                    interval = 2,
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_HYPERSPACE_JUMP,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_ENTER_HYPERSPACE,
					ShipEnterHyperspaceJumpTriggerSettings(),
					triggerResult = SequenceTrigger.startPhase(FLIGHT_IN_HYPERSPACE)
				),
			),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Jump to a new star system by entering the command: "),
                    text("/jump Horizons_End_Transit_Hub", AQUA)
                )
            ),
			effects = listOf(
				NEXT_PHASE_SOUND,

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("Now that the hyperdrive is fueled, execute the command ‘/jump Horizons_End_Transit_Hub’.")), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),
                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Run the command: "),
                            newline(),
                            text("/jump Horizons_End_Transit_Hub"),
                            newline(),
                            text("to jump to hyperspace"),
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = true,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                )
			)
		)
		bootstrapPhase(
			phaseKey = FLIGHT_IN_HYPERSPACE,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				),
				SequenceTrigger(
					SequenceTriggerTypes.PRE_EXIT_HYPERSPACE,
					ShipPreExitHyperspaceJumpTrigger.ShipPreExitHyperspaceJumpTriggerSettings(),
					triggerResult = handleEvent<StarshipPreExitHyperspaceEvent> { _, _, event ->
						event.exitLocation.x = 0.0
						event.exitLocation.y = 205.0
						event.exitLocation.z = 0.0
					}
				),
			),
            description = PhaseDescription(text("- Wait until the escape pod completes the hyperspace transit")),
			effects = listOf(
				NEXT_PHASE_SOUND,

				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("Your starship is now in hyperspace!")), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("Moving through this dimension allows travel immensely faster than real space, but real space can still interfere with travel.")), EffectTiming.START),
				SendMessage(ofChildren(janePrefix, text("We are in deep space, so this isn't an issue, but strong gravity wells such as planets and stars can pull you out of hyperspace.")), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START)
			)
		)

    }

    private fun registerTutorialBranches() {
        bootstrapPhase(
            phaseKey = BRANCH_LOOK_OUTSIDE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
				SequenceTrigger(
					SequenceTriggerTypes.STARSHIP_UNPILOT,
					StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
					triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event -> event.isCancelled = true; player.userError("You can't release your ship right now!") }
				)
			),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                SendMessage(Component.empty(), EffectTiming.START),
                SendMessage(text("They look like the infamous Sky Dogs Pirates to you.", GRAY, ITALIC), EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_pirates", true, EffectTiming.END),
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
                SequencePhaseEffect.SetSequenceData("seen_dynmap", true, EffectTiming.END),
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
                    text("This is a starship computer. It is the primary point of interface for ships. They allow a pilot to start piloting, detect a ship, and manage settings.", GRAY, ITALIC),
                    EffectTiming.START
                ),
                SequencePhaseEffect.HighlightBlock(Vec3i(0, 0, 0), 60L, EffectTiming.START),
                SendMessage(Component.empty(), EffectTiming.START),

                GoToPreviousPhase(EffectTiming.START),
                SequencePhaseEffect.SetSequenceData("seen_ship_computer", true, EffectTiming.END),
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
				SendMessage(text("These are navigation machines and the ship's hyperdrives." +
                        "Their damaged state prevents this cruiser from escaping into hyperspace.", GRAY, ITALIC), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),

				GoToPreviousPhase(EffectTiming.START),

				SequencePhaseEffect.SetSequenceData("seen_navigation", true, EffectTiming.END),
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
				SendMessage(text("These are power machines. They would normally be used by the" +
                        "crew to supply power to their gear, but you don't think the crew will be returning while" +
                        "the ship is in this state.", GRAY, ITALIC), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),

				GoToPreviousPhase(EffectTiming.START),

				SequencePhaseEffect.SetSequenceData("seen_multiblocks", true, EffectTiming.END),
			)
		)

		bootstrapPhase(
			phaseKey = BRANCH_CARGO_CRATES,
			sequenceKey = SequenceKeys.TUTORIAL,
			triggers = listOf(

			),
			effects = listOf(
				RANDOM_EXPLOSION_SOUND,
				NEXT_PHASE_SOUND,
				SendMessage(Component.empty(), EffectTiming.START),
				SendMessage(text("These cargo crates won't be making it to their destination.", GRAY, ITALIC), EffectTiming.START),
				SendMessage(Component.empty(), EffectTiming.START),

				GoToPreviousPhase(EffectTiming.START),

				SequencePhaseEffect.SetSequenceData("seen_crates", true, EffectTiming.END),
			)
		)
    }

	private fun fullBoundingBox(pos1: Vec3i, pos2: Vec3i): BoundingBox {
		val (x1: Int, y1: Int, z1: Int) = pos1
		val (x2: Int, y2: Int, z2: Int) = pos2

		val minX: Double = (minOf(x1, x2)).toDouble()
		val minY: Double = (minOf(y1, y2)).toDouble()
		val minZ: Double = (minOf(z1, z2)).toDouble()
		val maxX: Double = (maxOf(x1, x2) + 1).toDouble()
		val maxY: Double = (maxOf(y1, y2) + 1).toDouble()
		val maxZ: Double = (maxOf(z1, z2) + 1).toDouble()

		return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
	}
}
