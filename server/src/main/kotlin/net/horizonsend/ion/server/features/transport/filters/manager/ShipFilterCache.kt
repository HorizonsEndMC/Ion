package net.horizonsend.ion.server.features.transport.filters.manager

import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey

class ShipFilterCache(manager: ShipTransportManager) : FilterCache(manager) {
	fun loadFilters(possibleLocs: Set<Vec3i>) {
		for (location in possibleLocs) {
			getFilter(toBlockKey(location))
		}
	}
}
