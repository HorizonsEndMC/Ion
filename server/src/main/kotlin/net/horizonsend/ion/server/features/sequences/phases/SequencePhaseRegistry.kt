package net.horizonsend.ion.server.features.sequences.phases

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.QUEST_OBJECTIVE_ICON
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.configuration.util.StaticFloatAmount
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.core.registration.registries.Registry
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceKeys
import net.horizonsend.ion.server.features.sequences.SequenceKeys.TUTORIAL_TRANSIT_HUB
import net.horizonsend.ion.server.features.sequences.SequenceUtils.ACHIEVEMENT_SOUND
import net.horizonsend.ion.server.features.sequences.SequenceUtils.JANE_TITLE
import net.horizonsend.ion.server.features.sequences.SequenceUtils.NEXT_PHASE_SOUND
import net.horizonsend.ion.server.features.sequences.SequenceUtils.RANDOM_EXPLOSION_SOUND
import net.horizonsend.ion.server.features.sequences.SequenceUtils.RANDOM_HEAVY_TURRET_SOUND
import net.horizonsend.ion.server.features.sequences.SequenceUtils.RANDOM_PHASER_SOUND
import net.horizonsend.ion.server.features.sequences.SequenceUtils.SPAWN_PIRATES
import net.horizonsend.ion.server.features.sequences.SequenceUtils.disallowDroppingItem
import net.horizonsend.ion.server.features.sequences.SequenceUtils.disallowJumpWarmup
import net.horizonsend.ion.server.features.sequences.SequenceUtils.disallowOpeningDoor
import net.horizonsend.ion.server.features.sequences.SequenceUtils.disallowStarshipReleaseTrigger
import net.horizonsend.ion.server.features.sequences.SequenceUtils.disallowStarshipUnpilotTrigger
import net.horizonsend.ion.server.features.sequences.SequenceUtils.emptyMessage
import net.horizonsend.ion.server.features.sequences.SequenceUtils.fullBoundingBox
import net.horizonsend.ion.server.features.sequences.SequenceUtils.janeMessage
import net.horizonsend.ion.server.features.sequences.SequenceUtils.lookingBranchTrigger
import net.horizonsend.ion.server.features.sequences.SequenceUtils.questMarkerEffects
import net.horizonsend.ion.server.features.sequences.SequenceUtils.textInWorld
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.Companion.ifPreviousPhase
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.GoToPreviousPhase
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.SendDelayedMessage
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.SendMessage
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.ARRIVE_AT_PORT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BOARD_SHUTTLE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_ASTERI
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_ASTERI_SHUTTLE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_CARGO_CRATES
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_DYNMAP
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_FLIGHT_INSIDE_BEACON_RANGE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_FLIGHT_OUTSIDE_BEACON_RANGE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_FLIGHT_SHIFT_INCREMENT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_FLIGHT_STOP_CRUISE_INITIATED
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_ILIOS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_ILIOS_SHUTTLE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_LOOK_OUTSIDE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_MULTIBLOCKS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_NAVIGATION
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_REGULUS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_REGULUS_SHUTTLE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_SHIP_COMPUTER
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_SIRIUS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BRANCH_SIRIUS_SHUTTLE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.BROKEN_ELEVATOR
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.CREW_QUARTERS
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.ENTERED_ESCAPE_POD
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.EXIT_CRYOPOD_ROOM
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.ENTER_TRANSIT_HUB
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.EXPLORE_TRANSIT_HUB
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FIRE_OBSTACLE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CHETHERITE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CRUISE_START
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CRUISE_STOP
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_CRUISE_NAVIGATE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_EXIT_HYPERSPACE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_HYPERSPACE_JUMP
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_INTERMISSION
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_IN_HYPERSPACE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_LEAVE_POD
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_PARKING
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_ROTATION_LEFT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_ROTATION_RIGHT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_SHIFT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_SPACE_SUIT
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.FLIGHT_START
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.GET_CHETHERITE
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.GO_TO_ESCAPE_POD
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.LOOK_AT_TRACTOR
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.TUTORIAL_END
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.TUTORIAL_START
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.TUTORIAL_TRANSIT_HUB_END
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.TUTORIAL_TRANSIT_HUB_START
import net.horizonsend.ion.server.features.sequences.trigger.CombinedAndTrigger
import net.horizonsend.ion.server.features.sequences.trigger.HasItemInInventoryTrigger
import net.horizonsend.ion.server.features.sequences.trigger.DataPredicate
import net.horizonsend.ion.server.features.sequences.trigger.HasItemEquippedTrigger
import net.horizonsend.ion.server.features.sequences.trigger.PlayerChangedWorldTrigger
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.inBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.lookingAtBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger.Companion.handleEvent
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger.Companion.multiTriggerResult
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger.Companion.startPhase
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.features.sequences.trigger.ShipEnterHyperspaceJumpTrigger.ShipEnterHyperspaceJumpTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.ShipManualFlightTrigger
import net.horizonsend.ion.server.features.sequences.trigger.ShipPreExitHyperspaceJumpTrigger
import net.horizonsend.ion.server.features.sequences.trigger.ShipRotateTrigger.ShipRotationTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.SimpleContextTriggerPredicate
import net.horizonsend.ion.server.features.sequences.trigger.StarshipCruiseStartTrigger
import net.horizonsend.ion.server.features.sequences.trigger.StarshipCruiseStopTrigger
import net.horizonsend.ion.server.features.sequences.trigger.StarshipMovementTrigger
import net.horizonsend.ion.server.features.sequences.trigger.StarshipMovementTrigger.belowCruiseSpeed
import net.horizonsend.ion.server.features.sequences.trigger.StarshipReleaseTrigger
import net.horizonsend.ion.server.features.sequences.trigger.UsedTractorBeamTrigger.TractorBeamTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.WaitTimeTrigger
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.dealers.NPCDealerShip
import net.horizonsend.ion.server.features.starship.dealers.StarshipDealers
import net.horizonsend.ion.server.features.starship.event.StarshipEnterHyperspaceEvent
import net.horizonsend.ion.server.features.starship.event.StarshipPreExitHyperspaceEvent
import net.horizonsend.ion.server.features.starship.subsystem.misc.HyperdriveSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Sound.ENTITY_BREEZE_WIND_BURST
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

class SequencePhaseRegistry : Registry<SequencePhase>(RegistryKeys.SEQUENCE_PHASE) {
    override fun getKeySet(): KeyRegistry<SequencePhase> = SequencePhaseKeys

    override fun boostrap() {
        registerTutorial()
        registerTutorialTransitHub()
    }

    private fun bootstrapPhase(
		phaseKey: IonRegistryKey<SequencePhase, SequencePhase>,
		sequenceKey: IonRegistryKey<Sequence, Sequence>,
		triggers: Collection<SequenceTrigger<*>>,
        description: PhaseDescription? = null,
		effects: List<SequencePhaseEffect>,
	) = register(phaseKey, SequencePhase(phaseKey, sequenceKey, triggers, description, effects))

    private fun registerTutorial() {
        // Place cruiser at: world Tutorial, starship computer (in front of ship) at absolute position:
        // world: Tutorial, x: 1250, y: 192, z: 1500 (world border should be (1250, 1250)) (facing south)
        // Place exit gate at world Tutorial, at absolute position:
        // world: Tutorial, x: 1250, y: 192, z: 1000 (player travels around 900 blocks in ship)
        // gate should point to world Tutorial2, at absolute position:
        // world: TransitHub, x: 1000, y: 192, z: 1000 (when the player finishes the tutorial, they will
        // be teleported to the actual hub world) (world border 2000 wide, centered at (1000, 1000))
        // (When the ship jumps, move the actual jump to the side of the station)

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
                disallowDroppingItem(),
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
                    triggerResult = startPhase(EXIT_CRYOPOD_ROOM)
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
                emptyMessage(delayTicks = 20L),
                emptyMessage(delayTicks = 20L),
                SendDelayedMessage(text("Welcome to Horizon's End!", GOLD, BOLD), delayTicks = 20L, EffectTiming.START),
                SendDelayedMessage(text("This is the start of the tutorial.", GRAY, ITALIC), delayTicks = 20L, EffectTiming.START),
                SendDelayedMessage(text("Exit the cryopod room to begin.", GRAY, ITALIC), delayTicks = 20L, EffectTiming.START),
                emptyMessage(delayTicks = 20L),
                emptyMessage(delayTicks = 20L),

                *questMarkerEffects(Vec3i(-9, -1, -56)),
            )
        )

        // TUTORIAL.EXIT_CRYOPOD_ROOM
        bootstrapPhase(
            phaseKey = EXIT_CRYOPOD_ROOM,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
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
                    triggerResult = startPhase(BROKEN_ELEVATOR)
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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,

                ifPreviousPhase(
                    TUTORIAL_START, EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    //SPAWN_PIRATES,
                    emptyMessage(),
                    SendMessage(text("The ships's communication system crackles to life:", GRAY, ITALIC), null),
                    SendMessage(text("This is your captain speaking, we're under attack by pirates!", YELLOW, ITALIC), null),
                    SendMessage(text("They hit the main reactor! All passengers, abandon ship!", YELLOW, ITALIC), null),
                    SendMessage(text("Proceed to the elevator down to the hangar bay!", AQUA, ITALIC), null),
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
                disallowDroppingItem(),
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
                    triggerResult = startPhase(LOOK_AT_TRACTOR)
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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,

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

                    SendMessage(text("There is a backup crew elevator other side of the ship!", AQUA, ITALIC), null),
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
                disallowDroppingItem(),
                SequenceTrigger(
                    SequenceTriggerTypes.USE_TRACTOR_BEAM,
                    TractorBeamTriggerSettings(),
                    triggerResult = startPhase(CREW_QUARTERS)
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
                description = template(
                    text("- Activate the elevator by {0} and {1} while standing on the {2}"),
                    text("holding your controller (Clock) ", GREEN),
                    text("SNEAKING ", AQUA),
                    text("Glass Block ", GREEN),
                )
            ),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,
                ifPreviousPhase(
                    BROKEN_ELEVATOR,
                    EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    emptyMessage(),
                    SendMessage(
                        text(
                            "To use the elevator, hold your controller (clock), stand on the glass block, and sneak.",
                            AQUA,
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
                disallowDroppingItem(),
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
                    triggerResult = startPhase(FIRE_OBSTACLE)
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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,

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
                disallowDroppingItem(),
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
                    triggerResult = startPhase(GET_CHETHERITE)
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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,

                ifPreviousPhase(
                    CREW_QUARTERS, EffectTiming.START,
                    NEXT_PHASE_SOUND,
                    emptyMessage(),
                    SendMessage(
                        template(
                            text("The ship's gravity generators have failed in the attack! {0}!", GRAY, ITALIC),
                            template(
                                text("Fly ({0}) over the flames", AQUA, ITALIC),
                                text("Double Jump", GREEN)
                            )
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
                disallowDroppingItem(),
                SequenceTrigger(
                    type = SequenceTriggerTypes.HAS_ITEM_IN_INVENTORY,
                    settings = HasItemInInventoryTrigger.HasItemInInventoryTriggerSetting { it?.customItem?.key == CustomItemKeys.CHETHERITE },
                    triggerResult = startPhase(GO_TO_ESCAPE_POD)
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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,

                NEXT_PHASE_SOUND,
                emptyMessage(),
                SendMessage(
                    template(
                        text("Quick, you'll need to grab some fuel for the escape pod's emergency hyperdrive. {0}.", GRAY, ITALIC),
                        text("You can find some in that cargo container", AQUA, ITALIC)
                    ), EffectTiming.START
                ),
                emptyMessage(),

                *questMarkerEffects(Vec3i(4, -5, -66)),
            )
        )

        // TUTORIAL.GO_TO_ESCAPE_POD
        bootstrapPhase(
            phaseKey = GO_TO_ESCAPE_POD,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
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
                    triggerResult = startPhase(ENTERED_ESCAPE_POD)
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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,

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
                        template(
                            text("Now quick! {0} before the ship is completely lost!", GRAY, ITALIC),
                            text("Make your way to the escape pod", AQUA, ITALIC)
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
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("ENTERED_ESCAPE_POD_START", TimeUnit.SECONDS.toMillis(6)),
                    triggerResult = startPhase(FLIGHT_START)
                )
            ),
            description = PhaseDescription(
                description = text("- Pilot the escape pod")
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
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_START_DELAY", TimeUnit.SECONDS.toMillis(10)),
                    triggerResult = startPhase(FLIGHT_SHIFT)
                )
            ),
            description = PhaseDescription(
                description = template(
                    text("- Get acquainted with {0}"),
                    JANE_TITLE
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,
                SequencePhaseEffect.SuppliedSetSequenceData(
                    "FLIGHT_START_DELAY",
                    { System.currentTimeMillis() },
                    EffectTiming.START
                ),

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                SendMessage(text("A light starts flickering on the control panel.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                SendMessage(text("A robot voice starts to speak.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                janeMessage(
                    text("Hello! I am the Journey Assistive Navigational Educator, or "),
                    JANE_TITLE,
                    delayTicks = 60L
                ),
                emptyMessage(60L),

                janeMessage(
                    text("I am here to assist you and teach you how to pilot this spacecraft."),
                    delayTicks = 120L
                ),

                emptyMessage(120L),
            )
        )

        // TUTORIAL.FLIGHT_SHIFT
        bootstrapPhase(
            phaseKey = FLIGHT_SHIFT,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                // data predicate MUST come before movement detection, as these are only checked on movement and break if the first trigger result is fulfilled
                SequenceTrigger(
                    SequenceTriggerTypes.DATA_PREDICATE,
                    DataPredicate.DataPredicateSettings<Int>("flight_shift_count") { it != null && it >= 5 },
                    triggerResult = startPhase(FLIGHT_ROTATION_LEFT)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_MANUAL_FLIGHT,
                    ShipManualFlightTrigger.ShiftFlightTriggerSettings(),
                    triggerResult = startPhase(BRANCH_FLIGHT_SHIFT_INCREMENT)
                ),
            ),
            description = PhaseDescription(
                description = template(
                    text("- Test ship movement by holding your controller and pressing your {0} key"),
                    text("SNEAK", AQUA),
                )
            ),
            effects = listOf(
                ifPreviousPhase(FLIGHT_START, EffectTiming.START,
                    NEXT_PHASE_SOUND,

                    SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                        delayTicks = 80L
                    ),
                    emptyMessage(80L),
                ),

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
                ),

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
                        seeThrough = false,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),
            )
        )

        // TUTORIAL.FLIGHT_ROTATION_LEFT
        bootstrapPhase(
            phaseKey = FLIGHT_ROTATION_LEFT,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_ROTATE,
                    ShipRotationTriggerSettings { player, movement ->
                        if (!movement.clockwise) true else {
                            player.userError("Not quite! Try the other direction.")
                            false
                        }
                    },
                    triggerResult = startPhase(FLIGHT_ROTATION_RIGHT)
                )
            ),
            description = PhaseDescription(
                description = template(
                    text("- Turn the escape pod left by pressing your {0} key"),
                    text("DROP ITEM ", AQUA),
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                    delayTicks = 100L
                ),
                emptyMessage(100L),

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
                ),

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
                        seeThrough = false,
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
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_ROTATE,
                    ShipRotationTriggerSettings { player, movement ->
                        if (movement.clockwise) true else {
                            player.userError("Not quite! Try the other direction.")
                            false
                        }
                    },
                    triggerResult = startPhase(FLIGHT_INTERMISSION)
                ),
            ),
            description = PhaseDescription(
                description = template(
                    text("- Turn the escape pod right by pressing your {0} key"),
                    text("SWAP ITEM TO OFFHAND", AQUA),
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                ),

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
                        seeThrough = false,
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
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_INTERMISSION_START", TimeUnit.SECONDS.toMillis(13)),
                    triggerResult = startPhase(FLIGHT_CRUISE_START)
                ),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_ROTATE,
                    ShipRotationTriggerSettings { player, _ ->
                        player.userError("Not quite! Try the other direction.")
                        false
                    },
                    triggerResult = startPhase(FLIGHT_ROTATION_RIGHT)
                )
            ),
            description = PhaseDescription(template(text("- Listen to {0} for further instructions"), JANE_TITLE)),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                    ), 100L, EffectTiming.START
                ),
                SendDelayedMessage(
                    text("\"The pirates are too busy shooting the cruiser, go now!\"", YELLOW, ITALIC),
                    100L,
                    EffectTiming.START
                ),
                emptyMessage(100L),

                janeMessage(
                    template(
                        text("I've marked an escape route on the starship's HUD. Engaging {0} will give you the best chance of escaping."),
                        text("cruising mode", AQUA),
                    ),
                    delayTicks = 200L
                ),
                emptyMessage(200L),

                *questMarkerEffects(Vec3i(0, 0, -1000)),
            )
        )

        // TUTORIAL.FLIGHT_CRUISE_START
        bootstrapPhase(
            phaseKey = FLIGHT_CRUISE_START,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_CRUISE_START,
                    StarshipCruiseStartTrigger.StartCruseTriggerSettings(),
                    triggerResult = startPhase(FLIGHT_CRUISE_NAVIGATE)
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

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                    delayTicks = 80L
                ),
                emptyMessage(80L),

                janeMessage(
                    template(
                        text("Your ship will move {0} when you activate cruising mode. {1}"),
                        text("in the direction you were looking", LIGHT_PURPLE),
                        text("You can also cruise diagonally.", AQUA)
                    ),
                    delayTicks = 240L
                ),
                emptyMessage(240L),

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
                        seeThrough = false,
                        highlight = false,
                        EffectTiming.TICKED
                    ),
                    2
                ),

                *questMarkerEffects(Vec3i(0, 0, -1000)),
            )
        )

        // TUTORIAL.FLIGHT_CRUISE_NAVIGATE
        bootstrapPhase(
            phaseKey = FLIGHT_CRUISE_NAVIGATE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    type = SequenceTriggerTypes.STARSHIP_MOVEMENT,
                    settings = StarshipMovementTrigger.StarshipMovementTriggerSettings(StarshipMovementTrigger.withinRadius(Vec3i(0, 0, -1000), 300)),
                    triggerResult = startPhase(FLIGHT_CRUISE_STOP)
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

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                    delayTicks = 140L
                ),
                emptyMessage(140L),

                janeMessage(
                    template(
                        text("Manual flight ({0}) is also possible during cruise, and can be used to make small adjustments."),
                        Component.keybind("key.sneak", YELLOW),
                    ),
                    delayTicks = 300L
                ),
                emptyMessage(300L),

                janeMessage(
                    text("Now make your way through the asteroid belt."),
                    delayTicks = 400L
                ),
                emptyMessage(400L),

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
                    delayTicks = 455L
                ),
                emptyMessage(455L),

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
                        seeThrough = false,
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
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                // Go to FLIGHT_CHETHERITE when the player is nearby the hyperspace beacon and their cruise speed is low enough
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND,
                    CombinedAndTrigger.CombinedAndTriggerSettings(
                        SequenceTrigger(
                            type = SequenceTriggerTypes.STARSHIP_MOVEMENT,
                            settings = StarshipMovementTrigger.StarshipMovementTriggerSettings(StarshipMovementTrigger.withinRadius(Vec3i(0, 0, -1000), 250)),
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                        SequenceTrigger(
                            SequenceTriggerTypes.STARSHIP_MOVEMENT,
                            StarshipMovementTrigger.StarshipMovementTriggerSettings(belowCruiseSpeed(3.0)),
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        )
                    ),
                    triggerResult = startPhase(FLIGHT_CHETHERITE)
                ),
                // Go to BRANCH_FLIGHT_STOP_CRUISE_INITIATED when the player stops cruising for the first time
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND,
                    CombinedAndTrigger.CombinedAndTriggerSettings(
                        SequenceTrigger(
                            type = SequenceTriggerTypes.STARSHIP_CRUISE_STOP,
                            settings = StarshipCruiseStopTrigger.StopCruseTriggerSettings(),
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                        SequenceTrigger(
                            type = SequenceTriggerTypes.DATA_PREDICATE,
                            settings = DataPredicate.DataPredicateSettings<Boolean>("stopped_cruise") { it != true },
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                    ),
                    triggerResult = startPhase(BRANCH_FLIGHT_STOP_CRUISE_INITIATED)
                ),
                // Go to BRANCH_FLIGHT_OUTSIDE_BEACON_RANGE if the player has entered the beacon's range and subsequently left it
                SequenceTrigger(
                    SequenceTriggerTypes.COMBINED_AND,
                    CombinedAndTrigger.CombinedAndTriggerSettings(
                        SequenceTrigger(
                            SequenceTriggerTypes.DATA_PREDICATE,
                            DataPredicate.DataPredicateSettings<Boolean>("starship_inside_beacon_range") { it == true },
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                        SequenceTrigger(
                            SequenceTriggerTypes.STARSHIP_MOVEMENT,
                            StarshipMovementTrigger.StarshipMovementTriggerSettings(StarshipMovementTrigger.outsideRadius(Vec3i(0, 0, -1000), 250)),
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        )
                    ),
                    triggerResult = startPhase(BRANCH_FLIGHT_OUTSIDE_BEACON_RANGE)
                ),
                // Go to BRANCH_FLIGHT_INSIDE_BEACON_RANGE if the player has entered the beacon's range
                SequenceTrigger(
                    type = SequenceTriggerTypes.STARSHIP_MOVEMENT,
                    settings = StarshipMovementTrigger.StarshipMovementTriggerSettings(StarshipMovementTrigger.withinRadius(Vec3i(0, 0, -1000), 250)),
                    triggerResult = startPhase(BRANCH_FLIGHT_INSIDE_BEACON_RANGE)
                ),
            ),
            description = PhaseDescription(
                description = ofChildren(
                    text("- Deactivate cruise mode by "),
                    text("LEFT CLICKING/ATTACKING ", AQUA),
                    text("the Cruise sign, or by running the /cruise command (get close to {0})"),
                ),
                position = Vec3i(0, 0, -1000)
            ),
            effects = listOf(
                ifPreviousPhase(phase = FLIGHT_CRUISE_NAVIGATE, timing = EffectTiming.START,
                    NEXT_PHASE_SOUND,

                    SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                        delayTicks = 120L
                    ),
                    emptyMessage(120L),
                ),

                SequencePhaseEffect.DataConditionalEffects(
                    "starship_inside_beacon_range",
                    { it.getOrDefault(true) },
                    EffectTiming.TICKED,

                    SequencePhaseEffect.OnTickInterval(
                        SequencePhaseEffect.DisplayHudText(
                            distance = 10.0,
                            text = template(
                                text("Left click the {0} to disable cruise mode, and {1}"),
                                text("cruise sign", GREEN),
                                text("wait for the ship to stop", AQUA)
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

                    SequencePhaseEffect.OnTickInterval(
                        SequencePhaseEffect.DisplayHudText(
                            distance = 10.0,
                            text = template(
                                text("Left click the {0} to disable cruise mode, and {1}"),
                                text("cruise sign", GREEN),
                                text("wait for the ship to stop", AQUA)
                            ),
                            durationTicks = 2L,
                            scale = 2.0f,
                            backgroundColor = Color.fromARGB(0x00000000),
                            defaultBackground = false,
                            seeThrough = false,
                            highlight = false,
                            EffectTiming.TICKED
                        ),
                        2
                    ),
                ),

                SequencePhaseEffect.DataConditionalEffects(
                    "starship_inside_beacon_range",
                    { !it.getOrDefault(true) },
                    EffectTiming.TICKED,

                    SequencePhaseEffect.OnTickInterval(
                        SequencePhaseEffect.DisplayHudText(
                            distance = 10.0,
                            text = text("Move the starship back to the hyperspace beacon", RED),
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
                        SequencePhaseEffect.DisplayHudText(
                            distance = 10.0,
                            text = text("Move the starship back to the hyperspace beacon", RED),
                            durationTicks = 2L,
                            scale = 2.0f,
                            backgroundColor = Color.fromARGB(0x00000000),
                            defaultBackground = false,
                            seeThrough = false,
                            highlight = false,
                            EffectTiming.TICKED
                        ),
                        2
                    ),
                ),

                *questMarkerEffects(Vec3i(0, 0, -1000)),
            )
        )

        // TUTORIAL.FLIGHT_CHETHERITE
        bootstrapPhase(
            phaseKey = FLIGHT_CHETHERITE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.HYPERDRIVE_HAS_FUEL,
                    SimpleContextTriggerPredicate(),
                    triggerResult = startPhase(FLIGHT_HYPERSPACE_JUMP)
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

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

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
                        text("I've highlighted the hyperdrive. "),
                        text("It is in the back of the ship, above the door.", AQUA)
                    ),
                    delayTicks = 120L
                ),
                emptyMessage(120L),

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
                                player.sendText(
                                    location = it.location.toCenterLocation(),
                                    text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                                    durationTicks = 2L + 1L,
                                    scale = 1.0f,
                                    seeThrough = false,
                                )
                            }
                        }
                    }, EffectTiming.TICKED),
                    interval = 2,
                ),

                *questMarkerEffects(Vec3i(0, 0, -1000)),
            )
        )

        // TUTORIAL.FLIGHT_HYPERSPACE_JUMP
        bootstrapPhase(
            phaseKey = FLIGHT_HYPERSPACE_JUMP,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_ENTER_HYPERSPACE,
                    ShipEnterHyperspaceJumpTriggerSettings(),
                    triggerResult = multiTriggerResult(
                        startPhase(FLIGHT_IN_HYPERSPACE),
                        handleEvent<StarshipEnterHyperspaceEvent> { _, _, event ->
                            event.movement.totalDistance = 10000.0
                        }
                    )
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

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                janeMessage(
                    template(
                        text("Now that the hyperdrive is fueled, execute (or click in chat) the command '{0}'."),
                        text("/jump Horizons_End_Transit_Hub", AQUA)
                            .hoverEvent(text("Run '/jump Horizons_End_Transit_Hub'"))
                            .clickEvent(ClickEvent.runCommand("/jump Horizons_End_Transit_Hub"))
                    )
                ),
                emptyMessage(),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Run the command: "),
                            newline(),
                            text("/jump Horizons_End_Transit_Hub", AQUA),
                            newline(),
                            text("to jump to hyperspace", LIGHT_PURPLE),
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

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.DisplayHudText(
                        distance = 10.0,
                        text = ofChildren(
                            text("Run the command: "),
                            newline(),
                            text("/jump Horizons_End_Transit_Hub", AQUA),
                            newline(),
                            text("to jump to hyperspace", LIGHT_PURPLE),
                        ),
                        durationTicks = 2L,
                        scale = 2.0f,
                        backgroundColor = Color.fromARGB(0x00000000),
                        defaultBackground = false,
                        seeThrough = false,
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
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.PRE_EXIT_HYPERSPACE,
                    ShipPreExitHyperspaceJumpTrigger.ShipPreExitHyperspaceJumpTriggerSettings(),
                    triggerResult = multiTriggerResult(
                        handleEvent<StarshipPreExitHyperspaceEvent> { _, _, event ->
                            event.exitLocation.y = 205.0
                            event.exitLocation.z += 150.0
                            val transitHubWorld = Bukkit.getWorld("TransitHub")
                            if (transitHubWorld != null) event.exitLocation.world = transitHubWorld
                        },
                        startPhase(FLIGHT_EXIT_HYPERSPACE)
                    )
                ),
            ),
            description = PhaseDescription(text("- Wait until the escape pod completes the hyperspace transit")),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                janeMessage(text("Your starship is now in hyperspace!")),
                emptyMessage(),

                janeMessage(
                    template(
                        text("This alternate dimension allows your ship to {0}"),
                        text("travel much faster than in real space.", LIGHT_PURPLE)
                    ),
                    delayTicks = 100L
                ),
                emptyMessage(100L),

                janeMessage(
                    template(
                        text("However, {0} from {1}, {2}, and {3}, can {4} and {5}."),
                        text("gravity wells", AQUA),
                        text("planets", GREEN),
                        text("stars", GREEN),
                        text("interdicting starships", GREEN),
                        text("pull your ship out of hyperspace", LIGHT_PURPLE),
                        text("prevent you from jumping into hyperspace", LIGHT_PURPLE),
                    ),
                    delayTicks = 200L
                ),
                emptyMessage(200L),

                janeMessage(
                    text("Our journey through deep space avoids all of these, so we will exit hyperspace at the " +
                            "Transit Hub."),
                    delayTicks = 360L
                ),
                emptyMessage(360L),
            )
        )

        // TUTORIAL.FLIGHT_EXIT_HYPERSPACE
        bootstrapPhase(
            phaseKey = FLIGHT_EXIT_HYPERSPACE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("FLIGHT_EXIT_HYPERSPACE_START", TimeUnit.SECONDS.toMillis(5L)),
                    triggerResult = startPhase(TUTORIAL_END)
                )
            ),
            description = PhaseDescription(template(text("- Listen to {0} for further instructions"), JANE_TITLE)),
            effects = listOf(
                SequencePhaseEffect.SuppliedSetSequenceData(
                    "FLIGHT_EXIT_HYPERSPACE_START",
                    { System.currentTimeMillis() },
                    EffectTiming.START
                ),

                emptyMessage(),
                janeMessage(
                    template(
                        text("You have arrived at the {0}."),
                        text("Horizon's End Transit Hub", GREEN),
                    )
                ),
                emptyMessage(),
            )
        )

        // TUTORIAL.TUTORIAL_END
        bootstrapPhase(
            phaseKey = TUTORIAL_END,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowStarshipUnpilotTrigger(),
            ),
            effects = listOf(
                SequencePhaseEffect.EndSequence(EffectTiming.START),
                SequencePhaseEffect.ClearSequenceData(EffectTiming.START),
                SequencePhaseEffect.StartSequence(TUTORIAL_TRANSIT_HUB, EffectTiming.START),
            )
        )
    }

    private fun registerTutorialBranches() {
        // TUTORIAL.BRANCH_LOOK_OUTSIDE
        bootstrapPhase(
            phaseKey = BRANCH_LOOK_OUTSIDE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,
                SequencePhaseEffect.PlayVisualProjectilesAtPlayer(Color.RED, EffectTiming.TICKED),

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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,
                SequencePhaseEffect.PlayVisualProjectilesAtPlayer(Color.RED, EffectTiming.TICKED),

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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,
                SequencePhaseEffect.PlayVisualProjectilesAtPlayer(Color.RED, EffectTiming.TICKED),

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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,
                SequencePhaseEffect.PlayVisualProjectilesAtPlayer(Color.RED, EffectTiming.TICKED),

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
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,
                SequencePhaseEffect.PlayVisualProjectilesAtPlayer(Color.RED, EffectTiming.TICKED),

                NEXT_PHASE_SOUND,
                emptyMessage(),
                SendMessage(text("These are power machines. They would normally be used by the " +
                        "crew to supply power to their gear, but you don't think the crew will be returning while " +
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
            triggers = listOf(),
            effects = listOf(
                RANDOM_EXPLOSION_SOUND,
                RANDOM_HEAVY_TURRET_SOUND,
                RANDOM_PHASER_SOUND,
                SequencePhaseEffect.PlayVisualProjectilesAtPlayer(Color.RED, EffectTiming.TICKED),

                NEXT_PHASE_SOUND,
                emptyMessage(),
                SendMessage(text("These cargo crates won't be making it to their destination.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_crates", true, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_FLIGHT_SHIFT_INCREMENT
        bootstrapPhase(
            phaseKey = BRANCH_FLIGHT_SHIFT_INCREMENT,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                SequencePhaseEffect.DataConditionalEffects(
                    "flight_shift_count",
                    { it.getOrDefault(0) == 0 },
                    EffectTiming.START,
                    janeMessage(
                        text("Good job! Continue to practice your manual flying controls.")
                    ),
                    emptyMessage(),
                ),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.ArithmeticSetSequenceData("flight_shift_count", 1, 0, Int::plus, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_FLIGHT_STOP_CRUISE_INITIATED
        bootstrapPhase(
            phaseKey = BRANCH_FLIGHT_STOP_CRUISE_INITIATED,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                emptyMessage(),
                janeMessage(
                    ofChildren(
                        text("Wait until the ship has fully stopped. "),
                        text("You may need to turn and cruise back to the hyperspace beacon if you are not nearby.", AQUA)
                    )
                ),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("stopped_cruise", true, EffectTiming.END),
            )
        )

        bootstrapPhase(
            phaseKey = BRANCH_FLIGHT_INSIDE_BEACON_RANGE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                GoToPreviousPhase(EffectTiming.START),
                SequencePhaseEffect.SetSequenceData("starship_inside_beacon_range", true, EffectTiming.END),
            )
        )

        // TUTORIAL.BRANCH_FLIGHT_OUTSIDE_BEACON_RANGE
        bootstrapPhase(
            phaseKey = BRANCH_FLIGHT_OUTSIDE_BEACON_RANGE,
            sequenceKey = SequenceKeys.TUTORIAL,
            triggers = listOf(),
            effects = listOf(
                emptyMessage(),
                emptyMessage(),
                janeMessage(
                    text("You have left the hyperspace beacon's range!", RED, BOLD),
                ),
                janeMessage(
                    text("You must move the starship closer to the hyperspace beacon to proceed.", AQUA)
                ),
                emptyMessage(),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("starship_inside_beacon_range", false, EffectTiming.END),
            )
        )
    }

    private fun registerTutorialTransitHub() {
        registerTutorialTransitHubFlightSection()

        registerTutorialTransitHubStationSection()

        registerTutorialTransitHubBranches()
    }

    private fun registerTutorialTransitHubFlightSection() {
        // TUTORIAL_TRANSIT_HUB.TUTORIAL_TRANSIT_HUB_START
        bootstrapPhase(
            phaseKey = TUTORIAL_TRANSIT_HUB_START,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("TUTORIAL_TRANSIT_HUB_START_TIME", TimeUnit.SECONDS.toMillis(1L)),
                    triggerResult = startPhase(FLIGHT_PARKING)
                )
            ),
            effects = listOf(
                SequencePhaseEffect.SuppliedSetSequenceData(
                    "TUTORIAL_TRANSIT_HUB_START_TIME",
                    { System.currentTimeMillis() },
                    EffectTiming.START
                ),
            )
        )

        // TUTORIAL_TRANSIT_HUB.FLIGHT_PARKING
        bootstrapPhase(
            phaseKey = FLIGHT_PARKING,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    type = SequenceTriggerTypes.STARSHIP_MOVEMENT,
                    settings = StarshipMovementTrigger.StarshipMovementTriggerSettings(
                        StarshipMovementTrigger.inBoundingBox(
                            fullBoundingBox(
                                Vec3i(-35, -6, 10),
                                Vec3i(35, 27, 75)
                            )
                        )
                    ),
                    triggerResult = startPhase(FLIGHT_SPACE_SUIT)
                )
            ),
            description = PhaseDescription(
                text("- Move your ship to the docking platform at {0}"),
                position = Vec3i(0, 15, 45)
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                janeMessage(
                    template(
                        text("Move your starship to the docking platform. Remember to manually " +
                                "fly ({0}, {1}) to move your ship accurately."),
                        text("SNEAK", AQUA),
                        Component.keybind("key.sneak", YELLOW),
                    )
                ),
                emptyMessage(),

                *questMarkerEffects(Vec3i(0, 11, 43)),
            )
        )

        // TUTORIAL_TRANSIT_HUB.FLIGHT_SPACE_SUIT
        bootstrapPhase(
            phaseKey = FLIGHT_SPACE_SUIT,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                disallowStarshipReleaseTrigger(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    type = SequenceTriggerTypes.COMBINED_AND,
                    settings = CombinedAndTrigger.CombinedAndTriggerSettings(
                        SequenceTrigger(
                            type = SequenceTriggerTypes.HAS_ITEM_EQUIPPED,
                            settings = HasItemEquippedTrigger.HasItemEquippedTriggerSettings { it?.type == Material.CHAINMAIL_HELMET },
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                        SequenceTrigger(
                            type = SequenceTriggerTypes.HAS_ITEM_EQUIPPED,
                            settings = HasItemEquippedTrigger.HasItemEquippedTriggerSettings { it?.type == Material.CHAINMAIL_CHESTPLATE },
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                        SequenceTrigger(
                            type = SequenceTriggerTypes.HAS_ITEM_EQUIPPED,
                            settings = HasItemEquippedTrigger.HasItemEquippedTriggerSettings { it?.type == Material.CHAINMAIL_LEGGINGS },
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                        SequenceTrigger(
                            type = SequenceTriggerTypes.HAS_ITEM_EQUIPPED,
                            settings = HasItemEquippedTrigger.HasItemEquippedTriggerSettings { it?.type == Material.CHAINMAIL_BOOTS },
                            triggerResult = SequenceTrigger.emptyTriggerResult()
                        ),
                    ),
                    triggerResult = startPhase(FLIGHT_LEAVE_POD)
                ),
            ),
            description = PhaseDescription(
                template(
                    text("- Equip your {0}"),
                    text("space suit", AQUA)
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                janeMessage(
                    template(
                        text("Before leaving your starship, you must first equip your {0}. We " +
                                "wouldn't want you to go through all this trouble only to get spaced!"),
                        text("space suit", AQUA)
                    ),
                ),
                emptyMessage(),
            ),
        )

        // TUTORIAL_TRANSIT_HUB.FLIGHT_LEAVE_POD
        bootstrapPhase(
            phaseKey = FLIGHT_LEAVE_POD,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                disallowOpeningDoor(),
                disallowJumpWarmup(),
                disallowStarshipUnpilotTrigger(),
                SequenceTrigger(
                    SequenceTriggerTypes.STARSHIP_RELEASE,
                    StarshipReleaseTrigger.StarshipReleaseTriggerSettings(),
                    triggerResult = startPhase(ENTER_TRANSIT_HUB)
                )
            ),
            description = PhaseDescription(
                template(
                    text("- Release your starship by {0} the {1}"),
                    text("USING/RIGHT CLICKING", AQUA),
                    text("starship computer", GREEN)
                )
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                janeMessage(
                    template(
                        text("Now you must {0} the starship in order to {1}."),
                        text("release", AQUA),
                        text("exit through the airlock", LIGHT_PURPLE)
                    )
                ),
                emptyMessage(),

                janeMessage(
                    template(
                        text("{0} ({1}) the {2}, while {3}."),
                        text("USE", AQUA),
                        Component.keybind("key.use", YELLOW),
                        text("starship computer", GREEN),
                        text("holding your starship controller", LIGHT_PURPLE)
                    ),
                    delayTicks = 80L
                ),
                emptyMessage(80L),

                janeMessage(
                    ofChildren(
                        text("I've highlighted the starship computer. "),
                        text("It is in the front of the ship, on the floor.", AQUA)
                    ),
                    delayTicks = 140L
                ),
                emptyMessage(140L),

                SequencePhaseEffect.OnTickInterval(
                    SequencePhaseEffect.RunCode({ player, _ ->
                        Tasks.sync {
                            val starship = PilotedStarships[player] ?: return@sync
                            val shipComputerLocation = Vec3i(starship.data.blockKey).toLocation(starship.world).toCenterLocation()
                            player.sendText(
                                location = shipComputerLocation,
                                text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                                durationTicks = 2L + 1L,
                                scale = 1.0f,
                                seeThrough = true,
                            )
                            player.sendText(
                                location = shipComputerLocation,
                                text = text(QUEST_OBJECTIVE_ICON).font(SPECIAL_FONT_KEY),
                                durationTicks = 2L + 1L,
                                scale = 1.0f,
                                seeThrough = false,
                            )
                        }
                    }, EffectTiming.TICKED),
                    interval = 2,
                )
            )
        )
    }

    private fun registerTutorialTransitHubStationSection() {
        // TUTORIAL_TRANSIT_HUB.ENTER_TRANSIT_HUB
        bootstrapPhase(
            phaseKey = ENTER_TRANSIT_HUB,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(
                        inBoundingBox(
                            fullBoundingBox(
                                Vec3i(-7, 3, 29),
                                Vec3i(7, 9, 33)
                            )
                        )
                    ),
                    triggerResult = startPhase(EXPLORE_TRANSIT_HUB)
                )
            ),
            description = PhaseDescription(
                text("- Leave the starship and move to the transit hub landing platform at {0}"),
                position = Vec3i(0, 6, 31)
            ),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                janeMessage(
                    template(
                        text("Egress the starship through the {0} and spacewalk to the transit hub landing platform."),
                        text("rear airlock", AQUA),
                    )
                ),
                emptyMessage(),

                janeMessage(
                    text("I've transferred my communication link to your auditory uplink, so I'll still be " +
                            "able to assist you while disconnected from the starship."),
                    delayTicks = 80L
                ),
                emptyMessage(delayTicks = 80L),

                *questMarkerEffects(Vec3i(0, 6, 31)),
            )
        )

        // TUTORIAL_TRANSIT_HUB.EXPLORE_TRANSIT_HUB
        bootstrapPhase(
            phaseKey = EXPLORE_TRANSIT_HUB,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_MOVEMENT,
                    settings = MovementTriggerSettings(
                        inBoundingBox(
                            fullBoundingBox(
                                Vec3i(37, 4, -2),
                                Vec3i(41, 7, 2)
                            )
                        )
                    ),
                    triggerResult = startPhase(BOARD_SHUTTLE)
                ),
                // Bottom left
                lookingBranchTrigger(
                    phaseKey = BRANCH_ASTERI,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(-25, 4, 5),
                        Vec3i(-21, 8, 13)
                    ),
                    distance = 5.0,
                    dataKey = "seen_asteri"
                ),
                // Top right
                lookingBranchTrigger(
                    phaseKey = BRANCH_SIRIUS,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(21, 4, -13),
                        Vec3i(25, 8, -5)
                    ),
                    distance = 5.0,
                    dataKey = "seen_sirius"
                ),
                // Top left
                lookingBranchTrigger(
                    phaseKey = BRANCH_REGULUS,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(-25, 4, -13),
                        Vec3i(-21, 8, -5)
                    ),
                    distance = 5.0,
                    dataKey = "seen_regulus"
                ),
                // Bottom right
                lookingBranchTrigger(
                    phaseKey = BRANCH_ILIOS,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(21, 4, 5),
                        Vec3i(25, 8, 13)
                    ),
                    distance = 5.0,
                    dataKey = "seen_ilios"
                ),
            ),
            description = PhaseDescription(
                text("- Get to the shuttle platforms at {0}, or read about the different systems"),
                position = Vec3i(39, 6, 0)
            ),
            effects = listOf(
                ifPreviousPhase(
                    ENTER_TRANSIT_HUB, EffectTiming.START,
                    NEXT_PHASE_SOUND,

                    SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                    emptyMessage(),
                    janeMessage(
                        text(
                            "Unfortunately, the attack on the transport cruiser leaves you stranded " +
                                    "from your original destination. One of the four systems here will have to do " +
                                    "while you sort things out."
                        )
                    ),
                    emptyMessage(),

                    janeMessage(
                        template(
                            text("Feel free to {0}. When you're finished, {1} to board a shuttle to that system."),
                            text("read about the different systems", AQUA),
                            text("proceed to the system shuttles", AQUA)
                        ),
                        delayTicks = 200L
                    ),
                    emptyMessage(delayTicks = 200L),
                ),

                *questMarkerEffects(Vec3i(39, 6, 0)),

                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_asteri",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(-23, 8, 9)),
                ),
                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_sirius",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(23, 8, -9)),
                ),
                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_regulus",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(-23, 8, -9)),
                ),
                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_ilios",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(23, 8, 9)),
                )
            )
        )

        // TUTORIAL_TRANSIT_HUB.BOARD_SHUTTLE
        bootstrapPhase(
            phaseKey = BOARD_SHUTTLE,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                SequenceTrigger(
                    type = SequenceTriggerTypes.PLAYER_CHANGED_WORLD,
                    settings = PlayerChangedWorldTrigger.PlayerChangedWorldTriggerSettings(
                        { _, context -> (context.event as? PlayerChangedWorldEvent)?.from?.name == "TransitHub" }
                    ),
                    triggerResult = startPhase(ARRIVE_AT_PORT)
                ),

                // Shuttle confirmation messages
                lookingBranchTrigger(
                    phaseKey = BRANCH_ASTERI_SHUTTLE,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(104, 2, 23),
                        Vec3i(112, 6, 40)
                    ),
                    distance = 5.0,
                    dataKey = "seen_asteri_shuttle"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_SIRIUS_SHUTTLE,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(82, 2, -40),
                        Vec3i(90, 6, -23)
                    ),
                    distance = 5.0,
                    dataKey = "seen_sirius_shuttle"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_REGULUS_SHUTTLE,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(104, 2, -40),
                        Vec3i(112, 6, -23)
                    ),
                    distance = 5.0,
                    dataKey = "seen_regulus_shuttle"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_ILIOS_SHUTTLE,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(126, 2, 23),
                        Vec3i(134, 6, 40)
                    ),
                    distance = 5.0,
                    dataKey = "seen_ilios_shuttle"
                ),

                // Looking at previous displays
                lookingBranchTrigger(
                    phaseKey = BRANCH_ASTERI,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(-25, 4, 5),
                        Vec3i(-21, 8, 13)
                    ),
                    distance = 5.0,
                    dataKey = "seen_asteri"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_SIRIUS,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(21, 4, -13),
                        Vec3i(25, 8, -5)
                    ),
                    distance = 5.0,
                    dataKey = "seen_sirius"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_REGULUS,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(-25, 4, -13),
                        Vec3i(-21, 8, -5)
                    ),
                    distance = 5.0,
                    dataKey = "seen_regulus"
                ),
                lookingBranchTrigger(
                    phaseKey = BRANCH_ILIOS,
                    lookingAtBoundingBox = fullBoundingBox(
                        Vec3i(21, 4, 5),
                        Vec3i(25, 8, 13)
                    ),
                    distance = 5.0,
                    dataKey = "seen_ilios"
                ),
            ),
            description = PhaseDescription(
                text("- Board one of the shuttles and depart for another system"),
            ),
            effects = listOf(
                ifPreviousPhase(
                    EXPLORE_TRANSIT_HUB, EffectTiming.START,
                    NEXT_PHASE_SOUND,

                    SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                    emptyMessage(),
                    janeMessage(
                        text(
                            "Now that you're well-informed, please board a shuttle and take a " +
                                    "ride to the system's transit port."
                        )
                    ),
                    emptyMessage(),

                    janeMessage(
                        template(
                            text("Don't worry too much about your choice. {0}."),
                            text(
                                "You can always travel or move to another system later on " +
                                        "if you so choose", GREEN
                            ),
                        ),
                        delayTicks = 120L
                    ),
                    emptyMessage(delayTicks = 120L),

                    janeMessage(
                        text(
                            "If you need a moment to think, you can go back to the transit hub displays " +
                                    "and read more about the systems."
                        ),
                        delayTicks = 260L
                    ),
                    emptyMessage(260L),
                ),

                *questMarkerEffects(Vec3i(110, 6, 38)),
                textInWorld(Vec3i(110, 10, 38), text("Shuttle to Asteri")),
                *questMarkerEffects(Vec3i(88, 6, -38)),
                textInWorld(Vec3i(88, 10, -38), text("Shuttle to Sirius")),
                *questMarkerEffects(Vec3i(110, 6, -38)),
                textInWorld(Vec3i(110, 10, -38), text("Shuttle to Regulus")),
                *questMarkerEffects(Vec3i(132, 6, 38)),
                textInWorld(Vec3i(132, 10, 38), text("Shuttle to Ilios")),

                // In case the player did not see the displays in the previous step
                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_asteri",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(-23, 8, 9)),
                ),
                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_sirius",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(23, 8, -9)),
                ),
                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_regulus",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(-23, 8, -9)),
                ),
                SequencePhaseEffect.DataConditionalEffects<Boolean>(
                    "seen_ilios",
                    { it.getOrNull() != true },
                    EffectTiming.TICKED,
                    *questMarkerEffects(Vec3i(23, 8, 9)),
                )
            ),
        )

        bootstrapPhase(
            phaseKey = ARRIVE_AT_PORT,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(
                disallowDroppingItem(),
                SequenceTrigger(
                    SequenceTriggerTypes.WAIT_TIME,
                    WaitTimeTrigger.WaitTimeTriggerSettings("ARRIVED_AT_STATION_DELAY_TIMER", TimeUnit.SECONDS.toMillis(64)),
                    triggerResult = startPhase(TUTORIAL_TRANSIT_HUB_END)
                )
            ),
            description = PhaseDescription(template(text("- Listen to {0} for further instructions"), JANE_TITLE)),
            effects = listOf(
                NEXT_PHASE_SOUND,

                SequencePhaseEffect.ClearDelayedMessages(EffectTiming.START),

                emptyMessage(),
                janeMessage(text("You have now arrived at this system's port terminal.")),
                emptyMessage(),

                janeMessage(
                    template(
                        text("You will respawn here by default (unless you build a {0}), and you can also purchase a ship at that {1} over there."),
                        text("cryopod", AQUA),
                        text("Ship Dealer", GOLD)
                    ),
                    delayTicks = 60L
                ),
                emptyMessage(60L),

                janeMessage(
                    template(
                        text("If you would like to {0} and become a prospector for ores, purchase an {1} equipped with a {2} and on-board refining systems."),
                        text("mine asteroids", LIGHT_PURPLE),
                        text("Ozark", AQUA),
                        text("Mining Laser", GREEN),
                    ),
                    delayTicks = 220L
                ),
                emptyMessage(220L),

                janeMessage(
                    template(
                        text("Or, if you prefer {0}, buy a {1} equipped with {2} and blast the starships of various {3}, or {4} like yourself."),
                        text("starship combat", LIGHT_PURPLE),
                        text("Vulture", AQUA),
                        text("Plasma Cannons", GREEN),
                        text("NPC factions", YELLOW),
                        text("other pilots", RED),
                    ),
                    delayTicks = 380L
                ),
                emptyMessage(380L),

                janeMessage(
                    template(
                        text("If you're not sure on what you want to do, I'd recommend the {0}, as it can {1} to {2} and is well-suited for any colonization effort."),
                        text("Vesta", AQUA),
                        text("haul cargo crates", LIGHT_PURPLE),
                        text("trade cities", GREEN),
                    ),
                    delayTicks = 560L
                ),
                emptyMessage(560L),

                janeMessage(
                    template(
                        text("From here, the possibilities are endless. {0} to form {1} and {2}, {3} to impose your economic and military influence, and {4} to discover new locations and cultures."),
                        text("Team up with other pilots", LIGHT_PURPLE),
                        text("settlements", AQUA),
                        text("nations", AQUA),
                        text("build starships", LIGHT_PURPLE),
                        text("explore new planets and star systems", LIGHT_PURPLE),
                    ),
                    delayTicks = 760L
                ),
                emptyMessage(760L),

                janeMessage(
                    template(
                        text("If you're ever lost on what to do, {0} as they will surely be able to help."),
                        text("ask other pilots in chat for help", LIGHT_PURPLE),
                    ),
                    delayTicks = 1020L
                ),
                emptyMessage(1020L),

                janeMessage(
                    template(
                        text("Wherever your path takes you, {0}."),
                        text("may your journey lead you towards the stars", GOLD),
                    ),
                    delayTicks = 1180L
                ),
                emptyMessage(1180L),

                SequencePhaseEffect.SuppliedSetSequenceData(
                    "ARRIVED_AT_STATION_DELAY_TIMER",
                    { System.currentTimeMillis() },
                    EffectTiming.START
                ),
            )
        )

        bootstrapPhase(
            phaseKey = TUTORIAL_TRANSIT_HUB_END,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                ACHIEVEMENT_SOUND,
                SequencePhaseEffect.EndSequence(EffectTiming.START),
                SequencePhaseEffect.ClearSequenceData(EffectTiming.START),
                SequencePhaseEffect.SendTitle(Title.title(text("MISSION COMPLETE", GOLD), text("Tutorial", AQUA)), EffectTiming.START)
            )
        )
    }

    private fun registerTutorialTransitHubBranches() {
        // TUTORIAL_TRANSIT_HUB.BRANCH_ASTERI
        bootstrapPhase(
            phaseKey = BRANCH_ASTERI,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Asteri is the safest system, with many planets and enough resources to support the aspiring colonist.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_asteri", true, EffectTiming.END),
            )
        )

        // TUTORIAL_TRANSIT_HUB.BRANCH_SIRIUS
        bootstrapPhase(
            phaseKey = BRANCH_SIRIUS,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Sirius is home to many trade cities and highly populated worlds, and also borders the frontier system of Horizon.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_sirius", true, EffectTiming.END),
            )
        )

        // TUTORIAL_TRANSIT_HUB.BRANCH_REGULUS
        bootstrapPhase(
            phaseKey = BRANCH_REGULUS,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Regulus is considered the central system of the Core Worlds, with lots of traffic in both trade and piracy.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_regulus", true, EffectTiming.END),
            )
        )

        // TUTORIAL_TRANSIT_HUB.BRANCH_ILIOS
        bootstrapPhase(
            phaseKey = BRANCH_ILIOS,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Ilios is a frontier system outside of the Core Worlds, with many resources waiting to be exploited by industrial nations.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_ilios", true, EffectTiming.END),
            )
        )

        // TUTORIAL_TRANSIT_HUB.BRANCH_ASTERI_SHUTTLE
        bootstrapPhase(
            phaseKey = BRANCH_ASTERI_SHUTTLE,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Are you sure you want to travel to Asteri, the Sanctuary System? You can always travel to the other systems later on.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_asteri_shuttle", true, EffectTiming.END),
            )
        )

        // TUTORIAL_TRANSIT_HUB.BRANCH_SIRIUS_SHUTTLE
        bootstrapPhase(
            phaseKey = BRANCH_SIRIUS_SHUTTLE,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Are you sure you want to travel to Sirius, the Prosperous System? You can always travel to the other systems later on.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_sirius_shuttle", true, EffectTiming.END),
            )
        )

        // TUTORIAL_TRANSIT_HUB.BRANCH_REGULUS_SHUTTLE
        bootstrapPhase(
            phaseKey = BRANCH_REGULUS_SHUTTLE,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Are you sure you want to travel to Regulus, the Crossroads System? You can always travel to the other systems later on.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_regulus_shuttle", true, EffectTiming.END),
            )
        )

        // TUTORIAL_TRANSIT_HUB.BRANCH_ILIOS_SHUTTLE
        bootstrapPhase(
            phaseKey = BRANCH_ILIOS_SHUTTLE,
            sequenceKey = TUTORIAL_TRANSIT_HUB,
            triggers = listOf(),
            effects = listOf(
                NEXT_PHASE_SOUND,

                emptyMessage(),
                SendMessage(text("Are you sure you want to travel to Ilios, the Abundant System? You can always travel to the other systems later on.", GRAY, ITALIC), EffectTiming.START),
                emptyMessage(),

                GoToPreviousPhase(EffectTiming.START),

                SequencePhaseEffect.SetSequenceData("seen_ilios_shuttle", true, EffectTiming.END),
            )
        )
    }
}
