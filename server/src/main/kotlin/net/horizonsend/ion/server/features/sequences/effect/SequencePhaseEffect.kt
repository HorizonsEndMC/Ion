package net.horizonsend.ion.server.features.sequences.effect

import kotlinx.serialization.InternalSerializationApi
import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.common.utils.text.QUEST_OBJECTIVE_ICON
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.util.FloatAmount
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
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
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.Optional
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

abstract class SequencePhaseEffect(val timing: EffectTiming?) {
	abstract fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext)

	class EndSequence(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.endPhase(player, sequenceKey) }
	}

	class GoToPhase(val phase: IonRegistryKey<SequencePhase, SequencePhase>, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.startPhase(player, sequenceKey, phase) }
	}

	class GoToPreviousPhase(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.startPhase(player, sequenceKey, SequenceManager.getSequenceData(player, sequenceKey).get<IonRegistryKey<SequencePhase, SequencePhase>>("last_phase").get()) }
	}

	class ClearSequenceData(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { SequenceManager.clearSequenceData(player) }
	}

	class SetSequenceData<T : Any>(val key: String, val value: T, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		@OptIn(InternalSerializationApi::class)
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			SequenceManager.getSequenceData(player, sequenceKey).set(key, value)
		}
	}

	class SuppliedSetSequenceData<T : Any>(val key: String, val valueProvider: Supplier<T>, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		@OptIn(InternalSerializationApi::class)
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			SequenceManager.getSequenceData(player, sequenceKey).set(key, valueProvider.get())
		}
	}

	class DataConditionalEffect<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effect.playEffect(
			player,
			sequenceKey,
			context
		) }
	}

	class DataConditionalEffects<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, timing: EffectTiming, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effects.forEach { it.playEffect(
			player,
			sequenceKey,
			context
		) } }
	}

	class ConditionalEffects(val condition: (Player) -> Boolean, timing: EffectTiming, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (condition(player)) effects.forEach { it.playEffect(
			player,
			sequenceKey,
			context
		) } }
	}

	class DelayEffect<T : Any>(val delay: Long, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { Tasks.syncDelay(delay) { effect.playEffect(player, sequenceKey, context) } }
	}

	class Chance(val effect: SequencePhaseEffect, val chance: Double) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (testRandom(chance)) effect.playEffect(player, sequenceKey, context) }
	}

	class OnTickInterval(val effect: SequencePhaseEffect, val interval: Int) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { if (MinecraftServer.getServer().tickCount % interval == 0) effect.playEffect(
			player,
			sequenceKey,
			context
		) }
	}

	class SendMessage(val message: Component, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.sendMessage(message) }
	}


	class SendDelayedMessage(val message: Component, val delayTicks: Long, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { Tasks.asyncDelay(delayTicks) { player.sendMessage(message) } }
	}

	class SendTitle(val title: Title, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.showTitle(title) }
	}

	class PlaySetSound(val sound: Sound, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.playSound(sound) }
	}

	class PlaySound(val key: Key, val volume: FloatAmount, val pitch: FloatAmount, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) { player.playSound(Sound.sound(key, Sound.Source.AMBIENT, volume.get(), pitch.get())) }
	}

	class PlayVisualProjectile(val location: Location, val direction: Vector, val color: Color, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		constructor(origin: Location, destination: Location, color: Color, playPhase: EffectTiming) : this(origin, destination.toVector().subtract(origin.toVector()), color, playPhase)

		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			VisualProjectile(location.clone().add(context.getOrigin().toLocation(location.world)), direction, 100.0, 10.0, color, 1.0f, 0)
		}
	}

	class PlayParticle(val particle: Particle, val location: Location, val extraParticles: Int, val dx: Double, val dy: Double, val dz: Double, val options: ParticleOptions, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			player.spawnParticle(particle, location.clone().add(context.getOrigin().toLocation(location.world)), extraParticles, dx, dy, dz, options)
		}
	}

	class HighlightBlock(val position: Vec3i, val duration: Long, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) {
			player.highlightBlock(position.plus(context.getOrigin()), duration)
		}
	}

	class DisplayHudIcon(
		val position: Vec3i,
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
			val actualPosition = position.plus(context.getOrigin())

			val playerPosition = player.eyeLocation.toVector()
			val distance = actualPosition.toVector().distance(playerPosition)
			val direction = actualPosition.toVector().subtract(player.location.toVector()).normalize()

			// calculate position and offset
			val offset = direction.clone().normalize().multiply(min(distance, 10.0))
			val finalPosition = playerPosition.add(offset).toLocation(player.world)

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
			val actualPosition = position.plus(context.getOrigin())

			val playerPosition = player.eyeLocation.toVector()
			val distance = actualPosition.toVector().distance(playerPosition)
			val direction = actualPosition.toVector().subtract(player.location.toVector()).normalize()

			val text = Component.text("${distance.toInt()}m")

			// calculate position and offset
			val offset = direction.clone().normalize().multiply(min(distance, 10.0))
			val finalPosition = playerPosition.add(offset).toLocation(player.world).add(Vector(0.0, -1.5, 0.0))

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

	class RunCode(val block: (player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) -> Unit, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>, context: SequenceContext) = block.invoke(player, sequenceKey)
	}

	companion object {
		fun ifPreviousPhase(phase: IonRegistryKey<SequencePhase, SequencePhase>, timing: EffectTiming, vararg effects: SequencePhaseEffect): DataConditionalEffects<IonRegistryKey<SequencePhase, SequencePhase>> {
			return DataConditionalEffects("last_phase", { it.getOrNull() == phase }, timing, *effects)
		}

		fun ifContainsItem(itemPredicate: (ItemStack?) -> Boolean, vararg effects: SequencePhaseEffect): ConditionalEffects {
			return ConditionalEffects({ it.inventory.contents.any(itemPredicate) }, EffectTiming.TICKED, *effects)
		}
	}
}
