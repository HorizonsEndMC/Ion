package net.horizonsend.ion.server.features.starship.control.signs.map

import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.inventory.ItemStack

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
class MapButtonDisplay(identifier: String, map: DisplayMap, rx: Float, ry: Float, sizeX: Float, sizeY: Float, itemStack: ItemStack, offset: Float) : MapFeature(identifier, map, rx, ry, sizeX, sizeY, itemStack, offset) {

	val interaction: Interaction = map.location.world.spawnEntity(this.location.clone().add(0.0,-sizeY/16.0, 0.0), EntityType.INTERACTION) as Interaction

	override fun init() {
		super.init()

		//Setup for Interaction Entity
		interaction.isResponsive = true
		interaction.interactionWidth = sizeX * map.sizeX
		interaction.interactionHeight = sizeY * map.sizeY
	}
}
