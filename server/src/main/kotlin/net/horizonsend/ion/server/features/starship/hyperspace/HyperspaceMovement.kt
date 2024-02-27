package net.horizonsend.ion.server.features.starship.hyperspace

import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.common.extensions.informationAction
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.starship.PilotedStarships.isPiloted
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.distance
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Vibration
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class HyperspaceMovement(
	val ship: ActiveStarship,
	val speed: Int,
	val originWorld: World,
	val dest: Location
) : BukkitRunnable() {
	var x = ship.centerOfMass.x.toDouble()
	val originX = x
	var z = ship.centerOfMass.z.toDouble()
	val originZ = z

	private val direction = dest.toVector().subtract(ship.centerOfMass.toVector()).normalize()
	private val totalDistance = remainingDistance()
	private var travelled = 0.0

	init {
		runTaskTimer(IonServer, 2, 2)
	}

	private fun remainingDistance(): Double {
		return if (originWorld == dest.world) { distance(x, 0.0, z, dest.x, 0.0, dest.z) } else {
			Hyperspace.INTER_SYSTEM_DISTANCE - travelled
		}
	}

	init {
		(ship as? ActiveControlledStarship)?.playerPilot?.rewardAchievement(Achievement.USE_HYPERSPACE)
	}

	override fun run() {
		if (!isPiloted(ship as ActiveControlledStarship)) {
			cancel()
			return
		}

		x += direction.x * speed
		z += direction.z * speed
		travelled += speed

		playEffect()

		// Don't check for mass shadows if jumping to another world
		if (originWorld == dest.world) {
			val shadow: MassShadows.MassShadowInfo? = MassShadows.find(dest.world, x, z)
			if (shadow != null) {
				ship.onlinePassengers.forEach { player ->
					player.alertAction(
						"Ship caught by a mass shadow! Mass Shadow: ${shadow.description.plainText()} at ${shadow.x}, ${shadow.z} " +
								"with radius ${shadow.radius} (${shadow.distance} blocks away)"
					)
				}

				cancel()
				return
			}
		}

		if (travelled < totalDistance) {
			val percent = (travelled / totalDistance * 100).roundToInt()
			ship.onlinePassengers.forEach { player ->
				player.informationAction("Hyperspace Progress: ${travelled.roundToInt()}/${totalDistance.roundToInt()} ($percent%)")
			}
			return
		}

		ship.onlinePassengers.forEach { player -> player.informationAction("Jump complete") }
		Hyperspace.completeJumpMovement(this)
	}

	private val count = sqrt(ship.initialBlockCount.toDouble()).roundToInt()
	private val maxSide = maxOf(ship.max.x - ship.min.x, ship.max.y - ship.min.y, ship.max.z - ship.min.z).toDouble()

	private fun playEffect() {
		val origin = ship.centerOfMass.toLocation(ship.world)
		val vector = direction.clone().normalize().multiply(maxSide * 0.75)
		val forwardCenter = origin.clone().add(vector.clone())
		val rearCenter = origin.clone().subtract(vector.clone())
		val orthogonal = direction.clone().normalize().getCrossProduct(BlockFace.UP.direction).multiply(maxSide * 0.5)

		for (i in 0..count) {
			val radians = Random.nextDouble(0.0, 2.0) * PI
			val around = orthogonal.clone().rotateAroundAxis(vector, radians)

			val particleOrigin = forwardCenter.add(around)
			val particleDestination = rearCenter.add(around.clone().multiply(1.5))

			ship.world.spawnParticle(
				Particle.VIBRATION,
				particleOrigin,
				2,
				Vibration(
					Vibration.Destination.BlockDestination(particleDestination),
					75
				)
			)
		}
	}

	/** If the ship is still active, this will pull it out */
	override fun cancel() {
		super.cancel()

		if (!Hyperspace.isMoving(ship)) {
			return
		}

		Hyperspace.cancelJumpMovement(this)
	}
}
