package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Location
import java.util.Arrays

class WorldLimitFactor(private val enabledWorlds: Array<String>) : CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return Arrays.stream(enabledWorlds)
            .anyMatch { anotherString: String? -> location.world.name.equals(anotherString, ignoreCase = true) }
    }

	override fun canBeFound(location: Location): Boolean = factor(location)
}
