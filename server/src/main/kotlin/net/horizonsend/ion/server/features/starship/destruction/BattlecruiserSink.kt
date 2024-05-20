package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.server.features.customblocks.CustomBlocks
import net.horizonsend.ion.server.features.customblocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.SupercapReactorSubsystem
import org.bukkit.Material
import org.bukkit.Particle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class BattlecruiserSink(starship: ActiveStarship) : StandardSinkProvider(starship) {
	val standard = StandardSinkProvider(starship)

	override fun setup() {
		starship.iterateBlocks { x, y, z ->
			if (starship.world.getBlockAt(x, y, z).customBlock == CustomBlocks.REACTOR_CORE) {
				starship.world.setType(x, y, z, Material.AIR)
			}
		}

		super.setup()
	}

	private var particleRadius = 0.0

	private val reactor = starship.subsystems.filterIsInstance<SupercapReactorSubsystem>().firstOrNull()
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

			world.spawnParticle(Particle.SONIC_BOOM, newLoc, 1)
		}
	}

	companion object {
		private const val MAX_PARTICLE_RADIUS = 100.0
	}
}
