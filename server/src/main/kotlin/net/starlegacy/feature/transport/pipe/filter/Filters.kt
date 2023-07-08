package net.starlegacy.feature.transport.pipe.filter

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.IonComponent
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Hopper
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

object Filters : IonComponent() {
	private val cache: LoadingCache<FilterDataKey, FilterData> = CacheBuilder.newBuilder()
		.expireAfterWrite(1L, TimeUnit.MINUTES)
		.build(
			CacheLoader.from { key ->
				checkNotNull(key)
				val world = checkNotNull(Bukkit.getWorld(key.world))
				val block = world.getBlockAtKey(key.pos.toBlockKey())

				val state = block.getState(false) as Hopper

				val inventory = state.inventory
				val items: Set<FilterItemData> = getItemData(inventory)

				val face: BlockFace = (state.blockData as Directional).facing
				return@from FilterData(items, face)
			}
		)

	fun getItemData(inventory: Inventory): Set<FilterItemData> {
		val types = mutableSetOf<FilterItemData>()

		for (item: ItemStack? in inventory.contents!!) {
			val type = item?.type ?: continue

			if (type.isAir) {
				continue
			}

			types.add(createFilterItemData(item))
		}

		return types
	}

	fun createFilterItemData(item: ItemStack): FilterItemData {
		val material = item.type
		val itemMeta = item.itemMeta

		if (itemMeta.hasCustomModelData()) {
			return FilterItemData(material, itemMeta.customModelData)
		}

		return FilterItemData(material, null)
	}

	private fun getKey(world: World, x: Int, y: Int, z: Int) =
		FilterDataKey(world.uid, Vec3i(x, y, z))

	fun getCached(world: World, x: Int, y: Int, z: Int): FilterData? {
		return cache.getIfPresent(getKey(world, x, y, z))
	}

	fun cache(world: World, x: Int, y: Int, z: Int) {
		Tasks.checkMainThread()
		cache.get(getKey(world, x, y, z))
	}

	fun invalidate(world: World, x: Int, y: Int, z: Int) {
		cache.invalidate(getKey(world, x, y, z))
	}

	private fun invalidateBlock(block: Block) {
		invalidate(block.world, block.x, block.y, block.z)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockBreak(event: BlockBreakEvent) {
		val block = event.block
		invalidateBlock(block)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onInventoryEdit(event: InventoryCloseEvent) {
		val hopper = event.inventory.holder as? Hopper ?: return
		invalidateBlock(hopper.block)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPistonMove(event: BlockPistonExtendEvent) {
		event.blocks.forEach(::invalidateBlock)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPistonMove(event: BlockPistonRetractEvent) {
		event.blocks.forEach(::invalidateBlock)
	}
}
