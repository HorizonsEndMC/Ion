package net.horizonsend.ion.server.features.starship.control.signs.map

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Generates a button and interaction for the following inputted properties
 *
 * @constructor Creates a new MapButton
 * @property identifier the name of this Button
 * @property map the map this Button belongs too
 * @property rx relative x coordinate to spawn at
 * @property ry relative y coordinate to spawn at
 * @property itemStack the item stack used in the item Display
 */
class MapButton(var identifier: String, val map: DisplayMap, val rx: Double, val ry: Double, val sizeX: Float, val sizeY: Float, val itemStack: ItemStack) {
	val itemDisplay: ItemDisplay = map.location.world.spawnEntity(map.locationAtRelativeCoordinates(rx, ry, false),
		EntityType.ITEM_DISPLAY) as ItemDisplay
	val interaction: Interaction = map.location.world.spawnEntity(map.locationAtRelativeCoordinates(rx, ry, false), EntityType.INTERACTION) as Interaction

	init {
		//Setup for ItemDisplay
	    itemDisplay.setItemStack(itemStack)
		itemDisplay.teleportDuration = 0
		itemDisplay.interpolationDelay = 0
		itemDisplay.isPersistent = false
		itemDisplay.brightness = Display.Brightness(15,0)
		itemDisplay.transformation = Transformation(
			Vector3f(),
			ClientDisplayEntities.rotateToFaceVector(map.dir.toVector3f()),
			Vector3f(sizeX * map.sizeX, sizeY * map.sizeY, 0.001f),
			Quaternionf()
		)

		//Setup for Interaction Entity
		interaction.isResponsive = true
		interaction.interactionWidth = sizeX * map.sizeX
		interaction.interactionHeight = sizeY * map.sizeY
	}
}
