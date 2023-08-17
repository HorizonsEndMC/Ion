package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location

class WorldChanceFactor(chance: Float, var world: String) : RandomFactor(chance) {
    override fun factor(location: Location): Boolean {
        return super.factor(location) && location.world.name.equals(world, ignoreCase = true)
    }

	override fun canBeFound(location: Location): Boolean = location.world.name.equals(world, ignoreCase = true)
}
