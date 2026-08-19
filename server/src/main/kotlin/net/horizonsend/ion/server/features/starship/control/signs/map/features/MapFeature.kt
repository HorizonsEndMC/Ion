package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap.Companion.toVector3f
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.Display
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
 * Generates an itemDisplay for the following inputted properties
 *
 * @constructor Creates a new MapButton
 * @property identifier the name of this feature
 * @property map the map this feature belongs too
 * @property rx relative x coordinate to spawn at
 * @property ry relative y coordinate to spawn at
 * @property itemStack the item stack used in the item Display if there is one
 * @property offset is the space in the z relative axis you want the display to appear
 * @property relativeFeature allows you to specify if this feature is relative to another. This will add the relative locations together, and scales the Size accordingly
 */
open class MapFeature(
	val identifier: String,
	val map: DisplayMap,
	var rx: Double,
	var ry: Double,
	var sizeX: Double,
	var sizeY: Double,
	var itemStack: ItemStack? = null,
	var component: Component? = null,
	val offset: Double,
	val relativeFeature: MapFeature? = null
) {
	val entities = mutableListOf<Entity>()
	protected var display: Display? = null

	/*
	The following maths scales the coordinate axis specified in rx and ry of this class, to the size and rx/ry value of the
	relative feature class. This allows for quick and easy placement of features atop another
	For reference, its quite literally just the general formula for linear interpolation.
	Given that the bound of rx is [0,1] and the bound of relative Feature is [rx-sizeX, rx+sizeX] = [x,y]
	Then the formula used is:
	relativeCoordinate(z) = x + z*(y-x)

	Where z is either rx or ry

	The last step for getting the location is to get the relative shift in the axis perpendicular to the text display.
	This is a key step to stop z fighting, and to allow the overall display to have some depth.
	 */
	open fun location() = map.locationAtRelativeCoordinates(
		(((relativeFeature?.rx ?: 0.0) - (relativeFeature?.sizeX ?: 0.0) / 2.0) + (rx).times(
			(((relativeFeature?.rx ?: 1.0) + (relativeFeature?.sizeX ?: 1.0) / 2.0) - ((relativeFeature?.rx
				?: 1.0) - (relativeFeature?.sizeX ?: 1.0) / 2.0))
		)),
		(((relativeFeature?.ry ?: 0.0) - (relativeFeature?.sizeY ?: 0.0) / 2.0) + (ry).times(
			((relativeFeature?.ry ?: 1.0) + (relativeFeature?.sizeY ?: 1.0) / 2.0) - ((relativeFeature?.ry
				?: 1.0) - (relativeFeature?.sizeY ?: 1.0) / 2.0)
		)),
		false,
	).add(map.dir.clone().multiply(map.shiftPerLayer * offset))


	open fun init() {
		initItemDisplay()
	}

	open fun initItemDisplay() {
		if (itemStack != null) {
			//Setup for ItemDisplay
			display = map.location.world.spawnEntity(
				location().clone().add(
					Vector(
						0.0,
						sizeY / 16.0,
						0.0
					)
				),
				EntityType.ITEM_DISPLAY
			) as Display
			(display as ItemDisplay).setItemStack(itemStack)
			display!!.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f().mul(-1f)),
				Vector3d(
					sizeX * map.sizeX * (relativeFeature?.sizeX ?: 1.0),
					sizeY * map.sizeY * (relativeFeature?.sizeY ?: 1.0),
					0.001
				).toVector3f(),
				Quaternionf()
			)
		} else if (component != null) {
			//setup for TextDisplay
			display = map.location.world.spawnEntity(
				location().add(
					Vector(
						0.0,
						sizeY / 16.0,
						0.0
					)
				),
				EntityType.TEXT_DISPLAY
			) as Display
			(display as TextDisplay).text(component)
			(display as TextDisplay).backgroundColor = Color.fromARGB(0, 0, 0, 0)
			display!!.transformation = Transformation(
				Vector3f(),
				ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f().mul(1f)),
				Vector3d(
					(5.0 * sizeX) * map.sizeX,
					(5.0 * sizeY) * map.sizeY,
					0.001
				).toVector3f(),
				Quaternionf()
			)
		}

		if (display != null) {
			display!!.teleportDuration = 0
			display!!.interpolationDelay = 0
			display!!.isPersistent = false
			display!!.brightness = Display.Brightness(15, 0)
			entities.add(display!!)
		}
	}

	open fun despawn() {
		map.ship.entityPassengers.removeAll(entities.toSet())
		entities.forEach { entity -> entity.remove() }
		entities.clear()
	}

	open fun tick() {
	}
}
