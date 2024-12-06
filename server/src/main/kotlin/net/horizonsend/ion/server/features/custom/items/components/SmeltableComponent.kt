package net.horizonsend.ion.server.features.custom.items.components

import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.attribute.SmeltingResultAttribute
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class SmeltableComponenta(itemResult: Supplier<ItemStack>) : CustomItemComponent {
	constructor(customItemResult: NewCustomItem) : this(Supplier { customItemResult.constructItemStack() })

	private val attribute = SmeltingResultAttribute(itemResult)

	override fun decorateBase(baseItem: ItemStack) {}
	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = listOf(attribute)
}
