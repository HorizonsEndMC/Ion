package net.horizonsend.ion.server.features.ai.convoys

import org.bukkit.Location

class LocationConvoyRoute(
	val source: Location,
	val destinations: MutableList<Location>,
) : ConvoyRoute{

	override fun advanceDestination() : Location? {
		while (destinations.isNotEmpty()) {
			val next = destinations.removeFirst()

			if (next == source && destinations.isNotEmpty()) {
				// Source reached in middle of loop: push to end and skip
				destinations.add(next)
				continue
			}

			return next
		}

		// If we ran out of destinations, disband
		return null
	}

	override fun getSourceLocation(): Location {
		return source
	}
}
