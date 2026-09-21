package net.horizonsend.ion.server.features.starship.control.signs.display.features

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.control.signs.display.toVector3f
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

/**
 * Generates a feature for the following inputted properties
 *
 * @constructor Creates a new MapButton
 * @property identifier the name of this feature
 * @property display the map this feature belongs too
 * @property rx relative x coordinate to spawn at
 * @property ry relative y coordinate to spawn at
 * @property itemStack the item stack used in the item Display if there is one
 * @property offset is the space in the z relative axis you want the display to appear
 * @property relativeFeature allows you to specify if this feature is relative to another. This will add the relative locations together, and scales the Size accordingly
 */
open class DisplayFeature(
	val identifier: String,
	val display: net.horizonsend.ion.server.features.starship.control.signs.display.Display,
	var rx: Double,
	var ry: Double,
	var sizeX: Double,
	var sizeY: Double,
	var itemStack: ItemStack? = null,
	var component: Component? = null,
	val offset: Double,
	val relativeFeature: DisplayFeature? = null
) {
	val entities = mutableListOf<Entity>()
	var featureDisplay: org.bukkit.entity.Display? = null; private set

	open fun location() = display.locationAtRelativeCoordinates(
		(((relativeFeature?.rx ?: 0.0) - (relativeFeature?.sizeX ?: 0.0) / 2.0) + (rx).times(
			(((relativeFeature?.rx ?: 1.0) + (relativeFeature?.sizeX ?: 1.0) / 2.0) - ((relativeFeature?.rx
				?: 1.0) - (relativeFeature?.sizeX ?: 1.0) / 2.0))
		)),
		(((relativeFeature?.ry ?: 0.0) - (relativeFeature?.sizeY ?: 0.0) / 2.0) + (ry).times(
			((relativeFeature?.ry ?: 1.0) + (relativeFeature?.sizeY ?: 1.0) / 2.0) - ((relativeFeature?.ry
				?: 1.0) - (relativeFeature?.sizeY ?: 1.0) / 2.0)
		)),
		false,
	).add(display.dir.clone().multiply(display.shiftPerLayer * offset)).add(Vector(0.0,display.dir.y,0.0).multiply(display.shiftPerLayer*offset))


	open fun init() {
		initMainDisplay()
	}

	open fun initMainDisplay() {
		if (itemStack != null) {
			//Setup for ItemDisplay
			featureDisplay = display.location.world.spawnEntity(
				location().clone().add(
					Vector(
						0.0,
						sizeY / 16.0,
						0.0
					)
				),
				EntityType.ITEM_DISPLAY
			) as org.bukkit.entity.Display
			(featureDisplay as ItemDisplay).setItemStack(itemStack)
			featureDisplay!!.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(display.dir.toVector3f().mul(-1f)),
				Vector3d(
					sizeX * display.sizeX * (relativeFeature?.sizeX ?: 1.0),
					sizeY * display.sizeY * (relativeFeature?.sizeY ?: 1.0),
					0.01
				).toVector3f(),
				Quaternionf()
			)
		} else if (component != null) {
			//setup for TextDisplay
			featureDisplay = display.location.world.spawnEntity(
				location().add(
					Vector(
						0.0,
						sizeY / 16.0,
						0.0
					)
				),
				EntityType.TEXT_DISPLAY
			) as org.bukkit.entity.Display
			(featureDisplay as TextDisplay).text(component)
			(featureDisplay as TextDisplay).backgroundColor = Color.fromARGB(0, 0, 0, 0)
			featureDisplay!!.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(display.dir.toVector3f().mul(1f)),
				Vector3d(
					(5.0 * sizeX) * display.sizeX,
					(5.0 * sizeY) * display.sizeY,
					0.001
				).toVector3f(),
				Quaternionf()
			)
		}

		if (featureDisplay != null) {
			featureDisplay!!.teleportDuration = 0
			featureDisplay!!.interpolationDelay = 0
			featureDisplay!!.isPersistent = false
			featureDisplay!!.brightness = org.bukkit.entity.Display.Brightness(15, 0)
			entities.add(featureDisplay!!)
		}
		//Hide all the entities from players not in the ship. Showing only players of the ship
		this.entities.forEach { entity ->
			entity.isVisibleByDefault = false
			this.display.ship.onlinePassengers.forEach {
				it.showEntity(IonServer, entity)
			}
		}
	}

	open fun despawn() {
		display.ship.entityPassengers.removeAll(entities.toSet())
		entities.forEach { entity -> entity.remove() }
		entities.clear()
	}

	open fun tick() {
	}
}
