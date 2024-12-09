package net.horizonsend.ion.server.data.migrator.types.item.predicate

import org.bukkit.inventory.ItemStack

fun interface ItemMigratorPredicate {
	fun shouldMigrate(item: ItemStack): Boolean

	data object True : ItemMigratorPredicate {
		override fun shouldMigrate(item: ItemStack): Boolean {
			return true
		}
	}
}
