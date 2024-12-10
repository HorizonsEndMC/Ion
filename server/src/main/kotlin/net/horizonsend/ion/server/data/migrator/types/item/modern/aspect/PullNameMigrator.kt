package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.features.custom.NewCustomItem
import org.bukkit.inventory.ItemStack

/** Pulls name from the custom item registry and sets the item's lore */
class PullNameMigrator(val customItem: NewCustomItem) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		subject.setData(DataComponentTypes.CUSTOM_NAME, customItem.displayName)
		return MigratorResult.Mutation()
	}
}

