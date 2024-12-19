package net.horizonsend.ion.server.data.migrator

import net.horizonsend.ion.server.data.migrator.types.DataMigrator
import net.horizonsend.ion.server.data.migrator.types.item.ItemMigrationContext
import net.horizonsend.ion.server.data.migrator.types.item.legacy.LegacyCustomItemMigrator
import net.horizonsend.ion.server.data.migrator.types.item.modern.migrator.CustomItemStackMigrator
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class DataVersion private constructor(
	val versionNumber: Int,
	private val customItemMigrators: MutableMap<String, CustomItemStackMigrator>,
	private val legacyItemMigrators: MutableList<LegacyCustomItemMigrator>
) : Comparable<DataVersion> {
	fun migrateItem(inventory: Inventory, index: Int, itemStack: ItemStack, customItemIdentifier: String) {
		val context = ItemMigrationContext(inventory, index, itemStack)

		val modernMigrator = customItemMigrators[customItemIdentifier] ?: return
		context.migrate(modernMigrator)
	}

	fun migrateItem(inventory: Inventory, index: Int, itemStack: ItemStack) {
		val context = ItemMigrationContext(inventory, index, itemStack)

		println("Trying to migrate ${itemStack.type} in slot $index")

		legacyItemMigrators.filter { it.shouldMigrate(itemStack) }
		legacyItemMigrators.forEach { context.migrate(it) }
	}

	fun migrateInventory(inventory: Inventory) {
		for ((index, item) in inventory.contents.withIndex()) {
			if (item == null) continue

			val customItemIdentifier = item.persistentDataContainer.get(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING)

			if (customItemIdentifier != null) {
				migrateItem(inventory, index, item, customItemIdentifier)
			} else {
				println(2)
				migrateItem(inventory, index, item)
			}

		}
	}

	class Builder(private val versionNumber: Int) {
		private val customItemMigrators: MutableMap<String, CustomItemStackMigrator> = mutableMapOf()
		private val legacyItemStackMigrators: MutableList<LegacyCustomItemMigrator> = mutableListOf()

		fun addMigrator(migrator: DataMigrator<*, *>): Builder {
			when (migrator) {
				is CustomItemStackMigrator -> migrator.registerTo(customItemMigrators)
				is LegacyCustomItemMigrator -> legacyItemStackMigrators.add(migrator)
			}

			return this
		}

		fun build(): DataVersion = DataVersion(versionNumber, customItemMigrators, legacyItemStackMigrators)
	}

	companion object {
		fun builder(versionNumber: Int) = Builder(versionNumber)
	}

	override fun compareTo(other: DataVersion): Int {
		return versionNumber.compareTo(other.versionNumber)
	}
}
