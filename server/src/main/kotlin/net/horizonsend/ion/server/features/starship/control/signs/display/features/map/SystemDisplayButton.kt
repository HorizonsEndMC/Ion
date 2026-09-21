package net.horizonsend.ion.server.features.starship.control.signs.display.features.map

import net.horizonsend.ion.server.features.starship.control.signs.display.Display
import net.horizonsend.ion.server.features.starship.control.signs.display.DisplayMap
import net.horizonsend.ion.server.features.starship.control.signs.display.MapState
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayButtonFeature
import net.horizonsend.ion.server.features.starship.control.signs.display.features.DisplayFeature
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import kotlin.math.cos
import kotlin.math.max

class SystemDisplayButton(
	identifier: String,
	map: Display,
	rx: Double,
	ry: Double,
	sizeX: Double,
	sizeY: Double,
	itemStack: ItemStack? = null,
	component: Component? = null,
	offset: Double,
	relativeFeature: DisplayFeature? = null,
	function: (it: Display) -> Unit = {}
): DisplayButtonFeature(identifier, map, rx, ry, sizeX, sizeY, itemStack, component, offset, relativeFeature, function) {
	override fun init() {
		super.init()
		interaction.interactionHeight = (sizeY * display.sizeY * (relativeFeature?.sizeY ?: 1.0)).toFloat().times(
            max(
                cos(
                    display.dir.y
                ).toFloat(), .05f
            )
        )
	}
	override fun onClick() {
		if (display !is DisplayMap) return
		display.state = MapState.SYSTEMS_MAP
		display.systemForSystemMap = Bukkit.getWorld(this.identifier)
		display.placeSystemsMap()
	}
}
