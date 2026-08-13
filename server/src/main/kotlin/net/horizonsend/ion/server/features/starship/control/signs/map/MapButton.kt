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
class MapButton(identifier: String, map: DisplayMap, rx: Float, ry: Float, sizeX: Float, sizeY: Float, itemStack: ItemStack, offset: Float) : MapFeature(identifier, map, rx, ry, sizeX, sizeY, itemStack, offset) {

	val interaction: Interaction = map.location.world.spawnEntity(this.location, EntityType.INTERACTION) as Interaction

	override fun init() {
		super.init()

		//Setup for Interaction Entity
		interaction.isResponsive = true
		interaction.interactionWidth = sizeX * map.sizeX
		interaction.interactionHeight = sizeY * map.sizeY
	}
}
