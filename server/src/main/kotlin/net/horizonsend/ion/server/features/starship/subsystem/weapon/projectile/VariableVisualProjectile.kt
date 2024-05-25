package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.AntiAirCannonBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.damager.noOpDamager
import org.bukkit.Location
import org.bukkit.util.Vector

class VariableVisualProjectile(
	var loc: Location,
	var dir: Vector,
	val range: Double,
	val speed: Double,
	val drawParticle: (Location, Double) -> Unit
) : Projectile(null, noOpDamager) {
	override val balancing: StarshipWeapons.ProjectileBalancing = AntiAirCannonBalancing(
		range = 0.0,
		speed = 0.0,
		areaShieldDamageMultiplier = 0.0,
		starshipShieldDamageMultiplier = 0.0,
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
		drawParticle(loc, travel)
	}
}
