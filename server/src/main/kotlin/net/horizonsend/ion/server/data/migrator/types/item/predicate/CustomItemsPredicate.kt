package net.horizonsend.ion.server.data.migrator.types.item.predicate

import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import org.bukkit.inventory.ItemStack

class CustomItemsPredicate(vararg identifier: String) : ItemMigratorPredicate {
	private val identifiers = identifier.toList()
	override fun shouldMigrate(item: ItemStack): Boolean {
		return identifiers.contains(item.customItem?.identifier ?: return false)
	}
}
