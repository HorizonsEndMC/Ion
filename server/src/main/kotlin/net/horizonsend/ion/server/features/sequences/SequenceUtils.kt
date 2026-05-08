package net.horizonsend.ion.server.features.sequences

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.QUEST_OBJECTIVE_ICON
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.util.StaticFloatAmount
import net.horizonsend.ion.server.configuration.util.VariableFloatAmount
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.sequences.effect.EffectTiming
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.SendDelayedMessage
import net.horizonsend.ion.server.features.sequences.effect.SequencePhaseEffect.SendMessage
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import net.horizonsend.ion.server.features.sequences.trigger.CombinedAndTrigger
import net.horizonsend.ion.server.features.sequences.trigger.DataPredicate
import net.horizonsend.ion.server.features.sequences.trigger.PlayerInteractTrigger
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.MovementTriggerSettings
import net.horizonsend.ion.server.features.sequences.trigger.PlayerMovementTrigger.lookingAtBoundingBox
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger.Companion.emptyTriggerResult
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTrigger.Companion.handleEvent
import net.horizonsend.ion.server.features.sequences.trigger.SequenceTriggerTypes
import net.horizonsend.ion.server.features.sequences.trigger.SimpleContextTriggerPredicate
import net.horizonsend.ion.server.features.sequences.trigger.StarshipUnpilotTrigger
import net.horizonsend.ion.server.features.starship.event.StarshipJumpWarmupEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.DOOR_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.BoundingBox

object SequenceUtils {
    //region Common Effects
    val RANDOM_EXPLOSION_SOUND = SequencePhaseEffect.Chance(
        SequencePhaseEffect.PlaySound(
            RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_GENERIC_EXPLODE)!!,
            VariableFloatAmount(0.05f, 1.0f),
            StaticFloatAmount(1.0f),
            EffectTiming.TICKED
        ),
        0.02
    )

    val NEXT_PHASE_SOUND = SequencePhaseEffect.PlaySound(
        RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.ENTITY_ARROW_HIT_PLAYER)!!,
        StaticFloatAmount(1.0f),
        StaticFloatAmount(2.0f),
        EffectTiming.START
    )

    val ACHIEVEMENT_SOUND = SequencePhaseEffect.PlaySound(
        RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).getKey(Sound.UI_TOAST_CHALLENGE_COMPLETE)!!,
        StaticFloatAmount(1.0f),
        StaticFloatAmount(1.0f),
        EffectTiming.START
    )

    fun emptyMessage(delayTicks: Long = 0L) = if (delayTicks <= 0) {
        SendMessage(Component.empty(), EffectTiming.START)
    } else {
        SendDelayedMessage(Component.empty(), delayTicks, EffectTiming.START)
    }

    fun janeMessage(vararg message: Component, delayTicks: Long = 0L) = if (delayTicks <= 0) {
        SendMessage(ofChildren(JANE_PREFIX, *message), EffectTiming.START)
    } else {
        SendDelayedMessage(ofChildren(JANE_PREFIX, *message), delayTicks, EffectTiming.START)
    }

    fun textInWorld(position: Vec3i, vararg message: Component) = SequencePhaseEffect.OnTickInterval(
        SequencePhaseEffect.DisplayText(
            position = position,
            text = ofChildren(*message),
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
    )

    fun questMarkerEffects(position: Vec3i): Array<SequencePhaseEffect> = listOf(
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
        ),
    ).toTypedArray()
    //endregion

    //region Common Triggers
    fun lookingBranchTrigger(
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
                triggerResult = emptyTriggerResult()
            ),
            SequenceTrigger(
                SequenceTriggerTypes.DATA_PREDICATE,
                DataPredicate.DataPredicateSettings<Boolean>(dataKey) { it != true },
                triggerResult = emptyTriggerResult()
            )
        ),
        triggerResult = SequenceTrigger.startPhase(phaseKey)
    )

    fun disallowStarshipUnpilotTrigger() = SequenceTrigger(
        SequenceTriggerTypes.STARSHIP_UNPILOT,
        StarshipUnpilotTrigger.ShipUnpilotTriggerSettings(),
        triggerResult = handleEvent<StarshipUnpilotEvent> { player, _, event ->
            event.isCancelled = true; player.userError("You can't release your ship right now!")
        }
    )

    fun disallowOpeningDoor() = SequenceTrigger(
        SequenceTriggerTypes.PLAYER_INTERACT,
        PlayerInteractTrigger.PlayerInteractTriggerSettings(),
        triggerResult = handleEvent<PlayerInteractEvent> { player, _, event ->
            if (DOOR_TYPES.contains(event.clickedBlock?.type)) {
                player.userError("You can't open the starship airlock right now!")
                event.isCancelled = true
            }
        }
    )

    fun disallowJumpWarmup() = SequenceTrigger(
        SequenceTriggerTypes.STARSHIP_JUMP_WARMUP,
        SimpleContextTriggerPredicate(),
        triggerResult = handleEvent<StarshipJumpWarmupEvent> { player, _, event ->
            player.userError("You can't jump to hyperspace right now!")
            event.isCancelled = true
        }
    )
    //endregion

    //region Other Common Elements
    val JANE_COLOR = TextColor.color(45, 45, 170)
    val JANE_TITLE = text("J.A.N.E.", JANE_COLOR)
    val JANE_PREFIX = ofChildren(JANE_TITLE, text(" » ", HEColorScheme.HE_DARK_GRAY))

    fun fullBoundingBox(pos1: Vec3i, pos2: Vec3i): BoundingBox {
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
    //endregion
}
