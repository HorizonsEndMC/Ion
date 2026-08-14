package net.horizonsend.ion.server.features.starship.control.signs.map

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
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
 */
open class MapFeature(var identifier: String, val map: DisplayMap, val rx: Float, val ry: Float, val sizeX: Float, val sizeY: Float, val itemStack: ItemStack, val offset: Float) {
	open val location = map.locationAtRelativeCoordinates(rx, ry, false).add(map.dir.clone().multiply(map.shiftPerLayer * offset))

	open val itemDisplay: ItemDisplay? = null

	open fun init(){
		initItemDisplay()
	}

	open fun initItemDisplay(){
		//Setup for ItemDisplay
		map.location.world.spawnEntity(location,
			EntityType.ITEM_DISPLAY) as ItemDisplay
		itemDisplay?.setItemStack(itemStack)
		itemDisplay?.teleportDuration = 0
		itemDisplay?.interpolationDelay = 0
		itemDisplay?.isPersistent = false
		itemDisplay?.brightness = Display.Brightness(15,0)
		itemDisplay?.transformation = Transformation(
			Vector3f(),
			ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f().mul(-1f)),
			Vector3f(sizeX * map.sizeX, sizeY * map.sizeY, 0.001f),
			Quaternionf()
		)
	}
}
