package net.horizonsend.ion.server.features.sequences.effect

import kotlinx.serialization.InternalSerializationApi
import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.configuration.util.FloatAmount
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceContext
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.VisualProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.MinecraftServer
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.Optional
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

abstract class SequencePhaseEffect(val timing: EffectTiming?) {
	abstract fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext)

	/**
	 * Represents an effect that signals the end of a sequence's current phase.
	 *
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class EndSequence(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.endPhase(player, sequenceKey) }
	}

	/**
	 * Represents an effect that sets the sequence's current phase.
	 *
	 * @param phase The phase to transition to.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class GoToPhase(val phase: IonRegistryKey<SequencePhase, SequencePhase>, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.startPhase(player, sequenceKey, phase) }
	}

	/**
	 * Represents an effect that sets the sequence's current phase to the previous phase.
	 *
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class GoToPreviousPhase(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.startPhase(player, sequenceKey, SequenceManager.getSequenceData(player, sequenceKey).get<IonRegistryKey<SequencePhase, SequencePhase>>("last_phase").get()) }
	}

	/**
	 * Represents an effect that clears the sequence's data.
	 *
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class ClearSequenceData(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.clearSequenceData(player) }
	}

	/**
	 * Represents an effect that stores a key-value pair in the sequence data store for a player.
	 *
	 * @param T The type of the value to be stored in the sequence data store.
	 * @param key The key used to identify the data to be stored.
	 * @param value The value to be stored in the sequence data store.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class SetSequenceData<T : Any>(val key: String, val value: T, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		@OptIn(InternalSerializationApi::class)
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			SequenceManager.getSequenceData(player, sequenceKey).set(key, value)
		}
	}

	/**
	 * Represents an effect that stores a key-value pair in the sequence data store for a player, with the value being
	 * a supplier. Use [SetSequenceData] to store a raw value.
	 *
	 * @param T The type of the value to be stored in the sequence data store.
	 * @param key The key used to identify the data to be stored.
	 * @param valueProvider The supplier that provides the value to be stored in the sequence data store.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class SuppliedSetSequenceData<T : Any>(val key: String, val valueProvider: Supplier<T>, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		@OptIn(InternalSerializationApi::class)
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			SequenceManager.getSequenceData(player, sequenceKey).set(key, valueProvider.get())
		}
	}

	/**
	 * Represents an effect that wraps another [SequencePhaseEffect] and executes it if data stored in the sequence data
	 * store for a player matches the specified condition. Uses the wrapped effect's timing.
	 *
	 * @param T The type of the data stored in the sequence data store.
	 * @param key The key used to identify the data in the sequence data store. This value will be checked against [condition].
	 * @param condition The condition that must be met for the effect to be executed.
	 * @param effect The [SequencePhaseEffect] to be wrapped.
	 */
	class DataConditionalEffect<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effect.playEffect(
			player,
			sequenceKey,
			context
		) }
	}

	/**
	 * Represents an effect that wraps multiple [SequencePhaseEffect] and executes them if data stored in the sequence data
	 * store for a player matches the specified condition. Plays the effect at the [timing] specified, regardless of the
	 * wrapped effects' original timing.
	 *
	 * @param T The type of the data stored in the sequence data store.
	 * @param key The key used to identify the data in the sequence data store. This value will be checked against [condition].
	 * @param condition The condition that must be met for the effect to be executed.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 * @param effects All of the [SequencePhaseEffect] to be wrapped.
	 */
	class DataConditionalEffects<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, timing: EffectTiming, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effects.forEach { it.playEffect(
			player,
			sequenceKey,
			context
		) } }
	}

	/**
	 * Represents an effect that wraps multiple [SequencePhaseEffect] and executes them if the player meets a specified
	 * [condition]. Plays the effect at the [timing] specified, regardless of the wrapped effect's original timing.
	 *
	 * @param condition The condition that must be met for the effect to be executed.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 * @param effects All of the [SequencePhaseEffect] to be wrapped.
	 */
	class PlayerConditionalEffects(val condition: (Player) -> Boolean, timing: EffectTiming, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (condition(player)) effects.forEach { it.playEffect(
			player,
			sequenceKey,
			context
		) } }
	}

	/**
	 * Represents an effect that wraps another [SequencePhaseEffect] and applies a delay before executing it.
	 *
	 * @param delay The delay in ticks before the effect is executed.
	 * @param effect The [SequencePhaseEffect] to be wrapped.
	 */
	class DelayEffect<T : Any>(val delay: Long, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { Tasks.syncDelay(delay) { effect.playEffect(player, sequenceKey, context) } }
	}

	/**
	 * Represents an effect that wraps another [SequencePhaseEffect] and applies a chance to execute it.
	 *
	 * @param effect The [SequencePhaseEffect] to be wrapped.
	 * @param chance The chance that the effect will be executed (as a percentage).
	 */
	class Chance(val effect: SequencePhaseEffect, val chance: Double) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (testRandom(chance)) effect.playEffect(player, sequenceKey, context) }
	}

	/**
	 * Represents an effect that wraps another [SequencePhaseEffect] and executes it on a tick interval.
	 *
	 * @param effect The [SequencePhaseEffect] to be wrapped.
	 * @param interval The interval in ticks at which the effect is executed.
	 */
	class OnTickInterval(val effect: SequencePhaseEffect, val interval: Int) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (MinecraftServer.getServer().tickCount % interval == 0) effect.playEffect(
			player,
			sequenceKey,
			context
		) }
	}

	/**
	 * Represents an effect that sends a message to a player.
	 *
	 * @param message The message to be sent.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class SendMessage(val message: Component, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.sendMessage(message) }
	}

	/**
	 * Represents an effect that stores a message to be sent later. The message is placed in a queue and will be sent
	 * [delayTicks] ticks after the effect is triggered, in the order it was queued.
	 *
	 * @param message The message to be sent.
	 * @param delayTicks The delay in ticks before the message is sent.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class SendDelayedMessage(val message: Component, val delayTicks: Long, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			val dataStore = SequenceManager.getSequenceData(player, sequenceKey)
			val scheduledTick = MinecraftServer.getServer().tickCount + delayTicks
			dataStore.queueDelayedMessage(scheduledTick, message)
		}
	}

	/**
	 * Represents an effect that sends a title to a player.
	 *
	 * @param title The title to be sent.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class SendTitle(val title: Title, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.showTitle(title) }
	}

	/**
	 * Represents an effect that plays a sound to a player.
	 *
	 * @param sound The sound to be played.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class PlaySetSound(val sound: Sound, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.playSound(sound) }
	}

	/**
	 * Represents an effect that plays a sound to a player.
	 *
	 * @param key The key of the sound to be played.
	 * @param volume The volume of the sound to be played.
	 * @param pitch The pitch of the sound to be played.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class PlaySound(val key: Key, val volume: FloatAmount, val pitch: FloatAmount, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.playSound(Sound.sound(key, Sound.Source.AMBIENT, volume.get(), pitch.get())) }
	}

	/**
	 * Represents an effect that displays a visual projectile.
	 *
	 * @param location The location of the projectile.
	 * @param direction The direction of the projectile.
	 * @param color The color of the projectile.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class PlayVisualProjectile(val location: Location, val direction: Vector, val color: Color, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		constructor(origin: Location, destination: Location, color: Color, playPhase: EffectTiming) : this(origin, destination.toVector().subtract(origin.toVector()), color, playPhase)

		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			VisualProjectile(location.clone().add(context.getOrigin().toLocation(location.world)), direction, 100.0, 10.0, color, 1.0f, 0)
		}
	}

	/**
	 * Represents an effect that displays a particle to a specific player.
	 *
	 * @param particle The particle to be displayed.
	 * @param location The location of the particle.
	 * @param extraParticles The number of particles to be displayed.
	 * @param dx The x-axis offset of the particle.
	 * @param dy The y-axis offset of the particle.
	 * @param dz The z-axis offset of the particle.
	 * @param options The options for the particle.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class PlayParticle(val particle: Particle, val location: Location, val extraParticles: Int, val dx: Double, val dy: Double, val dz: Double, val options: ParticleOptions, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			player.spawnParticle(particle, location.clone().add(context.getOrigin().toLocation(location.world)), extraParticles, dx, dy, dz, options)
		}
	}

	/**
	 * Represents an effect that highlights a block to a specific player.
	 *
	 * @param position The position of the block to be highlighted.
	 * @param duration The duration of the highlight in ticks.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class HighlightBlock(val position: Vec3i, val duration: Long, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			player.highlightBlock(position.plus(context.getOrigin()), duration)
		}
	}

	/**
	 * Represents an effect that displays a text display entity to a specific player. The entity's rendered position
	 * will be extrapolated from its world position and be rendered around the player.
	 *
	 * @param position The position (in the world) of the text display entity.
	 * @param text The text to be displayed.
	 * @param durationTicks The duration of the text display in ticks.
	 * @param scale The scale of the text.
	 * @param backgroundColor The background color of the text.
	 * @param defaultBackground Whether the background should be the default background color.
	 * @param seeThrough Whether the text should be visible through blocks.
	 * @param highlight Whether the text should be highlighted.
	 * @param positionOffset The offset of the text display entity from the player's perspective.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class DisplayText(
		val position: Vec3i,
		val text: Component,
		val durationTicks: Long,
		val scale: Float = 1.0f,
		val backgroundColor: Color = Color.fromARGB(0x00000000),
		val defaultBackground: Boolean = false,
		val seeThrough: Boolean = false,
		val highlight: Boolean = false,
		val positionOffset: Vector = Vector(0.0, 0.0, 0.0),
		timing: EffectTiming?,
	) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			val actualPosition = position.plus(context.getOrigin())

			val playerPosition = player.eyeLocation.toVector()
			val distance = actualPosition.toVector().distance(playerPosition)
			val direction = actualPosition.toVector().subtract(player.location.toVector()).normalize()

			// calculate position and offset
			val offset = direction.clone().normalize().multiply(min(distance, 10.0))
			val finalPosition = playerPosition.add(offset).toLocation(player.world).add(positionOffset)

			player.sendText(
				location = finalPosition,
				text = text,
				durationTicks = durationTicks + 1,
				scale = scale,
				backgroundColor = backgroundColor,
				defaultBackground = defaultBackground,
				seeThrough = seeThrough,
				highlight = highlight,
			)
		}
	}

	/**
	 * Represents an effect that displays a text display entity to a specific player, containing a distance value.
	 *
	 * @param position The position of the text display entity.
	 * @param durationTicks The duration of the text display in ticks.
	 * @param scale The scale of the text.
	 * @param backgroundColor The background color of the text.
	 * @param defaultBackground Whether the background should be the default background color.
	 * @param seeThrough Whether the text should be visible through blocks.
	 * @param highlight Whether the text should be highlighted.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class DisplayDistanceText(
		val position: Vec3i,
		val durationTicks: Long,
		val scale: Float = 1.0f,
		val backgroundColor: Color = Color.fromARGB(0x00000000),
		val defaultBackground: Boolean = false,
		val seeThrough: Boolean = false,
		val highlight: Boolean = false,
		timing: EffectTiming?
	) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			val distance = position.plus(context.getOrigin()).toVector().distance(player.eyeLocation.toVector())
			val text = Component.text("${distance.toInt()}m")

			DisplayText(
				position,
				text,
				durationTicks,
				scale,
				backgroundColor,
				defaultBackground,
				seeThrough,
				highlight,
				Vector(0.0, -1.5, 0.0),
				timing
			).playEffect(player, sequenceKey, context)
		}
	}

	/**
	 * Represents an effect that displays a text display entity attached to the player's perspective.
	 *
	 * @param distance The position of the text display from the player's eye location.
	 * @param text The text to be displayed.
	 * @param durationTicks The duration of the effect in ticks.
	 * @param scale The scale of the text display entity.
	 * @param backgroundColor The background color of the text display entity.
	 * @param defaultBackground Whether the text display entity should have a default background.
	 * @param seeThrough Whether the text display entity should be visible through blocks.
	 * @param highlight Whether the text display entity should be highlighted.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class DisplayHudText(
		val distance: Double,
		val text: Component,
		val durationTicks: Long,
		val scale: Float = 1.0f,
		val backgroundColor: Color = Color.fromARGB(0x00000000),
		val defaultBackground: Boolean = false,
		val seeThrough: Boolean = false,
		val highlight: Boolean = false,
		timing: EffectTiming?
	) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			val playerPosition = player.eyeLocation.toVector()
			val direction = player.location.direction

			// calculate position and offset
			val offset = direction.clone().normalize().multiply(distance)
			val finalPosition = playerPosition.add(offset).toLocation(player.world).add(Vector(0.0, 3.0, 0.0))

			player.sendText(
				location = finalPosition,
				text = text,
				durationTicks = durationTicks + 1,
				scale = scale,
				backgroundColor = backgroundColor,
				defaultBackground = defaultBackground,
				seeThrough = seeThrough,
				highlight = highlight,
			)
		}
	}

	/**
	 * Represents an effect that runs code when executed.
	 *
	 * @param block The code to be executed.
	 * @param timing The timing at which this effect should be executed (e.g., START, TICKED, or END).
	 */
	class RunCode(val block: (player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) -> Unit, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) = block.invoke(player, sequenceKey)
	}

	companion object {
		fun ifPreviousPhase(phase: IonRegistryKey<SequencePhase, SequencePhase>, timing: EffectTiming, vararg effects: SequencePhaseEffect): DataConditionalEffects<IonRegistryKey<SequencePhase, SequencePhase>> {
			return DataConditionalEffects("last_phase", { it.getOrNull() == phase }, timing, *effects)
		}

		fun ifContainsItem(itemPredicate: (ItemStack?) -> Boolean, vararg effects: SequencePhaseEffect): PlayerConditionalEffects {
			return PlayerConditionalEffects({ it.inventory.contents.any(itemPredicate) }, EffectTiming.TICKED, *effects)
		}
	}
}
