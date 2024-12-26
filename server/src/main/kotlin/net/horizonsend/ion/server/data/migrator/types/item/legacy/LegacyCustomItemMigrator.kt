package net.horizonsend.ion.server.data.migrator.types.item.legacy

import net.horizonsend.ion.server.data.migrator.types.item.ItemMigrator
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.predicate.ItemMigratorPredicate
import org.bukkit.inventory.ItemStack

class LegacyCustomItemMigrator(
	predicate: ItemMigratorPredicate,
	private val converter: (ItemStack) -> MigratorResult<ItemStack>
) : ItemMigrator<ItemStack>(predicate) {
	override fun performMigration(subject: ItemStack): MigratorResult<ItemStack> {
		return converter.invoke(subject)
	}
}
