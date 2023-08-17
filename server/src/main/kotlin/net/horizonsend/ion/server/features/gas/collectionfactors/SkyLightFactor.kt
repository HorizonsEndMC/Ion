package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

class SkyLightFactor(private val minimum: Int, private val maximum: Int) : CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return location.block.lightFromSky in minimum..maximum
    }

	override fun canBeFound(location: Location): Boolean = factor(location)
}
