package net.horizonsend.ion.server.features.gas.collectionfactors

import net.horizonsend.ion.server.features.space.SpaceWorlds
import org.bukkit.Location

class SpaceOnlyFactor : CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return SpaceWorlds.contains(location.world)
    }
}
