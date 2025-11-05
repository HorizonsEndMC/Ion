package net.horizonsend.ion.server.features.sequences.effect

import kotlinx.serialization.InternalSerializationApi
import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.configuration.util.FloatAmount
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.sequences.Sequence
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
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

abstract class SequencePhaseEffect(val timing: EffectTiming?) {
	abstract fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>)

	class EndSequence(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { SequenceManager.endPhase(player, sequenceKey) }
	}

	class GoToPhase(val phase: IonRegistryKey<SequencePhase, SequencePhase>, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { SequenceManager.startPhase(player, sequenceKey, phase) }
	}

	class GoToPreviousPhase(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { SequenceManager.startPhase(player, sequenceKey, SequenceManager.getSequenceData(player, sequenceKey).get<IonRegistryKey<SequencePhase, SequencePhase>>("last_phase").get()) }
	}

	class ClearSequenceData(timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { SequenceManager.clearSequenceData(player) }
	}

	class SetSequenceData<T : Any>(val key: String, val value: T, val valueClass: KClass<T>, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		@OptIn(InternalSerializationApi::class)
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) {
			SequenceManager.getSequenceData(player, sequenceKey).set<T>(key, value)
		}
	}

	class DataConditionalEffect<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effect.playEffect(player, sequenceKey) }
	}

	class DataConditionalEffects<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, timing: EffectTiming, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effects.forEach { it.playEffect(player, sequenceKey) } }
	}

	class ConditionalEffects(val condition: (Player) -> Boolean, timing: EffectTiming, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { if (condition(player)) effects.forEach { it.playEffect(player, sequenceKey) } }
	}

	class DelayEffect<T : Any>(val delay: Long, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { Tasks.syncDelay(delay) { effect.playEffect(player, sequenceKey) } }
	}

	class Chance(val effect: SequencePhaseEffect, val chance: Double) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { if (testRandom(chance)) effect.playEffect(player, sequenceKey) }
	}

	class OnTickInterval(val effect: SequencePhaseEffect, val interval: Int) : SequencePhaseEffect(effect.timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { if (MinecraftServer.getServer().tickCount % interval == 0) effect.playEffect(player, sequenceKey) }
	}

	class SendMessage(val message: Component, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { player.sendMessage(message) }
	}

	class SendTitle(val title: Title, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { player.showTitle(title) }
	}

	class PlaySetSound(val sound: Sound, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { player.playSound(sound) }
	}

	class PlaySound(val key: Key, val volume: FloatAmount, val pitch: FloatAmount, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { player.playSound(Sound.sound(key, Sound.Source.AMBIENT, volume.get(), pitch.get())) }
	}

	class PlayVisualProjectile(val origin: Location, val direction: Vector, val color: Color, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		constructor(origin: Location, destination: Location, color: Color, playPhase: EffectTiming) : this(origin, destination.toVector().subtract(origin.toVector()), color, playPhase)

		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { VisualProjectile(origin, direction, 100.0, 10.0, color, 1.0f, 0) }
	}

	class PlayParticle(val particle: Particle, val location: Location, val extraParticles: Int, val dx: Double, val dy: Double, val dz: Double, val options: ParticleOptions, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { player.spawnParticle(particle, location, extraParticles, dx, dy, dz, options) }
	}

	class HighlightBlock(val position: Vec3i, val duration: Long, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) { player.highlightBlock(position, duration) }
	}

	class RunCode(val block: (player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) -> Unit, timing: EffectTiming?) : SequencePhaseEffect(timing) {
		override fun playEffect(player: Player, sequenceKey: IonRegistryKey<Sequence, Sequence>) = block.invoke(player, sequenceKey)
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
