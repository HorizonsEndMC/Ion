package net.horizonsend.ion.server.data.migrator.types.item

import net.horizonsend.ion.server.data.migrator.types.item.migrator.CustomItemStackMigrator
import net.horizonsend.ion.server.features.custom.NewCustomItem
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ItemMigrationContext(
	private val sourceInventory: Inventory,
	private val itemIndex: Int,
	private var item: ItemStack,
	private val customItem: NewCustomItem
) {
	fun migrate(migrator: CustomItemStackMigrator, chunkDataVersion: Int) {
		val result = migrator.migrate(item, customItem)

		if (result !is MigratorResult.Replacement<*>) return
		result as MigratorResult.Replacement<ItemStack>

		item = result.new
		sourceInventory.setItem(itemIndex, result.new)
	}
}
