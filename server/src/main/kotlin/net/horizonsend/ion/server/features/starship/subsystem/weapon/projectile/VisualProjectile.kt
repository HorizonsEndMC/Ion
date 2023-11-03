package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.Colors
import net.horizonsend.ion.server.features.starship.damager.noOpDamager
import net.horizonsend.ion.server.miscellaneous.utils.bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector

abstract class VisualProjectile(
	var loc: Location,
	var dir: Vector,
	val range: Double,
	val speed: Double,
	val color: Colors.Color,
	val particleThickness: Float
) : Projectile(null, noOpDamager) {
	abstract val thickness: Double

	private var distance: Double = 0.0
	private var firedAtNanos: Long = -1
	private var lastTick: Long = -1
	protected var delta: Double = 0.0

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
		for (i in 0 until travel.toInt()) {
			val x = loc.x + dir.x * i
			val y = loc.y + dir.y * i
			val z = loc.z + dir.z * i
			val force = i % 3 == 0

			val particle = Particle.REDSTONE
			val dustOptions = Particle.DustOptions(color.bukkit(), particleThickness * 4f)
			loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
		}
	}
}
