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
		println(1)
		if (!migrator.shouldMigrate(item)) return
		println(2)
		val result = migrator.migrate(item)

		if (result !is MigratorResult.Replacement<*>) return
		println(3)
		result as MigratorResult.Replacement<ItemStack>

		// In case the context is re-used in a loop
		item = result.new
		sourceInventory.setItem(itemIndex, result.new)
	}
}
