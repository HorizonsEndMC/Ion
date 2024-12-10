package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.features.custom.NewCustomItem
import org.bukkit.inventory.ItemStack

/** Pulls lore from the custom item registry and sets the item's lore */
class PullLoreMigrator(val customItem: NewCustomItem) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		subject.setData(DataComponentTypes.LORE, ItemLore.lore(customItem.assembleLore(subject)))
		return MigratorResult.Mutation()
	}
}
