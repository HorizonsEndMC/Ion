package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.map.MapState
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

class SystemMapFeature(
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
	function: (it: DisplayMap) -> Unit = {}
): MapButtonDisplay(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, function) {
	override fun onClick() {
		map.state = MapState.SYSTEMS_MAP
		map.systemForSystemMap = Bukkit.getWorld(this.identifier)
		map.placeSystemsMap()
	}
}
