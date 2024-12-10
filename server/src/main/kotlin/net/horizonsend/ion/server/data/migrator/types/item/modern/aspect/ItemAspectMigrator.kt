package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import org.bukkit.inventory.ItemStack

interface ItemAspectMigrator {
	fun migrate(subject: ItemStack) : MigratorResult<ItemStack>
}
