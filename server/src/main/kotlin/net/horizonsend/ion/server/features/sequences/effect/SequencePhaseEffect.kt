package net.horizonsend.ion.server.features.sequences.effect

import kotlinx.serialization.InternalSerializationApi
import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.configuration.util.FloatAmount
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys.SequencePhaseKey
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

abstract class SequencePhaseEffect(val playPhases: List<EffectTiming>) {
	abstract fun playEffect(player: Player, sequenceKey: String)

	class EndSequence(playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { SequenceManager.endPhase(player, sequenceKey) }
	}

	class GoToPhase(val phase: SequencePhaseKey, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { SequenceManager.startPhase(player, sequenceKey, phase) }
	}

	class GoToPreviousPhase(playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { SequenceManager.startPhase(player, sequenceKey, SequenceManager.getSequenceData(player, sequenceKey).get<SequencePhaseKey>("last_phase").get()) }
	}

	class ClearSequenceData(playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { SequenceManager.clearSequenceData(player) }
	}

	class SetSequenceData<T : Any>(val key: String, val value: T, val valueClass: KClass<T>, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		@OptIn(InternalSerializationApi::class)
		override fun playEffect(player: Player, sequenceKey: String) {
			SequenceManager.getSequenceData(player, sequenceKey).set<T>(key, value)
		}
	}

	class DataConditionalEffect<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effect.playEffect(player, sequenceKey) }
	}

	class DataConditionalEffects<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, playPhases: List<EffectTiming>, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { if (condition(SequenceManager.getSequenceData(player, sequenceKey).get<T>(key))) effects.forEach { it.playEffect(player, sequenceKey) } }
	}

	class ConditionalEffects(val condition: (Player) -> Boolean, playPhases: List<EffectTiming>, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { if (condition(player)) effects.forEach { it.playEffect(player, sequenceKey) } }
	}

	class DelayEffect<T : Any>(val delay: Long, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { Tasks.syncDelay(delay) { effect.playEffect(player, sequenceKey) } }
	}

	class Chance(val effect: SequencePhaseEffect, val chance: Double) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { if (testRandom(chance)) effect.playEffect(player, sequenceKey) }
	}

	class OnTickInterval(val effect: SequencePhaseEffect, val interval: Int) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { if (MinecraftServer.getServer().tickCount % interval == 0) effect.playEffect(player, sequenceKey) }
	}

	class SendMessage(val message: Component, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { player.sendMessage(message) }
	}

	class SendTitle(val title: Title, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { player.showTitle(title) }
	}

	class PlaySetSound(val sound: Sound, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { player.playSound(sound) }
	}

	class PlaySound(val key: Key, val volume: FloatAmount, val pitch: FloatAmount, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { player.playSound(Sound.sound(key, Sound.Source.AMBIENT, volume.get(), pitch.get())) }
	}

	class PlayVisualProjectile(val origin: Location, val direction: Vector, val color: Color, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		constructor(origin: Location, destination: Location, color: Color, playPhase: List<EffectTiming>) : this(origin, destination.toVector().subtract(origin.toVector()), color, playPhase)

		override fun playEffect(player: Player, sequenceKey: String) { VisualProjectile(origin, direction, 100.0, 10.0, color, 1.0f, 0) }
	}

	class PlayParticle(val particle: Particle, val location: Location, val extraParticles: Int, val dx: Double, val dy: Double, val dz: Double, val options: ParticleOptions, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { player.spawnParticle(particle, location, extraParticles, dx, dy, dz, options) }
	}

	class HighlightBlock(val position: Vec3i, val duration: Long, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player, sequenceKey: String) { player.highlightBlock(position, duration) }
	}

	companion object {
		fun ifPreviousPhase(phase: SequencePhaseKey, playPhases: List<EffectTiming>, vararg effects: SequencePhaseEffect): DataConditionalEffects<SequencePhaseKey> {
			return DataConditionalEffects("last_phase", { it.getOrNull() == phase }, playPhases, *effects)
		}

		fun ifContainsItem(itemPredicate: (ItemStack?) -> Boolean, vararg effects: SequencePhaseEffect): ConditionalEffects {
			return ConditionalEffects({ it.inventory.contents.any(itemPredicate) }, listOf(EffectTiming.TICKED), *effects)
		}
	}
}
