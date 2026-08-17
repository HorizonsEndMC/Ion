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
 * @property offset is the space in the z relative axis you want the display to appear.
 */
class MapButtonDisplay(identifier: String, map: DisplayMap, rx: Double, ry: Double, sizeX: Double, sizeY: Double, itemStack: ItemStack, offset: Double, relativeFeature: MapFeature? = null,val function: (it: DisplayMap) -> Unit) : MapFeature(identifier, map, rx, ry, sizeX, sizeY, itemStack, offset, relativeFeature) {

	val interaction: Interaction = map.location.world.spawnEntity(this.location.clone(), EntityType.INTERACTION) as Interaction //.add(0.0,-sizeY/16.0, 0.0)

	override fun init() {
		super.init()
		//Setup for Interaction Entity
		interaction.isResponsive = true
		interaction.interactionWidth = (sizeX * map.sizeX * (relativeFeature?.sizeX ?: 1.0)).toFloat()
		interaction.interactionHeight = (sizeY * map.sizeY * (relativeFeature?.sizeY ?: 1.0)).toFloat()
	}

	fun onClick(){
		function.invoke(map)
	}

	fun onDespawn(){
		super.despawn()
		interaction.remove()
	}
}
