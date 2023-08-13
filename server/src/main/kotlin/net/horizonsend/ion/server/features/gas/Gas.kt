package net.horizonsend.ion.server.features.gas

import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location

class Gas(
	val name: String,
	val itemId: String,
	private val factors: List<List<CollectionFactor>>
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

	data class BurnProperty(
		val otherId: String,
		val burnTime: Long,
		val power: Int
	)

	enum class GasType(color: NamedTextColor) {
		NONE(NamedTextColor.DARK_GRAY),
		FLAMMABLE(NamedTextColor.RED),
		OXIDIZER(NamedTextColor.YELLOW),
		NOBLE(NamedTextColor.DARK_PURPLE),
		OTHER(NamedTextColor.GRAY)
	}
}
