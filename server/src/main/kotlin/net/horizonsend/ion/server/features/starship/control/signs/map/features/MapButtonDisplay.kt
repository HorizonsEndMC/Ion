package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.kyori.adventure.text.Component
import org.bukkit.entity.Display
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
open class MapButtonDisplay(
	identifier: String,
	map: DisplayMap,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	itemStack: ItemStack? = null,
	component: Component? = null,
	offset: Double,
	relativeFeature: MapFeature? = null,
	val function: (it: DisplayMap) -> Unit
) : MapFeature(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature) {
	val interaction: Interaction = map.location.world.spawnEntity(
		this.location().add(if (itemStack== null&&component==null) Vector(0,0,0) else Vector(0.0,-sizeY,0.0)),
		EntityType.INTERACTION
	) as Interaction

	override fun init() {
		//Setup for Interaction Entity
		interaction.isResponsive = true
		interaction.interactionWidth = (sizeX * map.sizeX * (relativeFeature?.sizeX ?: 1.0)).toFloat()
		interaction.interactionHeight = (sizeY * map.sizeY * (relativeFeature?.sizeY ?: 1.0)).toFloat()
		entities.add(interaction)
		super.init()
	}

	fun getDisplayEntities(): List<Display> {
		return entities.filterIsInstance<Display>()
	}

	open fun onClick() {
		function.invoke(map)
	}

	fun onDespawn() {
		super.despawn()
	}
}
