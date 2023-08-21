package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

class WorldLimitFactor(private val enabledWorlds: List<String>) : CollectionFactor() {
	override fun factor(location: Location): Boolean {
		return enabledWorlds.any { allowedWorld -> location.world.name.equals(allowedWorld, ignoreCase = true) }
	}

	override fun canBeFound(location: Location): Boolean = factor(location)
}
