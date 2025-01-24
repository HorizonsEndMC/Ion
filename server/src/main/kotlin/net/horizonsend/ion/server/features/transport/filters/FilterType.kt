package net.horizonsend.ion.server.features.transport.filters

import com.manya.pdc.minecraft.ItemStackDataType
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.FluidPersistentDataType
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

abstract class FilterType<T : Any>(val cacheType: CacheType, val identifier: String, val typeClass: Class<T>, val persistentDataType: PersistentDataType<*, T>) {
	fun store(pdc: PersistentDataContainer, data: Any) {
		if (!typeClass.isInstance(data)) return
		if (data is ItemStack && data.isEmpty) {
			return
		}

		@Suppress("UNCHECKED_CAST")
		pdc.set(NamespacedKeys.FILTER_ENTRY, persistentDataType, data as T)
	}

	fun retrieve(pdc: PersistentDataContainer): T? {
		return pdc.get(NamespacedKeys.FILTER_ENTRY, persistentDataType)
	}

	fun loadFilterEntries(
		primitive: List<PersistentDataContainer>,
		context: PersistentDataAdapterContext
	): MutableList<out FilterEntry<T>> {
		return primitive.mapTo(mutableListOf()) { entry ->
			@Suppress("UNCHECKED_CAST")
			FilterEntry.fromPrimitive(entry, context) as FilterEntry<T>
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun cast(entry: FilterEntry<*>): FilterEntry<T> = entry as FilterEntry<T>

	abstract fun toItem(entry: FilterEntry<T>): ItemStack?

	data object FluidType : FilterType<Fluid>(CacheType.FLUID, "FLUID", Fluid::class.java, FluidPersistentDataType) {
		override fun toItem(entry: FilterEntry<Fluid>): ItemStack? {
			val value = entry.value
			if (value == null) return null

			return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				.updateDisplayName(value.displayName)
				.updateLore(value.categories.map { it.toComponent() })
		}
	}

	data object ItemType : FilterType<ItemStack>(CacheType.ITEMS, "ITEMS", ItemStack::class.java, ItemStackDataType()) {
		override fun toItem(entry: FilterEntry<ItemStack>): ItemStack? = entry.value?.clone()
	}

	companion object {
		private val byId = mapOf("FLUID" to FluidType, "ITEMS" to ItemType)

		operator fun get(identifier: String): FilterType<*> = byId[identifier]!!

		private val byClass: Map<KClass<*>, FilterType<*>> = mapOf(Fluid::class to FluidType, ItemStack::class to ItemType)

		operator fun get(clazz: KClass<*>): FilterType<*> = byClass[clazz]!!
	}
}
