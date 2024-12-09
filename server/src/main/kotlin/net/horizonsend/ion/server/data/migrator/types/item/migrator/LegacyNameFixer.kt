package net.horizonsend.ion.server.data.migrator.types.item.migrator

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.aspect.PullNameMigrator
import net.horizonsend.ion.server.data.migrator.types.item.predicate.ItemMigratorPredicate
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.newCustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack

class LegacyNameFixer(private vararg val itemIdentifiers: String) : CustomItemStackMigrator(ItemMigratorPredicate { item ->
	val custom = item.customItem ?: return@ItemMigratorPredicate false
	val idealName = custom.constructItemStack().itemMeta.displayName() ?: return@ItemMigratorPredicate false
	val displayName = item.itemMeta.displayName() ?: return@ItemMigratorPredicate false
	if (idealName == displayName) return@ItemMigratorPredicate false
	if (displayName.children().isNotEmpty()) return@ItemMigratorPredicate false // New names
	if (displayName.decorations()[ITALIC] != TextDecoration.State.FALSE) return@ItemMigratorPredicate  false
	true
}) {
	override fun registerTo(map: MutableMap<String, CustomItemStackMigrator>) {
		map.putAll(itemIdentifiers.map { it to this })
	}

	override fun performMigration(subject: ItemStack): MigratorResult<ItemStack> {
		val customItem = subject.newCustomItem ?: return MigratorResult.Mutation()
		PullNameMigrator(customItem).migrate(subject)
		return MigratorResult.Mutation()
	}
}
