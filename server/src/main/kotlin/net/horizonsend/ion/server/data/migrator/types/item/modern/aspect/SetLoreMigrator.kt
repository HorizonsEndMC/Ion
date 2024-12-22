package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class SetLoreMigrator(val newLore: List<Component>) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		subject.setData(DataComponentTypes.LORE, ItemLore.lore(newLore))
		return MigratorResult.Mutation()
	}
}
