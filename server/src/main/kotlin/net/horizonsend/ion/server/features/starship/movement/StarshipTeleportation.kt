package net.horizonsend.ion.server.features.starship.movement

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Location
import org.bukkit.World
import java.util.concurrent.CompletableFuture

object StarshipTeleportation {
	fun teleportStarship(starship: ActiveStarship, destination: Location, callback: () -> Unit = {}): CompletableFuture<Boolean> {
		val origin = starship.centerOfMass
		val (x, y, z) = Vec3i(destination)
		val dx = x - origin.x
		val dy = y - origin.y
		val dz = z - origin.z

		if (starship is ActiveControlledStarship) {
			StarshipCruising.forceStopCruising(starship)
			starship.setDirectControlEnabled(false)
		}

		starship.isTeleporting = true

		val newWorld = if (destination.world != starship.world) destination.world else null

		return tryTeleport(starship, dx, dy, dz, newWorld).whenComplete { ok, ex ->
			starship.isTeleporting = false
			callback()
		}
	}

	private fun tryTeleport(
		starship: ActiveStarship,
		previousDX: Int,
		dy: Int,
		previousDZ: Int,
		newWorld: World?,
		previousTries: Int = 0,
		previousAdjustX: Int? = null,
		previousAdjustZ: Int? = null
	): CompletableFuture<Boolean> {
		if (previousTries >= 16) {
			starship.onlinePassengers.forEach { passenger ->
				passenger.userError("Failed to teleport, too many failed attempts")
			}

			return CompletableFuture.completedFuture(false)
		}

		val world = newWorld ?: starship.world

		if (wouldBeOutOfWorldBorder(starship, world, previousDX, previousDZ)) {
			starship.onlinePassengers.forEach { passenger ->
				passenger.userError("Failed to teleport, would be out of border")
			}

			return CompletableFuture.completedFuture(false)
		}

		var adjustX = previousAdjustX ?: randomOffset()
		var adjustZ = previousAdjustZ ?: randomOffset()

		while (wouldBeOutOfWorldBorder(starship, world, previousDX + adjustX, previousDZ + adjustZ)) {
			adjustX = previousAdjustX ?: randomOffset()
			adjustZ = previousAdjustZ ?: randomOffset()
		}

		val dx = previousDX + adjustX
		val dz = previousDZ + adjustZ

		return TranslateMovement
			.loadChunksAndMove(starship, dx, dy, dz, newWorld)
			.thenComposeAsync { success ->
				if (success) {
					return@thenComposeAsync CompletableFuture.completedFuture(true)
				}

				starship.information("Adjusting position...")


				val tries = previousTries + 1
				return@thenComposeAsync tryTeleport(starship, dx, dy, dz, newWorld, tries, adjustX, adjustZ)
			}
	}

	private fun wouldBeOutOfWorldBorder(starship: ActiveStarship, world: World, dx: Int, dz: Int): Boolean {
		return !world.worldBorder.isInside(starship.min.toLocation(world).add(dx.d(), 0.0, dz.d())) ||
			!world.worldBorder.isInside(starship.max.toLocation(world).add(dx.d(), 0.0, dz.d()))
	}

	private fun randomOffset() = listOf(-32, 32).random()
}
