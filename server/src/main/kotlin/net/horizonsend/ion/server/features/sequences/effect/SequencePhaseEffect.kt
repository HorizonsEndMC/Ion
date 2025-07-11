package net.horizonsend.ion.server.features.sequences.effect

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.configuration.util.FloatAmount
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.SequencePhaseKeys
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
import org.bukkit.util.Vector
import java.util.Optional

abstract class SequencePhaseEffect(val playPhases: List<EffectTiming>) {
	abstract fun playEffect(player: Player)

	class EndSequence(playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { SequenceManager.endPhase(player) }
	}

	class GoToPhase(val phase: SequencePhaseKeys.SequencePhaseKey, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { SequenceManager.startPhase(player, phase.getValue()) }
	}

	class ClearSequenceData(playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { SequenceManager.clearSequenceData(player) }
	}

	class SetSequenceData(val key: String, val value: Any, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { SequenceManager.getSequenceData(player)[key] = value }
	}

	class DataConditionalEffect<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player) { if (condition(SequenceManager.getSequenceData(player).get<T>(key))) effect.playEffect(player) }
	}

	class DataConditionalEffects<T : Any>(val key: String, val condition: (Optional<T>) -> Boolean, playPhases: List<EffectTiming>, vararg val effects: SequencePhaseEffect) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { if (condition(SequenceManager.getSequenceData(player).get<T>(key))) effects.forEach { it.playEffect(player) } }
	}

	class DelayEffect<T : Any>(val delay: Long, val effect: SequencePhaseEffect) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player) { Tasks.syncDelay(delay) { effect.playEffect(player) } }
	}

	class Chance(val effect: SequencePhaseEffect, val chance: Double) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player) { if (testRandom(chance)) effect.playEffect(player) }
	}

	class OnTickInterval(val effect: SequencePhaseEffect, val interval: Int) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player) { if (MinecraftServer.getServer().tickCount % interval == 0) effect.playEffect(player) }
	}

	class SendMessage(val message: Component, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.sendMessage(message) }
	}

	class SendTitle(val title: Title, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.showTitle(title) }
	}

	class PlaySetSound(val sound: Sound, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.playSound(sound) }
	}

	class PlaySound(val key: Key, val volume: FloatAmount, val pitch: FloatAmount, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.playSound(Sound.sound(key, Sound.Source.AMBIENT, volume.get(), pitch.get())) }
	}

	class PlayVisualProjectile(val origin: Location, val direction: Vector, val color: Color, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		constructor(origin: Location, destination: Location, color: Color, playPhase: List<EffectTiming>) : this(origin, destination.toVector().subtract(origin.toVector()), color, playPhase)

		override fun playEffect(player: Player) { VisualProjectile(origin, direction, 100.0, 10.0, color, 1.0f, 0) }
	}

	class PlayParticle(val particle: Particle, val location: Location, val extraParticles: Int, val dx: Double, val dy: Double, val dz: Double, val options: ParticleOptions, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.spawnParticle(particle, location, extraParticles, dx, dy, dz, options) }
	}

	class HighlightBlock(val position: Vec3i, val duration: Long, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.highlightBlock(position, duration) }
	}
}
