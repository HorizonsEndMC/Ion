package net.horizonsend.ion.server.data.migrator.types.item.migrator

import net.horizonsend.ion.server.data.migrator.types.DataMigrator
import net.horizonsend.ion.server.data.migrator.types.item.predicate.ItemMigratorPredicate
import net.horizonsend.ion.server.features.custom.NewCustomItem
import org.bukkit.inventory.ItemStack

abstract class CustomItemStackMigrator(
	private val predicate: ItemMigratorPredicate,
) : DataMigrator<ItemStack, NewCustomItem>() {
	fun shouldMigrate(itemStack: ItemStack) = predicate.shouldMigrate(itemStack)

	abstract fun registerTo(map: MutableMap<String, CustomItemStackMigrator>)
}


