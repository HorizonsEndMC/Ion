package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.transport.filters.FilterMeta.EmptyFilterMeta
import net.horizonsend.ion.server.features.transport.filters.FilterMeta.ItemFilterMeta
import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.FluidPersistentDataType
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

abstract class FilterType<T : Any, M : FilterMeta>(
	val cacheType: CacheType,
	val identifier: String,
	val typeClass: Class<T>,
	val persistentDataType: PersistentDataType<*, T>,
	val metaType: PDCSerializers.RegisteredSerializer<M>
) {
	fun castAndMatch(data: Any, isWhitelist: Boolean, entry: FilterEntry<in T, in M>): Boolean = matches(cast(data), isWhitelist, entry)
	abstract fun matches(data: T, isWhitelist: Boolean, entry: FilterEntry<in T, in M>): Boolean

	fun store(pdc: PersistentDataContainer, data: Any?) {
		if (!typeClass.isInstance(data)) return
		if (data is ItemStack && data.isEmpty) {
			return
		}

		@Suppress("UNCHECKED_CAST")
		data?.let { pdc.set(NamespacedKeys.FILTER_ENTRY, persistentDataType, it as T) }
	}

	fun retrieveValue(pdc: PersistentDataContainer): T? {
		return pdc.get(NamespacedKeys.FILTER_ENTRY, persistentDataType)
	}

	fun retrieveMeta(container: MetaDataContainer<*, *>): M {
		@Suppress("UNCHECKED_CAST")
		return container.data as M
	}

	abstract fun buildEmptyMeta(): M

	fun buildEmptyEntry(): FilterEntry<T, M> {
		return FilterEntry<T, M>(null, this, buildEmptyMeta())
	}

	fun loadFilterEntries(
		primitive: List<PersistentDataContainer>,
		context: PersistentDataAdapterContext
	): MutableList<out FilterEntry<T, M>> {
		return primitive.mapTo(mutableListOf()) { entry ->
			@Suppress("UNCHECKED_CAST")
			FilterEntry.fromPrimitive(entry, context) as FilterEntry<T, M>
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun cast(entry: FilterEntry<*, *>): FilterEntry<T, M> = entry as FilterEntry<T, M>

	@Suppress("UNCHECKED_CAST")
	fun cast(data: FilterData<*, *>): FilterData<T, M> = data as FilterData<T, M>

	fun cast(data: Any): T = data as T

	abstract fun toItem(entry: FilterEntry<T, M>): ItemStack?

	data object FluidType : FilterType<Fluid, EmptyFilterMeta>(
		cacheType = CacheType.FLUID,
		identifier = "FLUID",
		typeClass = Fluid::class.java,
		persistentDataType = FluidPersistentDataType,
		metaType = PDCSerializers.EMPTY_FILTER_META
	) {
		override fun toItem(entry: FilterEntry<Fluid, EmptyFilterMeta>): ItemStack? {
			val value = entry.value
			if (value == null) return null

			return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				.updateDisplayName(value.displayName)
				.updateLore(value.categories.map { it.toComponent() })
		}

		override fun buildEmptyMeta(): EmptyFilterMeta = EmptyFilterMeta

		override fun matches(
			data: Fluid,
			isWhitelist: Boolean,
			entry: FilterEntry<in Fluid, in EmptyFilterMeta>,
		): Boolean {
			val matched = entry.value ?: return true
			return matched == data
		}
	}

	data object ItemType : FilterType<ItemStack, ItemFilterMeta>(
		cacheType = CacheType.ITEMS,
		identifier = "ITEMS",
		typeClass = ItemStack::class.java,
		persistentDataType = ItemSerializer,
		metaType = PDCSerializers.ITEM_FILTER_META
	) {
		override fun toItem(entry: FilterEntry<ItemStack, ItemFilterMeta>): ItemStack? = entry.value?.clone()

		override fun buildEmptyMeta(): ItemFilterMeta = ItemFilterMeta()

		override fun matches(
			data: ItemStack,
			isWhitelist: Boolean,
			entry: FilterEntry<in ItemStack, in ItemFilterMeta>,
		): Boolean {
			if (entry.value !is ItemStack) return true

			return (entry.metaData as ItemFilterMeta).filterMethod.matches(data, entry.value as ItemStack) == isWhitelist
		}
	}

	companion object : IonServerComponent() {
		private val byId = mapOf("FLUID" to FluidType, "ITEMS" to ItemType)

		operator fun get(identifier: String): FilterType<*, *> = byId[identifier] ?: throw NoSuchElementException("Filter type $identifier not found")

		private val byClass: Map<KClass<*>, FilterType<*, *>> = mapOf(Fluid::class to FluidType, ItemStack::class to ItemType)

		operator fun get(clazz: KClass<*>): FilterType<*, *> = byClass[clazz] ?: throw NoSuchElementException("Filter type for ${clazz.simpleName} not found")
	}
}
