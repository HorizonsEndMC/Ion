package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.PI

abstract class AOEWave(starship: Starship, shooter: Damager, val firePos: Location) : Projectile(starship, shooter) {
	protected abstract val speed: Double
	protected abstract val separation: Double
	protected abstract val range: Double

	protected var lastTick: Long = -1
	protected var delta: Double = 0.0
	protected var distance: Double = 0.0
	protected var firedAtNanos: Long = -1

	private val circumference get() = 2.0 * PI * distance

	override fun fire() {
		firedAtNanos = System.nanoTime()
		lastTick = firedAtNanos

		super.fire()
	}

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds

		iterateCircumference()

		distance += (speed * delta)

		if (distance >= range) {
			return
		}

		lastTick = System.nanoTime()
		reschedule()
	}

	private fun iterateCircumference() {
		val vec = Vector(0.0, 0.0, distance)

		// Get the degree separation by getting the number of distance separations in the circumference, and converting to degrees
		val numberPoints = circumference / separation
		val separationDegree = 360.0 / numberPoints

		for (n in 0..numberPoints.toInt()) {
			val degrees = n * separationDegree
			val adjusted = vec.clone().rotateAroundY(Math.toRadians(degrees))

			handleCircumferencePosition(firePos.clone().add(adjusted))
		}
	}

	abstract fun handleCircumferencePosition(position: Location)
}
