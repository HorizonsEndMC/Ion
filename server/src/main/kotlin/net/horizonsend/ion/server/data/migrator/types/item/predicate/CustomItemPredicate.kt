package net.horizonsend.ion.server.data.migrator.types.item.predicate

import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import org.bukkit.inventory.ItemStack

class CustomItemPredicate(val identifier: String) : ItemMigratorPredicate {
	override fun shouldMigrate(item: ItemStack): Boolean {
		return item.customItem?.identifier == identifier
	}
}
