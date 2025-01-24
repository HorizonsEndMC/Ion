package net.horizonsend.ion.server.features.transport.filters.manager

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.custom.blocks.filter.CustomFilterBlock
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.server.MinecraftServer
import org.bukkit.Material
import org.bukkit.block.CommandBlock
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

abstract class FilterManager(val manager: TransportManager<*>) {
	val filters = Long2ObjectOpenHashMap<FilterData<*>>()

	private val mutex = Any()

	fun getFilters(): Collection<FilterData<*>> {
		return filters.values
	}

	fun <T: Any> getFilters(type: FilterType<T>): Collection<FilterData<T>> {
		return filters.values
			.filter { data -> data.type == type }
			.filterIsInstance<FilterData<T>>()
	}

	fun getFilter(key: BlockKey): FilterData<*>? {
		return filters[key]
	}

	fun addFilter(key: BlockKey, data: FilterData<*>) {
		val local = manager.getLocalCoordinate(toVec3i(key))

		synchronized(mutex) {
			filters[toBlockKey(local)] = data
		}
	}

	fun <T : Any, D : FilterData<T>> registerFilter(key: BlockKey, block: CustomFilterBlock<T, D>): D {
		val data = block.createData(key)

		addFilter(key, data)

		return data
	}

	fun removeFilter(key: BlockKey) = synchronized(mutex) {
		val local = manager.getLocalCoordinate(toVec3i(key))
	}

	fun isFilterPresent(key: BlockKey): Boolean = synchronized(mutex) {
		return filters.containsKey(key)
	}

	abstract fun save()
	abstract fun load()

	companion object {
		fun save(commandBlock: CommandBlock, data: FilterData<*>) {
			val test = ItemStack(Material.DIRT).updatePersistentDataContainer {
				set(NamespacedKeys.FILTER_DATA, FilterData, data)
			}

			println(NbtUtils.structureToSnbt(CraftItemStack.asNMSCopy(test).save(MinecraftServer.getServer().registryAccess()) as CompoundTag))

			commandBlock.persistentDataContainer.set(NamespacedKeys.FILTER_DATA, FilterData, data)
			commandBlock.update()
		}
	}
}
