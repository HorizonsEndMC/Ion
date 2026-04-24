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
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CRUISE_IDLE
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
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger
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
    companion object {
        val janeColor = TextColor.color(45, 45, 170)
        val janeTitle = text("J.A.N.E.", janeColor)
        val janePrefix = ofChildren(janeTitle, text(" » ", HEColorScheme.HE_DARK_GRAY))
    }

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
        // Place cruiser at: world Tutorial, starship computer (in front of ship) at absolute position:
        // world: Tutorial, x: 1250, y: 192, z: 2000 (world border should be (2500, 2500)) (facing south)
        // Place exit gate at world Tutorial, at absolute position:
        // world: Tutorial, x: 1250, y: 192, z: 1000 (player travels around 900 blocks in ship)
        // gate should point to world Tutorial2, at absolute position:
        // world: TransitHub, x: 1000, y: 192, z: 1000 (when the player finishes the tutorial, they will
        // be teleported to the actual hub world)

        registerTutorialPlayerSection()

        registerTutorialFlightSection()

        registerTutorialBranches()
    }
    private fun registerTutorialPlayerSection() {
        // TUTORIAL.TUTORIAL_START
        bootstrapPhase(
            phaseKey = TUTORIAL_START,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(
                        inBoundingBox(
                            box = fullBoundingBox(
                                Vec3i(-9, -1, -56),
                                Vec3i(-8, 1, -56)
                            )
                        )
                    ),
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
                SendMessage(text("This is the start of the tutorial."), EffectTiming.START),
                SendMessage(text("Exit the cryopod room to begin."), EffectTiming.START),
                emptyMessage(),

                *questMarkerEffects(Vec3i(-9, -1, -56)),
            )
        )

        // TUTORIAL.EXIT_CRYOPOD_ROOM
        bootstrapPhase(
            phaseKey = EXIT_CRYOPOD_ROOM,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.PLAYER_MOVEMENT,
                    MovementTriggerSettings(
                        lookingAtBoundingBox(
                            box = fullBoundingBox(
                                Vec3i(-1, -2, -69),
                                Vec3i(1, 3, -72)
                            ), distance = 4.5
                        )
                    ),
                    triggerResult = SequenceTrigger.startPhase(BROKEN_ELEVATOR)
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_LOOK_OUTSIDE,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(-106, -1, -129),
                        Vec3i(-45, 24, -7)
                    ),
                    distance = 100.0,
                    dataKey = "seen_pirates"
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
                    emptyMessage(),
                    SendMessage(text("The ships's communication system crackles to life:", GRAY, ITALIC), null),
                    SendMessage(text("This is your captain speaking, we're under attack by pirates!"), null),
                    SendMessage(text("They hit the main reactor! All passengers, abandon ship!"), null),
                    SendMessage(text("Proceed to the elevator down to the hangar bay!"), null),
                    emptyMessage(),
                ),

                *questMarkerEffects(Vec3i(0, 0, -71)),
            )
        )

        // TUTORIAL.BROKEN_ELEVATOR
        bootstrapPhase(
            phaseKey = BROKEN_ELEVATOR,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.PLAYER_MOVEMENT,
                    MovementTriggerSettings(
                        lookingAtBoundingBox(
                            box = fullBoundingBox(
                                Vec3i(3, -2, -20),
                                Vec3i(5, 3, -18)
                            ), distance = 3.5
                        )
                    ),
                    triggerResult = SequenceTrigger.startPhase(LOOK_AT_TRACTOR)
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_DYNMAP,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(3, -1, -13),
                        Vec3i(3, 2, -10)
                    ),
                    distance = 5.0,
                    dataKey = "seen_dynmap"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_SHIP_COMPUTER,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(3, -1, -4),
                        Vec3i(-5, 4, 5)
                    ),
                    distance = 5.0,
                    dataKey = "seen_ship_computer"
                ),
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
                    emptyMessage(),
                    SendMessage(
                        text(
                            "Smoke bellows out of the smouldering remains of an elevator, the dorsal hull appears to have taken a direct hit from enemy fire.",
                            GRAY,
                            ITALIC
                        ), null
                    ),

                    SendMessage(text("There is a backup crew elevator other side of the ship!"), null),
                    emptyMessage(),
                ),

                *questMarkerEffects(Vec3i(4, 0, -19)),
            )
        )

        // TUTORIAL.LOOK_AT_TRACTOR
        bootstrapPhase(
            phaseKey = LOOK_AT_TRACTOR,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.USE_TRACTOR_BEAM,
                    TractorBeamTriggerSettings(),
                    triggerResult = SequenceTrigger.startPhase(CREW_QUARTERS)
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_DYNMAP,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(3, -1, -13),
                        Vec3i(3, 2, -10)
                    ),
                    distance = 5.0,
                    dataKey = "seen_dynmap"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_SHIP_COMPUTER,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(3, -1, -4),
                        Vec3i(-5, 4, 5)
                    ),
                    distance = 5.0,
                    dataKey = "seen_ship_computer"
                ),
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
                    emptyMessage(),
                    SendMessage(
                        text(
                            "To use the elevator, hold your controller (clock), stand on the glass block, and sneak.",
                            GRAY,
                            ITALIC
                        ), null
                    ),
                    emptyMessage(),
                )
            )
        )

        // TUTORIAL.CREW_QUARTERS
        bootstrapPhase(
            phaseKey = CREW_QUARTERS,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(
                        inBoundingBox(
                            box = fullBoundingBox(
                                Vec3i(1, -4, -21),
                                Vec3i(2, -7, -17)
                            )
                        )
                    ),
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
                    emptyMessage(),
                    SendMessage(
                        text(
                            "Quick, make your way through the crew quarters and maintenance bays to the hangar bay!",
                            GRAY,
                            ITALIC
                        ), null
                    ),
                    emptyMessage(),
                )
            )
        )

        // TUTORIAL.FIRE_OBSTACLE
        bootstrapPhase(
            phaseKey = FIRE_OBSTACLE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(
                        inBoundingBox(
                            box = fullBoundingBox(
                                Vec3i(-2, -8, -58),
                                Vec3i(2, -5, -58)
                            )
                        )
                    ),
                    triggerResult = SequenceTrigger.startPhase(GET_CHETHERITE)
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_NAVIGATION,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(-3, -8, -34),
                        Vec3i(-9, -3, -39)
                    ),
                    distance = 10.0,
                    dataKey = "seen_navigation"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_MULTIBLOCKS,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(3, -8, -31),
                        Vec3i(7, -3, -41)
                    ),
                    distance = 10.0,
                    dataKey = "seen_multiblocks"
                ),
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
                    emptyMessage(),
                    SendMessage(
                        text(
                            "The ship's gravity generators have failed in the attack! Fly over the obstacle!",
                            GRAY,
                            ITALIC
                        ), EffectTiming.START
                    ),
                    emptyMessage(),
                ),

                *questMarkerEffects(Vec3i(0, -6, -58)),
            )
        )

        // TUTORIAL.GET_CHETHERITE
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
                emptyMessage(),
                SendMessage(
                    text(
                        "Quick, you'll need to grab some fuel for the escape pod's emergancy hyperdrive. You can find some in that gargo container.",
                        GRAY,
                        ITALIC
                    ), EffectTiming.START
                ),
                emptyMessage(),

                *questMarkerEffects(Vec3i(4, -5, -66)),
            )
        )

        // TUTORIAL.GO_TO_ESCAPE_POD
        bootstrapPhase(
            GO_TO_ESCAPE_POD, SequenceKeys.TUTORIAL, listOf(
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(
                        inBoundingBox(
                            box = fullBoundingBox(
                                Vec3i(-1, -4, -93),
                                Vec3i(1, -1, -93)
                            )
                        )
                    ),
                    triggerResult = SequenceTrigger.startPhase(ENTERED_ESCAPE_POD)
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_CARGO_CRATES,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(-14, -7, -83),
                        Vec3i(-12, -4, -83)
                    ),
                    distance = 5.0,
                    dataKey = "seen_crates"
                ),
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

                *questMarkerEffects(Vec3i(0, -3, -98)),

                ifPreviousPhase(
                    GET_CHETHERITE,
                    EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    emptyMessage(),
                    SendMessage(
                        text(
                            "Chetherite is hyperdrive fuel, you'll need it to travel faster than light.",
                            GRAY,
                            ITALIC
                        ), EffectTiming.START
                    ),
                    SendMessage(
                        text(
                            "Now quick! Make your way to the escape pod before the ship is completely lost!",
                            GRAY,
                            ITALIC
                        ), EffectTiming.START
                    ),
                    emptyMessage(),
                )
            )
        )
    }

    private fun registerTutorialFlightSection() {
        // TUTORIAL.ENTERED_ESCAPE_POD
        bootstrapPhase(
            phaseKey = ENTERED_ESCAPE_POD,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("ENTERED_ESCAPE_POD_START", TimeUnit.SECONDS.toMillis(6)),
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_START)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                )
            ),
            description = PhaseDescription(
                description = text("Pilot the escape pod")
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,
                SequencePhaseEffect.RunCode({ player, _ ->
                    Tasks.async {
                        StarshipDealers.loadDealerShipUnchecked(
                            player,
                            NPCDealerShip(ConfigurationFiles.serverConfiguration().tutorialEscapePodShip),
                            silent = true
                        )
                    }
                }, EffectTiming.START),
                SequencePhaseEffect.RunCode({ player, _ ->
                    player.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.BLINDNESS,
                            40,
                            1
                        )
                    )
                }, EffectTiming.START),
                SequencePhaseEffect.PlaySound(
                    org.bukkit.Registry.SOUNDS.getKeyOrThrow(ENTITY_BREEZE_WIND_BURST).key(),
                    StaticFloatAmount(1.0f),
                    StaticFloatAmount(0.0f),
                    EffectTiming.START
                ),

                emptyMessage(),
                emptyMessage(),
                SendMessage(text("You are now piloting the escape pod!", YELLOW, BOLD), EffectTiming.START),
                emptyMessage(),
                emptyMessage(),
                //SendMessage(text("Through the speaker in our shuttle, you hear the panicked voice of the captain once again.", GRAY, ITALIC), EffectTiming.START),
                //SendMessage(text("Attention all escape pods, the Horizon's End Transit Hub is within range! Go *TODO* and fly through the asteroid belt!", GRAY, ITALIC), EffectTiming.START), //TODO - finalize direction
                //SendMessage(Component.empty(), EffectTiming.START),

                SequencePhaseEffect.SuppliedSetSequenceData(
                    "ENTERED_ESCAPE_POD_START",
                    { System.currentTimeMillis() },
                    EffectTiming.START
                ),
            )
        )

        // TUTORIAL.FLIGHT_START
        bootstrapPhase(
            phaseKey = FLIGHT_START,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_START_DELAY", TimeUnit.SECONDS.toMillis(7)),
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_SHIFT)
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,
                SequencePhaseEffect.SuppliedSetSequenceData(
                    "FLIGHT_START_DELAY",
                    { System.currentTimeMillis() },
                    EffectTiming.START
                ),

                emptyMessage(),
                SendMessage(text("A light starts flickering on the control panel.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                SendMessage(text("A robot voice starts to speak.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                janeMessage(
                    text("Hello! I am the Journey Assistive Navigational Educator, or "),
                    janeTitle,
                    delayTicks = 40L
                ),

                janeMessage(
                    text("I am here to assist you and teach you how to pilot this spacecraft."),
                    delayTicks = 80L
                ),

                emptyMessage(80L),
            )
        )

        // TUTORIAL.FLIGHT_SHIFT
        bootstrapPhase(
            phaseKey = FLIGHT_SHIFT,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
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
                        val currentMovementsData =
                            SequenceManager.getSequenceData(player, context.sequence).get<Int>("flight_shift_count")
                        val currentMovements = if (currentMovementsData.isPresent) currentMovementsData.get() else 0
                        SequenceManager.getSequenceData(player, context.sequence)
                            .set("flight_shift_count", currentMovements + 1)
                    }
                ),
            ),
            description = PhaseDescription(
                description = template(
                    text("- Test ship movement by holding your controller and pressing your {0} key"),
                    text("SNEAK", AQUA),
                )
            ),
            effects = listOf(
                janeMessage(
                    template(
                        text("{0} by pressing your {1} key ({2}) {3}."),
                        text("Move the spacecraft", LIGHT_PURPLE),
                        text("SNEAK", AQUA),
                        Component.keybind("key.sneak", YELLOW),
                        text("while holding your controller", GREEN)
                    ),
                ),
                emptyMessage(),

                janeMessage(
                    text("The ship will move in the direction you are looking. Try it out!"),
                    delayTicks = 40L
                ),
                emptyMessage(40L),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = template(
                            text("Press your {0} key ({1}) {2} to {3}"),
                            text("SNEAK", AQUA),
                            Component.keybind("key.sneak", YELLOW),
                            text("while holding your controller", GREEN),
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

        // TUTORIAL.FLIGHT_ROTATION_LEFT
        bootstrapPhase(
            phaseKey = FLIGHT_ROTATION_LEFT,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_ROTATE,
                    ShipRotationTriggerSettings { player, movement ->
                        if (!movement.clockwise) true else {
                            player.userError("Not quite! Try the other direction.")
                            false
                        }
                    },
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_ROTATION_RIGHT)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                ),
            ),
            description = PhaseDescription(
                description = template(
                    text("- Turn the escape pod left by pressing your {0} key"),
                    text("DROP ITEM ", AQUA),
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                janeMessage(
                    template(
                        text("Very well! You can {0} by pressing your {1} key ({2})."),
                        text("turn your ship 90° left", LIGHT_PURPLE),
                        text("DROP ITEM", AQUA),
                        Component.keybind("key.drop", YELLOW),
                    )
                ),
                emptyMessage(),

                janeMessage(
                    template(
                        text("Now give it a try! Press your {0} key ({1}) to {2}."),
                        text("DROP ITEM", AQUA),
                        Component.keybind("key.drop", YELLOW),
                        text("turn left", LIGHT_PURPLE)
                    ),
                    delayTicks = 40L
                ),
                emptyMessage(40L),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = template(
                            text("Press your {0} key to {1}"),
                            Component.keybind("key.drop", YELLOW),
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

        // TUTORIAL.FLIGHT_ROTATION_RIGHT
        bootstrapPhase(
            phaseKey = FLIGHT_ROTATION_RIGHT,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_ROTATE,
                    ShipRotationTriggerSettings { player, movement ->
                        if (movement.clockwise) true else {
                            player.userError("Not quite! Try the other direction.")
                            false
                        }
                    },
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_INTERMISSION)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                )
            ),
            description = PhaseDescription(
                description = template(
                    text("- Turn the escape pod right by pressing your {0} key"),
                    text("SWAP ITEM TO OFFHAND ", AQUA),
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                janeMessage(
                    template(
                        text("Now press your {0} key ({1}) to {2}."),
                        text("SWAP OFF HAND", AQUA),
                        Component.keybind("key.swapOffhand", YELLOW),
                        text("turn right", LIGHT_PURPLE)
                    )
                ),
                emptyMessage(),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = template(
                            text("Press your {0} key to {1}"),
                            Component.keybind("key.swapOffhand", YELLOW),
                            text("turn right", LIGHT_PURPLE),
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

        // TUTORIAL.FLIGHT_INTERMISSION
        bootstrapPhase(
            phaseKey = FLIGHT_INTERMISSION,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_INTERMISSION_START", TimeUnit.SECONDS.toMillis(7)),
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_CRUISE_START)
                ) // TODO replace
            ),
            description = PhaseDescription(template(text("Listen to {0} for further instructions"), janeTitle)),
            effects = listOf(
                NEXT_PHASE_SOUND,
                SequencePhaseEffect.SuppliedSetSequenceData(
                    "FLIGHT_INTERMISSION_START",
                    { System.currentTimeMillis() },
                    EffectTiming.START
                ),

                emptyMessage(),
                janeMessage(text("Now you know the basics, it is time for you to start moving towards your destination.")),
                emptyMessage(),

                SendDelayedMessage(
                    text(
                        "The comms crackle to life and you hear the voice of the captain.",
                        GRAY,
                        ITALIC
                    ), 40L, EffectTiming.START
                ),
                SendDelayedMessage(
                    text("\"The pirates are too busy shooting the cruiser, go now!\"", GRAY, ITALIC),
                    40L,
                    EffectTiming.START
                ),
                emptyMessage(40L),

                janeMessage(
                    template(
                        text("I've marked an escape route on the starship's HUD. Engaging {0} will give you the best chance of escaping."),
                        text("cruising mode", AQUA),
                    ),
                    delayTicks = 80L
                ),
                emptyMessage(80L),

                *questMarkerEffects(Vec3i(0, 0, -1000)),
            )
        )

        // TUTORIAL.FLIGHT_CRUISE_START
        bootstrapPhase(
            phaseKey = FLIGHT_CRUISE_START,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_CRUISE_START,
                    StarshipCruiseStartTrigger.StartCruseTriggerSettings(),
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_CRUISE_IDLE)
                ),
            ),
            description = PhaseDescription(
                description = template(
                    text("- Activate cruise mode by {0} the Cruise sign, or by running the /cruise command"),
                    text("RIGHT CLICKING/USING", AQUA),
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                janeMessage(text("You will cruise through an asteroid belt to reach a hyperspace beacon.")),
                emptyMessage(),

                janeMessage(
                    template(
                        text("Cruising lets your ship move faster than manual flight. You can engage it by clicking the {0} using your {1} ({2}) key."),
                        text("cruise control sign", GREEN),
                        text("USE", AQUA),
                        Component.keybind("key.use", YELLOW),
                    ),
                    delayTicks = 40L
                ),
                emptyMessage(40L),

                janeMessage(
                    template(
                        text("Your ship will move {0} when you activate cruising mode. {1}"),
                        text("in the direction you were looking", AQUA),
                        text("You can also cruise diagonally.", AQUA)
                    ),
                    delayTicks = 100L
                ),
                emptyMessage(100L),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = template(
                            text("Right click the {0} to enable cruise mode"),
                            text("\"cruise sign\" ", AQUA),
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
                ),

                *questMarkerEffects(Vec3i(0, 0, -1000)),
            )
        )

        // TUTORIAL.FLIGHT_CRUISE_IDLE
        bootstrapPhase(
            phaseKey = FLIGHT_CRUISE_IDLE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                ),
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(PlayerMovementTrigger.withinRadius(Vec3i(0, 0, -1000), 300)),
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_CRUISE_STOP)
                )
            ),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Navigate your starship to the hyperspace beacon at: "),
                    text("{0}")
                ),
                position = Vec3i(0, 0, -1000)
            ),

            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                janeMessage(
                    template(
                        text("When you reach the hyperspace beacon, you can stop cruising by clicking the {0} using your {1} ({2}) key."),
                        text("cruise control sign", GREEN),
                        text("ATTACK", AQUA),
                        Component.keybind("key.attack", YELLOW),
                    ),
                ),
                emptyMessage(),

                janeMessage(
                    template(
                        text("To steer your ship while cruising, turning using {0} or {1} will cause the ship to start to accelerate in the new forward direction."),
                        Component.keybind("key.drop", YELLOW),
                        Component.keybind("key.swapOffhand", YELLOW)
                    ),
                    delayTicks = 60L
                ),
                emptyMessage(60L),

                janeMessage(
                    template(
                        text("Manual flight ({0}) is also possible during cruise, and can be used to make small adjustments."),
                        Component.keybind("key.sneak", YELLOW),
                    ),
                    delayTicks = 120L
                ),
                emptyMessage(120L),

                janeMessage(
                    text("Now make your way through the asteroid belt."),
                    delayTicks = 160L
                ),
                emptyMessage(160L),

                janeMessage(
                    template(
                        text("Remember to manually fly ({0}) and turn left ({1}) or right ({2}) to navigate around asteroids."),
                        template(
                            text("{0} while holding a controller and looking in a direction", AQUA),
                            Component.keybind("key.sneak", YELLOW),
                        ),
                        Component.keybind("key.drop", YELLOW),
                        Component.keybind("key.swapOffhand", YELLOW)
                    ),
                    delayTicks = 200L
                ),
                emptyMessage(200L),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = template(
                            text("Navigate through the asteroid belt by {0}, {1} ({2}, {3}), and {4} ({5}, {6})"),
                            text("cruising", AQUA),
                            text("manually flying", AQUA),
                            Component.keybind("key.sneak", YELLOW),
                            text("while holding a controller", YELLOW),
                            text("turning", AQUA),
                            Component.keybind("key.drop", YELLOW),
                            Component.keybind("key.swapOffhand", YELLOW),
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
                ),

                *questMarkerEffects(Vec3i(0, 0, -1000)),
            )
        )

        // TUTORIAL.FLIGHT_CRUISE_STOP
        bootstrapPhase(
            phaseKey = FLIGHT_CRUISE_STOP,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
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

                emptyMessage(),
                janeMessage(
                    template(
                        text("You have cleared the asteroid field. You can now stop cruising and {0}."),
                        text("prepare to jump to hyperspace.", LIGHT_PURPLE)
                    )
                ),
                emptyMessage(),

                janeMessage(
                    template(
                        text("You can stop cruising by {0} ({1}) the cruise control sign, or by running the {2} command."),
                        text("ATTACKING", AQUA),
                        Component.keybind("key.attack", YELLOW),
                        text("/cruise", AQUA)
                    ),
                    delayTicks = 40L
                ),
                emptyMessage(40L),

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

        // TUTORIAL.FLIGHT_CHETHERITE
        bootstrapPhase(
            phaseKey = FLIGHT_CHETHERITE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.HYPERDRIVE_HAS_FUEL,
                    SimpleContextTriggerPredicate(),
                    triggerResult = SequenceTrigger.startPhase(FLIGHT_HYPERSPACE_JUMP)
                ),
            ),
            description = PhaseDescription(
                description = template(
                    text("- Load the hyperdrive hoppers with the {0} you obtained earlier ({1})"),
                    text("chetherite", LIGHT_PURPLE),
                    text("at the back of the ship", AQUA)
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                janeMessage(
                    template(
                        text("Load the {0} you grabbed earlier into the hyperdrive. {1}"),
                        text("chetherite", LIGHT_PURPLE),
                        text("Each hopper needs at least 2 to make a jump.", AQUA)
                    )
                ),
                emptyMessage(),

                janeMessage(
                    ofChildren(
                        text("I've highlighted the hyperdrive."),
                        text("It is in the back of the ship, above the door.", AQUA)
                    ),
                    delayTicks = 60L
                ),
                emptyMessage(60L),

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

        // TUTORIAL.FLIGHT_HYPERSPACE_JUMP
        bootstrapPhase(
            phaseKey = FLIGHT_HYPERSPACE_JUMP,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
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

                emptyMessage(),
                janeMessage(text("Now that the hyperdrive is fueled, execute the command '/jump Horizons_End_Transit_Hub'.")),
                emptyMessage(),
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

        // TUTORIAL.FLIGHT_IN_HYPERSPACE
        bootstrapPhase(
            phaseKey = FLIGHT_IN_HYPERSPACE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_UNPILOT,
                    StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
                    triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
                        event.isCancelled = true; player.userError("You can't release your ship right now!")
                    }
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

                emptyMessage(),
                janeMessage(text("Your starship is now in hyperspace!")),
                janeMessage(text("Moving through this dimension allows travel immensely faster than real space, but real space can still interfere with travel.")),
                janeMessage(text("We are in deep space, so this isn't an issue, but strong gravity wells such as planets and stars can pull you out of hyperspace.")),
                emptyMessage(),
            )
        )
    }

    private fun registerTutorialBranches() {
        // TUTORIAL.BRANCH_LOOK_OUTSIDE
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
                emptyMessage(),
                SendMessage(text("They look like the infamous Sky Dogs Pirates to you.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_pirates", true, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_DYNMAP
        bootstrapPhase(
            phaseKey = BRANCH_DYNMAP,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                emptyMessage(),
                SendMessage(ofChildren(text("This server has an interactive web map. You can access it "), formatLink("here", "https://survival.horizonsend.net/")), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),
                SequencePhaseEffect.SetSequenceData("seen_dynmap", true, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_SHIP_COMPUTER
        bootstrapPhase(
            phaseKey = BRANCH_SHIP_COMPUTER,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                emptyMessage(),
                SendMessage(
                    text("This is a starship computer. It is the primary point of interface for ships. They allow a pilot to start piloting, detect a ship, and manage settings.", GRAY, ITALIC),
                    EffectTiming.START
                ),
                SequencePhaseEffect.HighlightBlock(Vec3i(0, 0, 0), 60L, EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),
                SequencePhaseEffect.SetSequenceData("seen_ship_computer", true, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_NAVIGATION
        bootstrapPhase(
            phaseKey = BRANCH_NAVIGATION,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                NEXT_PHASE_SOUND,
                emptyMessage(),
                SendMessage(text("These are navigation machines and the ship's hyperdrives. " +
                        "Their damaged state prevents this cruiser from escaping into hyperspace.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_navigation", true, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_MULTIBLOCKS
        bootstrapPhase(
            phaseKey = BRANCH_MULTIBLOCKS,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                NEXT_PHASE_SOUND,
                emptyMessage(),
                SendMessage(text("These are power machines. They would normally be used by the" +
                        "crew to supply power to their gear, but you don't think the crew will be returning while" +
                        "the ship is in this state.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_multiblocks", true, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_CARGO_CRATES
        bootstrapPhase(
            phaseKey = BRANCH_CARGO_CRATES,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(

            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                NEXT_PHASE_SOUND,
                emptyMessage(),
                SendMessage(text("These cargo crates won't be making it to their destination.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

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

    private fun emptyMessage(delay: Long = 0L) = if (delay <= 0) {
        SendMessage(Component.empty(), EffectTiming.START)
    } else {
        SendDelayedMessage(Component.empty(), delay, EffectTiming.START)
    }

    private fun janeMessage(vararg message: Component, delayTicks: Long = 0L) = if (delayTicks <= 0) {
        SendMessage(ofChildren(janePrefix, *message), EffectTiming.START)
    } else {
        SendDelayedMessage(ofChildren(janePrefix, *message), delayTicks, EffectTiming.START)
    }

    private fun questMarkerEffects(position: Vec3i): Array<SequencePhaseEffect> = listOf(
        SequencePhaseEffect.OnTickInterval(
            SequencePhaseEffect.DisplayText(
                position = position,
                text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                durationTicks = 2L,
                scale = 2.0f,
                backgroundColor = Color.fromARGB(0x00000000),
                defaultBackground = false,
                seeThrough = true,
                highlight = false,
                positionOffset = Vec3i(0, 0, 0).toVector(),
                EffectTiming.TICKED
            ),
            2
        ),
        SequencePhaseEffect.OnTickInterval(
            SequencePhaseEffect.DisplayDistanceText(
                position = position,
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
    ).toTypedArray()

    private fun lookingBranchTrigger(
        phaseKey: IonRegistryKey<SequencePhase, SequencePhase>,
        lookingAtBoundingBox: BoundingBox,
        distance: Double,
        dataKey: String
    ): SequenceTrigger<*> = SequenceTrigger(
        SequenceTriggerTypes.COMBINED_AND,
        CombinedAndTrigger.CombinedAndTriggerSettings(
            SequenceTrigger(
                SequenceTriggerTypes.PLAYER_MOVEMENT,
                MovementTriggerSettings(lookingAtBoundingBox(box = lookingAtBoundingBox, distance = distance)),
                triggerResult = SequenceTrigger.startPhase(phaseKey)
            ),
            SequenceTrigger(
                SequenceTriggerTypes.DATA_PREDICATE,
                DataPredicate.DataPredicateSettings<Boolean>(dataKey) { it != true },
                triggerResult = SequenceTrigger.startPhase(phaseKey)
            )
        ),
        triggerResult = SequenceTrigger.startPhase(phaseKey)
    )
}
