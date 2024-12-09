package net.horizonsend.ion.server.data.migrator

import net.horizonsend.ion.server.data.migrator.types.DataMigrator
import net.horizonsend.ion.server.data.migrator.types.item.ItemMigrationContext
import net.horizonsend.ion.server.data.migrator.types.item.migrator.CustomItemStackMigrator
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.newCustomItem
import net.horizonsend.ion.server.features.custom.NewCustomItem
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class DataVersion private constructor(
	val versionNumber: Int,
	private val customItemMigrators: MutableMap<String, CustomItemStackMigrator>
) : Comparable<DataVersion> {
	fun migrateItem(inventory: Inventory, index: Int, itemStack: ItemStack, customItem: NewCustomItem, chunkVersion: Int) {
		val migratorFor = customItemMigrators[customItem.identifier] ?: return
		val context = ItemMigrationContext(inventory, index, itemStack, customItem)

		context.migrate(migratorFor, chunkVersion)
	}

	fun migrateInventory(inventory: Inventory, chunkVersion: Int) {
		for ((index, item) in inventory.contents.withIndex()) {
			if (item == null) continue
			val customItem = item.newCustomItem ?: continue
			migrateItem(inventory, index, item, customItem, chunkVersion)
		}
	}

	class Builder(private val versionNumber: Int) {
		private val customItemMigrators: MutableMap<String, CustomItemStackMigrator> = mutableMapOf()

		fun addMigrator(migrator: DataMigrator<*, *>): Builder {
			when (migrator) {
				is CustomItemStackMigrator -> customItemMigrators[migrator.customItem.identifier] = migrator
			}

			return this
		}

		fun build(): DataVersion = DataVersion(versionNumber, customItemMigrators)
	}

	companion object {
		fun builder(versionNumber: Int) = Builder(versionNumber)
	}

	override fun compareTo(other: DataVersion): Int {
		return versionNumber.compareTo(other.versionNumber)
	}
}
