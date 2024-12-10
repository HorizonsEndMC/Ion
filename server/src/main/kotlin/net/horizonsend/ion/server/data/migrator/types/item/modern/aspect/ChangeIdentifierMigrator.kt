package net.horizonsend.ion.server.data.migrator.types.item.modern.aspect

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ChangeIdentifierMigrator(val old: String, val new: String) : ItemAspectMigrator {
	override fun migrate(subject: ItemStack): MigratorResult<ItemStack> {
		subject.updateMeta { it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, new) }
		return MigratorResult.Mutation()
	}
}
