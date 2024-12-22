package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ChangeTypeMigrator(private val newType: Material) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack) : MigratorResult<ItemStack> {
		return MigratorResult.Replacement(subject.withType(newType))
	}
}
