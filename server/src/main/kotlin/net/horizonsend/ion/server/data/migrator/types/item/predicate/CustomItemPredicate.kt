package net.horizonsend.ion.server.data.migrator.types.item.predicate

import net.horizonsend.ion.server.features.custom.CustomItemRegistry.newCustomItem
import org.bukkit.inventory.ItemStack

class CustomItemPredicate(val identifier: String) : ItemMigratorPredicate {
	override fun shouldMigrate(item: ItemStack): Boolean {
		return item.newCustomItem?.identifier == identifier
	}
}
