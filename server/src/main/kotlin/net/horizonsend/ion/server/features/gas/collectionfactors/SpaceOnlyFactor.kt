package net.horizonsend.ion.server.features.gas.collectionfactors

import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import org.bukkit.Location

class SpaceOnlyFactor : CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return location.world.ion.hasFlag(WorldFlag.SPACE_WORLD)
    }

	override fun canBeFound(location: Location): Boolean = factor(location)
}
