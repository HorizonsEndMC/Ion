package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.StarshipType.BATTLECRUISER
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.PlanetTeleportCooldown
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

object StarshipControl : IonServerComponent() {
	val CONTROLLER_TYPE = Material.CLOCK

	override fun onEnable() {
		Tasks.syncRepeat(1, 1, ::moveAIShips)
	}

	private fun moveAIShips() {
		val aIShips = ActiveStarships.all().filter { it.controller is AIController }

		for (starship in aIShips) {
			val controller = starship.controller as? AIController ?: continue
			val sneakMovements = controller.sneakMovements

			if (Hyperspace.isWarmingUp(starship)) return
			if (!controller.isSneakFlying()) return

			val now = System.currentTimeMillis()
			if (now - starship.lastManualMove < starship.manualMoveCooldownMillis) return
			starship.lastManualMove = now

			val maxAccel = starship.balancing.maxSneakFlyAccel
			val accelDistance = starship.balancing.sneakFlyAccelDistance

			val yawRadians = Math.toRadians(controller.yaw.toDouble())
			val pitchRadians = Math.toRadians(controller.pitch.toDouble())

			val distance = max(min(maxAccel, sneakMovements / min(1, accelDistance)), 1)

			val vertical = abs(pitchRadians) >= PI * 5 / 12 // 75 degrees

			val dx = if (vertical) 0 else sin(-yawRadians).roundToInt() * distance
			val dy = sin(-pitchRadians).roundToInt() * distance
			val dz = if (vertical) 0 else cos(yawRadians).roundToInt() * distance

			if (locationCheck(starship, dx, dy, dz)) return

			TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
		}
	}

	fun locationCheck(starship: ActiveControlledStarship, dx: Int, dy: Int, dz: Int): Boolean {
		val world = starship.world
		val newCenter = starship.centerOfMass.toLocation(world).add(dx.d(), dy.d(), dz.d())

		val planet = Space.getAllPlanets().asSequence()
			.filter { it.spaceWorld == world }
			.filter {
				it.location.toLocation(world).distanceSquared(newCenter) < starship.getEntryRange(it).toDouble().pow(2)
			}
			.firstOrNull()
			?: return false

		// Don't allow battlecruisers to enter planets
		if (starship.type == BATTLECRUISER) return false

		// Don't allow players that have recently entered planets to re-enter again
		val controller = starship.controller
		if (controller is PlayerController) {
			if (PlanetTeleportCooldown.cannotEnterPlanets(controller.player)) return false
			// Restrict planet entry if combat tagged
			PlanetTeleportCooldown.addEnterPlanetRestriction(controller.player)
		}

		val border = planet.planetWorld?.worldBorder
			?.takeIf { it.size < 60_000_000 } // don't use if it's the default, giant border
		val halfLength = if (border == null) 2500.0 else border.size / 2.0
		val centerX = border?.center?.x ?: halfLength
		val centerZ = border?.center?.z ?: halfLength

		//val distance = (halfLength - 250) * max(0.15, newCenter.y / starship.world.maxHeight)
		// removed the y-height factor in determining the entering planet range. this should now result in
		// planet enter positions being situated in a circle with a radius of 25% of the world border length.
		val distance = (halfLength - 250) * 0.5
		val offset = newCenter.toVector()
			.subtract(planet.location.toVector())
			.normalize().multiply(distance)

		val x = centerX + offset.x
		val y = 250.0 - (starship.max.y - starship.min.y)
		val z = centerZ + offset.z
		val target = Location(planet.planetWorld, x, y, z).toBlockLocation()

		starship.sendMessage(
			text()
				.color(NamedTextColor.GRAY)
				.decorate(TextDecoration.ITALIC)
				.append(text("Entering "))
				.append(text(planet.name, NamedTextColor.BLUE))
				.append(text("..."))
		)
		starship.setIsInterdicting(false)

		StarshipTeleportation.teleportStarship(starship, target)
		return true
	}
}
