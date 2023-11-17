package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.AntiAirCannonBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.damager.noOpDamager
import net.horizonsend.ion.server.miscellaneous.utils.alongVector
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

class VisualProjectile(
	var loc: Location,
	var dir: Vector,
	val range: Double,
	val speed: Double,
	val color: Color,
	val particleThickness: Float,
	val extraParticles: Int
) : Projectile(null, noOpDamager) {
	override val balancing: StarshipWeapons.ProjectileBalancing = AntiAirCannonBalancing(
		range = 0.0,
		speed = 0.0,
		shieldDamageMultiplier = 0,
		particleThickness = 0.0,
		explosionPower = 0.0f,
		volume = 0,
		pitch = 0.0f,
		soundName = "",
	)

	private var distance: Double = 0.0
	private var firedAtNanos: Long = -1
	private var lastTick: Long = -1
	private var delta: Double = 0.0

	override fun fire() {
		firedAtNanos = System.nanoTime()
		lastTick = firedAtNanos

		super.fire()
	}

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0

		val predictedNewLoc = loc.clone().add(dir.clone().multiply(delta * speed))
		val travel = loc.distance(predictedNewLoc)

		moveVisually(travel)

		loc = predictedNewLoc

		distance += travel

		if (distance >= range) {
			return
		}

		lastTick = System.nanoTime()
		reschedule()
	}

	private fun moveVisually(travel: Double) {
		val particle = Particle.REDSTONE
		val dustOptions = Particle.DustOptions(color, particleThickness * 4f)

		loc.alongVector(dir, 1 + extraParticles).forEach {
			loc.world.spawnParticle(particle, it, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		}
	}
}
