package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

/** Always returns true, for testing */
class DefaultFactor : CollectionFactor() {
	override fun factor(location: Location): Boolean = true
	override fun canBeFound(location: Location): Boolean = true
}
