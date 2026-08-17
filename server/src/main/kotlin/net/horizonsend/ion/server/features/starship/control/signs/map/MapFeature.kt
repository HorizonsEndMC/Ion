package net.horizonsend.ion.server.features.starship.control.signs.map

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap.Companion.toVector3f
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
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
 * @property itemStack the item stack used in the item Display
 * @property offset is the space in the z relative axis you want the display to appear
 * @property relativeFeature allows you to specify if this feature is relative to another. This will add the relative locations together, and scales the Size accordingly
 */
open class MapFeature(var identifier: String, val map: DisplayMap, val rx: Double, val ry: Double, val sizeX: Double, val sizeY: Double, val itemStack: ItemStack, val offset: Double, var relativeFeature: MapFeature? = null) {
	//open val location = map.locationAtRelativeCoordinates((rx*(relativeFeature?.sizeX?:1f)) + if(relativeFeature != null) 1f/32f else 0f, (ry*(relativeFeature?.sizeY ?: 1f) + if(relativeFeature != null) 2f/32f else 0f), false)
	open val location = map.locationAtRelativeCoordinates(
		(((relativeFeature?.rx ?: 0.0)-(relativeFeature?.sizeX ?: 0.0)/2.0)+(rx).times((((relativeFeature?.rx ?: 1.0)+(relativeFeature?.sizeX ?: 1.0)/2.0)-((relativeFeature?.rx ?: 1.0)-(relativeFeature?.sizeX ?: 1.0)/2.0)))),
		(((relativeFeature?.ry ?: 0.0)-(relativeFeature?.sizeY ?: 0.0)/2.0)+(ry).times(((relativeFeature?.ry ?: 1.0)+(relativeFeature?.sizeY ?: 1.0)/2.0)-((relativeFeature?.ry ?: 1.0)-(relativeFeature?.sizeY ?:1.0)/2.0))),
		false
	).add(map.dir.clone().multiply(map.shiftPerLayer * offset))

	open var itemDisplay: ItemDisplay? = null

	open fun init() {
		initItemDisplay()
	}

	open fun initItemDisplay() {
		if(itemStack.type == Material.AIR) return
		//Setup for ItemDisplay
		itemDisplay = map.location.world.spawnEntity(
			location.clone().add(Vector(0.0, sizeY / 16.0, 0.0)),
			EntityType.ITEM_DISPLAY
		) as ItemDisplay
		itemDisplay?.setItemStack(itemStack)
		itemDisplay?.teleportDuration = 0
		itemDisplay?.interpolationDelay = 0
		itemDisplay?.isPersistent = false
		itemDisplay?.brightness = Display.Brightness(15, 0)
		itemDisplay?.transformation = Transformation(
			Vector3f(),
			ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f().mul(-1f)),
			Vector3d(sizeX * map.sizeX * (relativeFeature?.sizeX ?: 1.0), sizeY * map.sizeY * (relativeFeature?.sizeY ?: 1.0), 0.001).toVector3f(),
			Quaternionf()
		)
	}

	open fun despawn() {
		this.itemDisplay?.remove()
	}
}
