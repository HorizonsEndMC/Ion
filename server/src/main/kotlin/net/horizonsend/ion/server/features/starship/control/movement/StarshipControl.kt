package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.StarshipType.BATTLECRUISER
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.PlanetTeleportCooldown
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import kotlin.math.max
import kotlin.math.pow

object StarshipControl : IonServerComponent() {
	val CONTROLLER_TYPE = Material.CLOCK

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

		StarshipTeleportation.teleportStarship(starship, target)
		return true
	}
}
