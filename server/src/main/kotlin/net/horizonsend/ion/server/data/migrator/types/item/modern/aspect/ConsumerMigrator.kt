package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class ConsumerMigrator(val consumer: Consumer<ItemStack>) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		consumer.accept(subject)
		return MigratorResult.Mutation()
	}
}
