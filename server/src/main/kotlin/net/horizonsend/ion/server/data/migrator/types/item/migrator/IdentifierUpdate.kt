package net.horizonsend.ion.server.data.migrator.types.item.migrator

import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.predicate.CustomItemPredicate
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class IdentifierUpdate(val old: String, val new: String) : CustomItemStackMigrator(CustomItemPredicate(old)) {
	override fun registerTo(map: MutableMap<String, CustomItemStackMigrator>) {
		map[old] = this
	}

	override fun performMigration(subject: ItemStack): MigratorResult<ItemStack> {
		subject.updateMeta { it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, new) }
		return MigratorResult.Mutation()
	}
}
