package net.horizonsend.ion.server.data.migrator.types.item

import net.horizonsend.ion.server.data.migrator.types.DataMigrator
import net.horizonsend.ion.server.data.migrator.types.item.predicate.ItemMigratorPredicate
import org.bukkit.inventory.ItemStack

abstract class ItemMigrator<W : Any>(protected val predicate: ItemMigratorPredicate) : DataMigrator<ItemStack, W>() {
	fun shouldMigrate(itemStack: ItemStack) = predicate.shouldMigrate(itemStack)
}
