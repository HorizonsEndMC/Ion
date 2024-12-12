package net.horizonsend.ion.server.data.migrator.types.item.modern.migrator

import net.horizonsend.ion.server.data.migrator.types.item.ItemMigrator
import net.horizonsend.ion.server.data.migrator.types.item.predicate.ItemMigratorPredicate
import net.horizonsend.ion.server.features.custom.items.CustomItem

abstract class CustomItemStackMigrator(
	predicate: ItemMigratorPredicate,
) : ItemMigrator<CustomItem>(predicate) {
	abstract fun registerTo(map: MutableMap<String, CustomItemStackMigrator>)
}


