package net.horizonsend.ion.server.data.migrator.types.item.modern.migrator

import net.horizonsend.ion.server.data.migrator.types.item.ItemMigrator
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.predicate.CustomItemsPredicate
import net.horizonsend.ion.server.features.custom.CustomItem
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.customItem
import org.bukkit.inventory.ItemStack

class ReplacementMigrator(vararg items: CustomItem) : ItemMigrator<CustomItem>(CustomItemsPredicate(*Array(items.size) { items[it].identifier })) {
	override fun performMigration(subject: ItemStack): MigratorResult<ItemStack> {
		val customItem = subject.customItem ?: return MigratorResult.Mutation()

		return MigratorResult.Replacement(customItem.constructItemStack(subject.amount))
	}
}
