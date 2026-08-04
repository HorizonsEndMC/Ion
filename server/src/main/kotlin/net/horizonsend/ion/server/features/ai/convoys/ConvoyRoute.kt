package net.horizonsend.ion.server.features.ai.convoys

import org.bukkit.Location

interface ConvoyRoute {


	fun advanceDestination(): Location?

	fun getSourceLocation(): Location
}
