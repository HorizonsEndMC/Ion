package net.horizonsend.ion.server.data.migrator.types

import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems
import org.bukkit.inventory.ItemStack

abstract class CustomItemStackMigrator(val customItemIdentifier: String, dataVersion: Int) : DataMigrator<ItemStack, NewCustomItem>(dataVersion) {
	val customItem get() = CustomItems.getByIdentifier(customItemIdentifier)!!
}
