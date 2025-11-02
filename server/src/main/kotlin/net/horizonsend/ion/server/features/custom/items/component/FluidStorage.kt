package net.horizonsend.ion.server.features.custom.items.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

class FluidStorage(val capacity: Double, val restriction: FluidRestriction) : CustomItemComponent, LoreManager {
	override val priority: Int = 100

	override fun decorateBase(baseItem: ItemStack, customItem: CustomItem) {
		setContents(baseItem, customItem, FluidStack.empty())
	}

	override fun getAttributes(baseItem: ItemStack): Iterable<CustomItemAttribute> = emptyList()

	override fun shouldIncludeSeparator(): Boolean = false
	override fun getLines(customItem: CustomItem, itemStack: ItemStack): List<Component> {
		val contents = getContents(itemStack)

		return listOf(
			contents.getDisplayName().itemLore,
			ofChildren(text(contents.amount, AQUA), text(" / ", GRAY), text(capacity, AQUA)).itemLore
		)
	}

	fun getContents(itemStack: ItemStack): FluidStack {
		return itemStack.persistentDataContainer.getOrDefault(storageKey, FluidStack, FluidStack.empty())
	}

	fun getRemainingRoom(itemStack: ItemStack): Double {
		return capacity - getContents(itemStack).amount
	}

	fun canStore(itemStack: ItemStack, newContents: FluidStack): Boolean {
		val contents = getContents(itemStack)

		if (contents.isEmpty()) return true
		if (contents.type != newContents.type) return false
		return restriction.canAdd(newContents)
	}

	fun setContents(itemStack: ItemStack, customItem: CustomItem, newContents: FluidStack) {
		itemStack.updatePersistentDataContainer { set(storageKey, FluidStack, newContents) }
		customItem.refreshLore(itemStack)
	}

	fun canAdd(itemStack: ItemStack, type: IonRegistryKey<FluidType, out FluidType>): Boolean {
		val contents = getContents(itemStack)

		if (contents.isEmpty()) return true
		if (contents.type != type) return false

		return restriction.canAdd(type)
	}

	fun hasRoomFor(itemStack: ItemStack, newContents: FluidStack): Boolean {
		val contents = getContents(itemStack)
		return newContents.amount + contents.amount <= capacity
	}

	fun addContents(itemStack: ItemStack, customItem: CustomItem, newContents: FluidStack, location: Location): Double {
		val contents = getContents(itemStack)

		if (newContents.isEmpty()) {
			throw IllegalArgumentException("Cannot add empty fluid stack!")
		}

		if (contents.isEmpty()) {
			setContents(itemStack, customItem, newContents)
			return 0.0
		}

		val newQuantity = minOf(getRemainingRoom(itemStack), newContents.amount)
		val toAdd = newContents.asAmount(newQuantity)

		val new = contents.clone()
		new.combine(toAdd, location)
		setContents(itemStack, customItem, new)

		return newContents.amount - newQuantity
	}

	/** Returns amount not removed */
	fun removeAmount(itemStack: ItemStack, customItem: CustomItem, amount: Double): Double {
		val contents = getContents(itemStack)

		val previousAmount = contents.amount
		val toRemove = minOf(amount, contents.amount)

		val notRemoved = amount - toRemove

		contents.amount -= toRemove
		setContents(itemStack, customItem, contents)

		return notRemoved
	}

	companion object {
		private val storageKey = NamespacedKeys.key("fluid_storage")
	}
}
