package net.horizonsend.ion.server.data.migrator

import net.horizonsend.ion.server.data.migrator.types.DataMigrator
import net.horizonsend.ion.server.data.migrator.types.item.ItemMigrationContext
import net.horizonsend.ion.server.data.migrator.types.item.migrator.CustomItemStackMigrator
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class DataVersion private constructor(
	val versionNumber: Int,
	private val customItemMigrators: MutableMap<String, CustomItemStackMigrator>
) : Comparable<DataVersion> {
	fun migrateItem(inventory: Inventory, index: Int, itemStack: ItemStack, customItemIdentifier: String) {
		val migratorFor = customItemMigrators[customItemIdentifier] ?: return
		val context = ItemMigrationContext(inventory, index, itemStack)

		context.migrate(migratorFor)
	}

	fun migrateInventory(inventory: Inventory, chunkVersion: Int) {
		for ((index, item) in inventory.contents.withIndex()) {
			if (item == null) continue
			val customItemIdentifier = item.persistentDataContainer.get(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING) ?: continue
			migrateItem(inventory, index, item, customItemIdentifier)
		}
	}

	class Builder(private val versionNumber: Int) {
		private val customItemMigrators: MutableMap<String, CustomItemStackMigrator> = mutableMapOf()

		fun addMigrator(migrator: DataMigrator<*, *>): Builder {
			when (migrator) {
				is CustomItemStackMigrator -> migrator.registerTo(customItemMigrators)
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
