package net.horizonsend.ion.server.features.starship.control.signs.display.features

import net.kyori.adventure.text.Component
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

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
 * @property relativeFeature is the feature that this button should align with
 */
open class DisplayButtonFeature(
	identifier: String,
	map: net.horizonsend.ion.server.features.starship.control.signs.display.Display,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	itemStack: ItemStack? = null,
	component: Component? = null,
	offset: Double,
	relativeFeature: DisplayFeature? = null,
	val function: (it: net.horizonsend.ion.server.features.starship.control.signs.display.Display) -> Unit
) : DisplayFeature(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature) {
	val interaction: Interaction = map.location.world.spawnEntity(
		this.location().add(
			if (itemStack== null&&component==null) Vector(0.0,(1.0/512.0)/sizeY, 0.0)
			else if(component==null) Vector(0.0,-map.sizeY/16.0,0.0)
			else Vector(0.0,map.sizeY/16.0,0.0)
		),
		EntityType.INTERACTION
	) as Interaction

	override fun init() {
		//Setup for Interaction Entity
		interaction.isResponsive = true
		interaction.interactionWidth = (sizeX * display.sizeX * (relativeFeature?.sizeX ?: 1.0)).toFloat()
		interaction.interactionHeight = (sizeY * display.sizeY * (relativeFeature?.sizeY ?: 1.0)).toFloat()
		entities.add(interaction)
		super.init()
	}

	fun getDisplayEntities(): List<org.bukkit.entity.Display> {
		return entities.filterIsInstance<org.bukkit.entity.Display>()
	}

	open fun onClick() {
		function(display)
	}

	fun onDespawn() {
		super.despawn()
	}
}
