package net.horizonsend.ion.server.data.migrator.types.item.migrator

import io.papermc.paper.datacomponent.DataComponentType.Valued
import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.server.data.migrator.types.item.MigratorResult
import net.horizonsend.ion.server.data.migrator.types.item.aspect.ChangeIdentifierMigrator
import net.horizonsend.ion.server.data.migrator.types.item.aspect.ChangeTypeMigrator
import net.horizonsend.ion.server.data.migrator.types.item.aspect.CustomNameMigrator
import net.horizonsend.ion.server.data.migrator.types.item.aspect.ItemAspectMigrator
import net.horizonsend.ion.server.data.migrator.types.item.aspect.ItemComponentMigrator
import net.horizonsend.ion.server.data.migrator.types.item.aspect.PullLoreMigrator
import net.horizonsend.ion.server.data.migrator.types.item.aspect.SetLoreMigrator
import net.horizonsend.ion.server.data.migrator.types.item.predicate.ItemMigratorPredicate
import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.HORIZONSEND_NAMESPACE
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class AspectMigrator private constructor(
	val customItem: NewCustomItem,
	predicate: ItemMigratorPredicate,
	private val aspects: Set<ItemAspectMigrator>,
	private val additionalIdentifiers: Set<String> = setOf()
) : CustomItemStackMigrator(predicate) {
	override fun registerTo(map: MutableMap<String, CustomItemStackMigrator>) {
		map[customItem.identifier] = this
		map.putAll(additionalIdentifiers.map { it to this })
	}

	override fun performMigration(subject: ItemStack): MigratorResult<ItemStack> {
		val iterator = aspects.iterator()
		var item = subject
		var replaced = false

		while (iterator.hasNext()) {
			val migrator = iterator.next()
			println("Performing ${migrator::class.java} to ${subject.type}")
			val result = migrator.migrate(item)

			if (result !is MigratorResult.Replacement) continue
			item = result.new
			replaced = true
		}

		return if (replaced) MigratorResult.Replacement(item) else MigratorResult.Mutation()
	}

	class Builder(private val customItem: NewCustomItem) {
		private val aspects: MutableSet<ItemAspectMigrator> = mutableSetOf()
		private val additionalIdentifiers: MutableSet<String> = mutableSetOf()

		private var predicate: ItemMigratorPredicate = ItemMigratorPredicate.True

		fun addAdditionalIdentifier(identifier: String): Builder {
			this.additionalIdentifiers.add(identifier)
			return this
		}

		fun setPredicate(itemMigratorPredicate: ItemMigratorPredicate): Builder {
			this.predicate = itemMigratorPredicate
			return this
		}

		fun changeIdentifier(old: String, new: String): Builder {
			aspects.add(ChangeIdentifierMigrator(old, new))
			return this
		}

		fun setItemMaterial(newMaterial: Material): Builder {
			aspects.add(ChangeTypeMigrator(newMaterial))
			return this
		}

		fun <T : Any> setDataComponent(type: Valued<T>, value: T): Builder {
			aspects.add(ItemComponentMigrator(type, value))
			return this
		}

		fun setLore(lore: List<Component>): Builder {
			aspects.add(SetLoreMigrator(lore))
			return this
		}

		fun pullLore(from: NewCustomItem): Builder {
			aspects.add(PullLoreMigrator(from))
			return this
		}

		fun setCustomName(new: Component): Builder {
			aspects.add(CustomNameMigrator(new))
			return this
		}

		fun pullName(from: NewCustomItem): Builder {
			aspects.add(PullLoreMigrator(from))
			return this
		}

		fun setModel(customModel: String): Builder {
			return setDataComponent(DataComponentTypes.ITEM_MODEL, Key.key(HORIZONSEND_NAMESPACE, customModel))
		}

		fun build(): AspectMigrator = AspectMigrator(customItem, predicate, aspects, additionalIdentifiers)
	}

	companion object {
		fun builder(customItem: NewCustomItem): Builder {
			return Builder(customItem)
		}
	}
}
