package net.horizonsend.ion.server.data.migrator.types.item.predicate

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.inventory.ItemStack
import java.awt.Component

class ItemNamePredicate(val name: Component) : ItemMigratorPredicate {
	override fun shouldMigrate(item: ItemStack): Boolean {
		return item.getData(DataComponentTypes.ITEM_NAME) == name
	}
}
