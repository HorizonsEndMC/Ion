package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class CustomNameMigrator(private val newName: Component) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		subject.setData(DataComponentTypes.CUSTOM_NAME, newName)
		return MigratorResult.Mutation()
	}
}
