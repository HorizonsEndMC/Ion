package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.attribute.SmeltingResultAttribute
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class Smeltable(itemResult: Supplier<ItemStack>) : CustomItemComponent {

	constructor(customItemResult: CustomItem) : this(Supplier { customItemResult.constructItemStack() })

	private val attribute = SmeltingResultAttribute(itemResult)

	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {}
	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf(attribute)
}
