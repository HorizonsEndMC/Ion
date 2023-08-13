package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import org.bukkit.Location

class Gas(
	val name: String,
	val itemId: String,
	val factors: List<List<CollectionFactor>>,
	val burnProperties: List<BurnProperty>
	) {

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

	fun burnsWith(otherId: String): Boolean = burnProperties.any { it.otherId == otherId }
	fun getPower(otherId: String) = burnProperties.first { it.otherId == otherId }.power
	fun getBurnTime(otherId: String) = burnProperties.first { it.otherId == otherId }.burnTime

	data class BurnProperty(
		val otherId: String,
		val burnTime: Long,
		val power: Int
	)
}
