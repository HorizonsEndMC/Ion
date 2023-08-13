package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

class AtmosphereHeightFactor(private val minAtmosphereHeight: Double, private val maxAtmosphereHeight: Double) :
    CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return !SpaceOnlyFactor().factor(location) && location.y >= minAtmosphereHeight && location.y <= maxAtmosphereHeight
    }
}
