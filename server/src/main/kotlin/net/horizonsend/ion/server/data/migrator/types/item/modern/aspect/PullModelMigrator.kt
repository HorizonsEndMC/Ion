package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.miscellaneous.utils.setModel
import org.bukkit.inventory.ItemStack

class PullModelMigrator(val customItem: CustomItem) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		val model = customItem.getItemFactory().customModel ?: return MigratorResult.Mutation()
		subject.setModel(model)
		return MigratorResult.Mutation()
	}
}

