package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import org.bukkit.Location

class Gas(val name: String, val itemId: String, val factors: List<List<CollectionFactor>>) {

    fun isAvailable(location: Location?): Boolean {
        return factors.stream().anyMatch { f: List<CollectionFactor> ->
            f.stream().allMatch { i: CollectionFactor ->
                i.factor(
                    location!!
                )
            }
        }
    }

    val item: CustomItem get() = CustomItems[itemId]!!
}
