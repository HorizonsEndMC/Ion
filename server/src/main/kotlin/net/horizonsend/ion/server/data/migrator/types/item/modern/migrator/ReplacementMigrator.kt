package net.horizonsend.ion.server.data.migrator.types.item.modern.migrator

import net.horizonsend.ion.server.data.migrator.types.item.ItemMigrator
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.predicate.CustomItemsPredicate
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.newCustomItem
import net.horizonsend.ion.server.features.custom.NewCustomItem
import org.bukkit.inventory.ItemStack

class ReplacementMigrator(vararg items: NewCustomItem) : ItemMigrator<NewCustomItem>(CustomItemsPredicate(*Array(items.size) { items[it].identifier })) {
	override fun performMigration(subject: ItemStack): MigratorResult<ItemStack> {
		val customItem = subject.newCustomItem ?: return MigratorResult.Mutation()

		return MigratorResult.Replacement(customItem.constructItemStack(subject.amount))
	}
}
