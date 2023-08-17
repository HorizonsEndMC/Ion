package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

open class RandomFactor(private val chance: Float) : CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return Math.random() <= chance
    }

	override fun canBeFound(location: Location): Boolean = true
}
