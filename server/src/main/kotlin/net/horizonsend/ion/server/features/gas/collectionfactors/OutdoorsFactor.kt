package net.horizonsend.ion.server.features.gas.collectionfactors

import net.horizonsend.ion.server.miscellaneous.utils.LegacyBlockUtils
import org.bukkit.Location

class OutdoorsFactor : CollectionFactor() {
    override fun factor(location: Location): Boolean {
        return !LegacyBlockUtils.isInside(location, 2)
    }

	override fun canBeFound(location: Location): Boolean = factor(location)
}
