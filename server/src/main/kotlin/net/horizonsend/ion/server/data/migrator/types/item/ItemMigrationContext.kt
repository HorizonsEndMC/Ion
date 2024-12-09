package net.horizonsend.ion.server.data.migrator.types.item

import net.horizonsend.ion.server.data.migrator.types.item.migrator.CustomItemStackMigrator
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ItemMigrationContext(
	private val sourceInventory: Inventory,
	private val itemIndex: Int,
	private var item: ItemStack
) {
	fun migrate(migrator: CustomItemStackMigrator) {
		val result = migrator.migrate(item)

		if (result !is MigratorResult.Replacement<*>) return
		result as MigratorResult.Replacement<ItemStack>

		item = result.new
		sourceInventory.setItem(itemIndex, result.new)
	}
}
