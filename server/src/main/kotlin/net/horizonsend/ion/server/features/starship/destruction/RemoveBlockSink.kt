package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.server.features.nations.utils.playSoundInRadius
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BattlecruiserReactorSubsystem
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RemoveBlockSink(starship: ActiveStarship, val checkRemove: (Block) -> Boolean) : StandardSinkProvider(starship) {
	override fun setup() {
		starship.iterateBlocks { x, y, z ->
			if (checkRemove(starship.world.getBlockAt(x, y, z))) {
				starship.world.setType(x, y, z, Material.AIR)
			}
		}

		starship.balancing.sounds.explode?.let {
			playSoundInRadius(starship.centerOfMass.toLocation(starship.world), 7_500.0, it.sound)
		}

		super.setup()
	}

	private var particleRadius = 0.0

	private val reactor = starship.subsystems.filterIsInstance<BattlecruiserReactorSubsystem>().firstOrNull()
	private val center = (reactor?.pos ?: starship.centerOfMass) .toLocation(starship.world)
	private val world = starship.world

	override fun tick() {
		super.tick()

		if (particleRadius >= MAX_PARTICLE_RADIUS) return

		particleRadius += 3.5

		val number = (2.0 * PI * particleRadius).toInt()

		for (count in 0..number) {
			// Get the fraction around the circle
			val degrees = 360.0 - (360.0 * (count.toDouble() / number.toDouble()))
			val radians = degrees * (PI / 180.0)
			val xOffset = cos(radians) * particleRadius
			val zOffset = sin(radians) * particleRadius

			val newLoc = center.clone().add(xOffset, 0.0, zOffset)

			world.spawnParticle(Particle.SONIC_BOOM, newLoc.x, newLoc.y, newLoc.z, 1, 0.0, 0.0, 0.0, 0.0, null,true)
		}
	}

	companion object {
		private const val MAX_PARTICLE_RADIUS = 100.0
	}
}
