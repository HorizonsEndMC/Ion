package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

class DistanceFactor(private val origin: Location, private val maxDistance: Double, private val multiplier: Float) :
    CollectionFactor() {
    override fun factor(location: Location): Boolean {
        if (origin.world.name != location.world.name) return false
        val distance = location.distance(origin)
        if (distance < 1) return true
        return if (distance > maxDistance) false else RandomFactor(multiplier / distance.toFloat()).factor(location)
    }
}
