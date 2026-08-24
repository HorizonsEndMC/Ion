package net.horizonsend.ion.server.features.starship.control.signs.map.features

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.starship.control.signs.map.DisplayMap
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.features.waypoint.command.WaypointCommand
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

//TODO ACTUALLY TEST THIS
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
		if (map.ship.playerPilot!=null) {
			val beaconsInSystem = ConfigurationFiles.serverConfiguration().beacons.filter {
				it.spaceLocation.bukkitWorld().name.equals(identifier, ignoreCase = true)
			}
			val vertex = WaypointManager.playerGraphs[map.ship.playerPilot!!.uniqueId]?.let { graph -> WaypointManager.getVertex(
				graph,
				beaconsInSystem.first().name)
			}
			val lastWaypoint = WaypointManager.playerPaths[map.ship.playerPilot!!.uniqueId]?.first()?.edgeList
			if (lastWaypoint?.last()?.target?.loc?.world == lastWaypoint?.last()?.source?.loc?.world){
				lastWaypoint?.removeLast()
			}

			if (vertex != null) {
				WaypointCommand.addVertexToRoute(map.ship.playerPilot!!, vertex)
			}
		}
	}
}
