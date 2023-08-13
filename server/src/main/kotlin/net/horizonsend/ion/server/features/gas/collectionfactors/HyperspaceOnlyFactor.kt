package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

class HyperspaceOnlyFactor : CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return location.world.name.contains("Hyperspace", ignoreCase = true)
    }
}
