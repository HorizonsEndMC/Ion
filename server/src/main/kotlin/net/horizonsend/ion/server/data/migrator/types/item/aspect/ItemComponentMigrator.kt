package net.horizonsend.ion.server.data.migrator.types.item.aspect

import io.papermc.paper.datacomponent.DataComponentType.Valued
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import org.bukkit.inventory.ItemStack

class ItemComponentMigrator<T : Any>(val type: Valued<T>, val value: T) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		subject.setData(type, value)
		return MigratorResult.Mutation()
	}
}
