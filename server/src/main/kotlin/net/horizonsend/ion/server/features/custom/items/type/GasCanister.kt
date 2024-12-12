package net.horizonsend.ion.server.features.custom.items.type

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.GAS_STORAGE
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.GasStorage
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.gas.type.Gas
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class GasCanister(
	identifier: String,

	val model: String,
	displayName: Component,
	private val gasSupplier: Supplier<Gas>
) : CustomItem(
	identifier,
	displayName,
	ItemFactory.unStackableCustomItem(model)
) {
	val gas get() = gasSupplier.get()
	val maximumFill = gas.configuration.maxStored

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(GAS_STORAGE, GasStorage(maximumFill, true, gasSupplier))
	}

	fun createWithFill(fill: Int): ItemStack {
		val new = constructItemStack()
		getComponent(GAS_STORAGE).setFill(new, this, fill)
		return new
	}

	fun setFill(itemStack: ItemStack, newValue: Int) {
		if (newValue <= 0) return replaceWithEmpty(itemStack)
		getComponent(GAS_STORAGE).setFill(itemStack, this, newValue)
	}

	fun replaceWithEmpty(itemStack: ItemStack) {
		itemStack.itemMeta = CustomItemRegistry.GAS_CANISTER_EMPTY.constructItemStack().itemMeta
	}

	fun getFill(itemStack: ItemStack): Int = getComponent(GAS_STORAGE).getFill(itemStack)

	/** Replaces the gas canister with an empty one **/
	fun empty(itemStack: ItemStack, inventory: Inventory) {
		val empty = CustomItemRegistry.GAS_CANISTER_EMPTY.constructItemStack()

		val firstMatching = inventory.all(itemStack).keys.firstOrNull() ?: return // Shouldn't happen

		inventory.setItem(firstMatching, empty)

		(inventory.holder as? Player)?.updateInventory()
	}
}
