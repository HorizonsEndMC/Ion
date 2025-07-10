package net.horizonsend.ion.server.features.sequences.effect

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.VisualProjectile
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minecraft.core.particles.ParticleOptions
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector

abstract class SequencePhaseEffect(val playPhases: List<EffectTiming>) {
	abstract fun playEffect(player: Player)

	class EndSequence(playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { SequenceManager.endPhase(player) }
	}

	class Chance(val effect: SequencePhaseEffect, val chance: Double) : SequencePhaseEffect(effect.playPhases) {
		override fun playEffect(player: Player) { if (testRandom(chance)) effect.playEffect(player) }
	}

	class SendMessage(val message: Component, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.sendMessage(message) }
	}

	class SendTitle(val title: Title, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.showTitle(title) }
	}

	class PlaySound(val sound: Sound, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.playSound(sound) }
	}

	class PlayVisualProjectile(val origin: Location, val direction: Vector, val color: Color, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		constructor(origin: Location, destination: Location, color: Color, playPhase: List<EffectTiming>) : this(origin, destination.toVector().subtract(origin.toVector()), color, playPhase)

		override fun playEffect(player: Player) { VisualProjectile(origin, direction, 100.0, 10.0, color, 1.0f, 0) }
	}

	class PlayParticle(val particle: Particle, val location: Location, val extraParticles: Int, val dx: Double, val dy: Double, val dz: Double, val options: ParticleOptions, playPhases: List<EffectTiming>) : SequencePhaseEffect(playPhases) {
		override fun playEffect(player: Player) { player.spawnParticle(particle, location, extraParticles, dx, dy, dz, options) }
	}
}
