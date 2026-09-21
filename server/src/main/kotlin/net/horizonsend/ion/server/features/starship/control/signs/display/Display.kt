package net.horizonsend.ion.server.features.starship.control.signs.display

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayButtonFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayFeature
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipReleaseEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.util.Vector
import org.joml.Vector3d

open class Display(
	val ship: Starship,
	var location: Location,
	var dir: Vector,
	val sizeX: Double,
	val sizeY: Double,
	val offset: Vector3d
) {
	val shiftPerLayer = .05
	var initialized: Boolean = false

	//Map features that are common to all the map states
	val commonFeatures: MutableList<DisplayFeature> = mutableListOf()
	val commonButtons = mutableListOf<DisplayButtonFeature>()

	//buttons pertaining to the current state
	val dynamicFeatures = mutableListOf<DisplayFeature>()

	open fun init(){
		if (initialized) return
		initialized = true
	}

	open fun tick(){
		if (ship.isTeleporting) return
		if (!initialized) init()

		//Tick & Show players the entities
		dynamicFeatures.toList().forEach { state ->
			state.tick()
			//Hide all the entities from players not in the ship. Showing only players of the ship
			for (entity in state.entities) {
				for (player in ship.onlinePassengers) {
					entity.isVisibleByDefault = false
					player.showEntity(IonServer, entity)
				}
			}
		}
		commonButtons.forEach { button ->
			for (entity in button.entities) {
				for (player in ship.onlinePassengers) {
					entity.isVisibleByDefault = false
					player.showEntity(IonServer, entity)
				}
			}
		}
		commonFeatures.forEach { feature ->
			for (entity in feature.entities) {
				for (player in ship.onlinePassengers) {
					entity.isVisibleByDefault = false
					player.showEntity(IonServer, entity)
				}
			}
		}
	}

	open fun despawn(){
		dynamicFeatures.forEach { it.despawn() }
		commonFeatures.forEach { it.despawn() }
		commonButtons.forEach { it.despawn() }

		dynamicFeatures.clear()
		commonFeatures.clear()
		commonButtons.clear()

		initialized = false
	}

	val worldUpBasisVector = Vector(0.0, 1.0, 0.0)

	/*
	Builds the local (right, up) basis for the display plane from its facing direction `dir`.
	`right` is derived from dir × worldUp, so it stays level with the horizon (pure yaw).
	`up` is derived from right × dir, so it tilts correctly as dir gains a y-component from pitch,
	instead of always being assumed to equal (0,1,0) like before.
	*/
	fun displayBasis(): Pair<Vector, Vector> {
		val forward = dir.clone().normalize()

		// Fallback axis for when dir is (near) straight up/down, where forward x worldUp
		// collapses to a zero vector and can't be normalized.
		val reference = worldUpBasisVector

		val right = forward.clone().crossProduct(reference).normalize()
		val up = right.clone().crossProduct(forward).normalize()
		return right to up
	}

	/*
	The following maths serves to center a given displayEntity onto the center of the location given.
	The maths is most useful for a given text display of sizeX & sizeY, as it will center the center of the text display
	onto the center of the block. Now respects full 3D rotation (yaw + pitch), not just yaw.
	*/
	fun displayLocation(): Location {
		val (right, up) = displayBasis()
		val oppositeDir = dir.clone().multiply(-1)

		val shipRight = ship.forward.direction.normalize().crossProduct(worldUpBasisVector)
		val shipOpposite = ship.forward.oppositeFace.direction

		return location.clone().add(
			// centers the displayEntity onto the center of the block
			Vector(0.5, 0.0, 0.5)
				.add(oppositeDir.clone().multiply(-0.25))
				.add(right.clone().multiply(0.5 * sizeX))
				.add(up.clone().multiply(-sizeY))
		).add(
			shipRight.clone().multiply(offset.x)
				.add(up.clone().multiply(offset.y))
				.add(shipOpposite.clone().multiply(offset.z))
		)
	}

	/**
	 * This function generates a location on the map, from the relative coordinates provided.
	 * Values for x and y must be between 0 and 1
	 * An example for its usage would be rx = .5, ry = .5. Which would be the center of the display.
	 *
	 * @rx: relative x
	 * @ry: relative y
	 * @isTextDisplay: shifts the returned location down by a pixel to account for the padding minecraft adds to text
	 * @return the location to set as the basis of the feature.
	 */
	fun locationAtRelativeCoordinates(rx: Double, ry: Double, isTextDisplay: Boolean): Location {
		val (right, up) = displayBasis()

		return displayLocation().clone().add(
			right.clone().multiply(-rx * sizeX)
				.add(
					up.clone().multiply(
						(sizeY * ry) + sizeY - 0.05 * sizeY * (if (isTextDisplay) 1.0 else 0.0)
					)
				)
		)
	}

	companion object : SLEventListener(){
		@EventHandler
		private fun onPlayerInteractWithInteraction(event: PlayerInteractEntityEvent) {
			val interaction = event.rightClicked
			if (interaction.type == EntityType.INTERACTION) {
				val player = event.player
				val ship = ActiveStarships.findByPassenger(player) ?: return
				//find the map that the interaction entity belongs too
				val map = ship.displays.find {
					it.commonButtons.find { button -> button.interaction == interaction } != null || it.dynamicFeatures.filterIsInstance<DisplayButtonFeature>()
						.find { button -> button.interaction == interaction } != null
				} ?: return
				//Find the button from the specified interaction entity inside the map
				val button = map.commonButtons.find { it.interaction == interaction }
					?: map.dynamicFeatures.filterIsInstance<DisplayButtonFeature>()
						.find { it.interaction == interaction } ?: return
				button.onClick()
			}
		}

		@EventHandler(priority = EventPriority.LOWEST)
		private fun onStarshipRelease(event: StarshipReleaseEvent) {
			event.starship.displays.forEach { display -> display.despawn() }
		}

		@EventHandler(priority = EventPriority.LOWEST)
		private fun onStarshipUnpilot(event: StarshipUnpilotEvent) {
			event.starship.displays.forEach { display -> display.despawn() }
		}

		@EventHandler
		private fun onStarshipPilot(event: StarshipPilotedEvent) {
			ActiveStarships.findByPilot(event.player)?.displays?.forEach { display -> display.init() }
		}
	}
}
