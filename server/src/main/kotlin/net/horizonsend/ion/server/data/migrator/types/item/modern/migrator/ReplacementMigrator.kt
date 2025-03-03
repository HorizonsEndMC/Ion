package net.horizonsend.ion.server.data.migrator.types.item.modern.migrator

import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.predicate.CustomItemsPredicate
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import org.bukkit.inventory.ItemStack

class ReplacementMigrator(vararg items: IonRegistryKey<CustomItem>) : CustomItemStackMigrator(CustomItemsPredicate(*Array(items.size) { items[it].key })) {
	private val items = listOf(*items)

	override fun performMigration(subject: ItemStack): MigratorResult<ItemStack> {
		val customItem = subject.customItem ?: return MigratorResult.Mutation()
		val ideal = customItem.constructItemStack(subject.amount)
		if (ideal.isSimilar(subject)) return MigratorResult.Mutation()

		return MigratorResult.Replacement(customItem.constructItemStack(subject.amount))
	}

	override fun registerTo(map: MutableMap<String, CustomItemStackMigrator>) {
		for (customItem in items) map[customItem.key] = this
	}
}
