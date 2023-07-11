package net.starlegacy.feature.transport.pipe

import net.horizonsend.ion.server.IonServerComponent
import net.starlegacy.feature.machine.GeneratorFuel
import net.starlegacy.feature.transport.Extractors
import net.starlegacy.feature.transport.pipe.filter.FilterData
import net.starlegacy.feature.transport.pipe.filter.FilterItemData
import net.starlegacy.feature.transport.pipe.filter.Filters
import net.starlegacy.feature.transport.transportConfig
import net.starlegacy.util.ADJACENT_BLOCK_FACES
import net.starlegacy.util.MATERIALS
import net.starlegacy.util.Tasks
import net.horizonsend.ion.server.miscellaneous.Vec3i
import net.starlegacy.util.blockKey
import net.starlegacy.util.chunkKey
import net.starlegacy.util.chunkKeyX
import net.starlegacy.util.chunkKeyZ
import net.starlegacy.util.getBlockTypeSafe
import net.starlegacy.util.getStateIfLoaded
import net.starlegacy.util.isGlass
import net.starlegacy.util.isGlassPane
import net.starlegacy.util.isStainedGlass
import net.starlegacy.util.isStainedGlassPane
import net.starlegacy.util.randomEntry
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.EnumMap
import java.util.EnumSet
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object Pipes : IonServerComponent() {
	private lateinit var thread: ExecutorService
	private val pending = AtomicInteger(0)
	private val inventoryCheckTasks = ConcurrentLinkedQueue<CheckInventoryTask>()

	override fun onEnable() {
		thread = Executors.newSingleThreadExecutor(Tasks.namedThreadFactory("sl-transport-pipes"))

		schedule()
	}

	private fun schedule() {
		val interval = transportConfig.pipes.inventoryCheckInterval

		Tasks.syncRepeat(delay = interval, interval = interval) {
			val start = System.nanoTime()

			var drained = 0

			val maxTime = TimeUnit.MILLISECONDS.toNanos(transportConfig.pipes.inventoryCheckMaxTime)

			while (!inventoryCheckTasks.isEmpty() && System.nanoTime() - start < maxTime) {
				val task = inventoryCheckTasks.poll()
				pending.decrementAndGet()
				drained++

				val data = task.data

				if (!data.world.isChunkLoaded(task.x shr 4, task.z shr 4) || task.inventories.any {
					!data.world.isChunkLoaded((task.x + it.modX) shr 4, (task.z + it.modZ) shr 4)
				}
				) {
					queueNotBusy(data.extractor)
					continue
				}

				execute(task)
			}

			val pendingNow = pending.get()
			if (pendingNow > 10_000) {
				log.warn("$pendingNow tasks pending, $drained drained!")
				if (pendingNow > 20_000) {
					log.warn("Clearing queue!")
					val worldCounts = mutableMapOf<World, AtomicInteger>()
					val chunkCounts = mutableMapOf<Pair<World, Long>, AtomicInteger>()
					while (!inventoryCheckTasks.isEmpty()) {
						val task = inventoryCheckTasks.poll()
						val data = task.data
						Extractors.BUSY_PIPE_EXTRACTORS.remove(data.extractor)
						pending.decrementAndGet()
						val world = data.world
						worldCounts.getOrPut(world, ::AtomicInteger).incrementAndGet()
						val chunkKey = chunkKey(task.x shr 4, task.z shr 4)
						chunkCounts.getOrPut(world to chunkKey, ::AtomicInteger).incrementAndGet()
					}
					for ((world, count) in worldCounts.entries.sortedByDescending { it.value.get() }) {
						log.warn("Cleared ${count.get()} in ${world.name}")
					}
					for ((key, count) in chunkCounts.entries.sortedByDescending { it.value.get() }) {
						val (world, chunkKey) = key
						val cx = chunkKeyX(chunkKey)
						val cz = chunkKeyZ(chunkKey)
						log.warn("Cleared ${count.get()} in ${world.name} at $cx $cz")
					}
				}
			}
		}
	}

	override fun onDisable() {
		thread.shutdown()
	}

	private val inventoryTypes: EnumSet<Material> = EnumSet.of(
		Material.CHEST,
		Material.TRAPPED_CHEST,
		Material.BARREL,
		Material.FURNACE,
		Material.DISPENSER,
		Material.DROPPER
	)

	fun isPipedInventory(material: Material): Boolean = inventoryTypes.contains(material)

	fun isAnyPipe(material: Material): Boolean = material.isGlass || material.isGlassPane

	private fun isDirectionalPipe(material: Material): Boolean = material.isGlassPane

	private fun isColoredPipe(material: Material): Boolean = material.isStainedGlass || material.isStainedGlassPane

	/**
	 * Starts a pipe chain that continues until it goes too long,
	 * finds an inventory to put the items in, or hits a dead end.
	 *
	 * @param world The world the extractor and inventory were in
	 * @param x The X-coordinate of the extractor
	 * @param y The Y-coordinate of the extractor
	 * @param z The Z-coordinate of the extractor
	 * @param dir The direction from the extractor to the pipe
	 * @param source The coordinates of the inventory block extracted from
	 */
	fun startPipeChain(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        dir: BlockFace,
        source: Vec3i,
        extractor: Vec3i,
        filteredItems: Set<FilterItemData>
	) {
		thread.submit {
			step(PipeChainData(source, extractor, world, x, y, z, dir, 0, filteredItems))
		}
	}

	/**
	 * This is the chained check for pipe glass blocks.
	 *
	 * It starts at the extractor (crafting table),
	 * moving into a randomly selected adjacent pipe (glass block).
	 *
	 * Then, it keeps running the step logic on each glass block it runs through,
	 * until it ends up at an inventory block that can fit it,
	 * or it runs out of blocks to move into, or it hits the max distance.
	 */
	private fun step(data: PipeChainData) {
		var ended = true

		try {
			check(!Bukkit.isPrimaryThread())

			// put a cap on the distance in order to avoid extremely long running pipes
			if (data.distance > transportConfig.pipes.maxDistance) {
				return
			}

			val nx = data.x + data.direction.modX
			val ny = data.y + data.direction.modY
			val nz = data.z + data.direction.modZ

			val nextType: Material = getBlockTypeSafe(data.world, nx, ny, nz) ?: return

			// if the next type is not even a pipe, end the chain
			if (!isAnyPipe(nextType)) {
				return
			}

			val sidePipes = mutableListOf<BlockFace>()
			val sideInventories = mutableListOf<BlockFace>()
			val filters = mutableMapOf<BlockFace, FilterData>()
			val filterItems = mutableMapOf<BlockFace, Set<FilterItemData>>()

			val reverse = data.direction.oppositeFace

			adjacentLoop@
			for (sideFace: BlockFace in ADJACENT_BLOCK_FACES) {
				// avoid going backwards
				if (sideFace == reverse) {
					continue
				}

				val sideX: Int = nx + sideFace.modX
				val sideY: Int = ny + sideFace.modY
				val sideZ: Int = nz + sideFace.modZ

				val adjacentType: Material = getBlockTypeSafe(data.world, sideX, sideY, sideZ)
					?: continue // skip if not loaded

				when {
					// if it's a pipe
					isAnyPipe(adjacentType) -> {
						if (canPipesTransfer(nextType, adjacentType)) {
							sidePipes.add(sideFace)
						}
					}

					// filter
					adjacentType == Material.HOPPER -> {
						val filterData = Filters.getCached(data.world, sideX, sideY, sideZ)

						if (filterData == null) {
							cacheFilterAndReschedule(data, sideX, sideY, sideZ)
							ended = false
							return
						}

						handleFilter(data, filterData, sideX, sideY, sideZ, sidePipes, sideFace, filters, filterItems)
					}

					// if it's an inventory
					isPipedInventory(adjacentType) && blockKey(sideX, sideY, sideZ) != data.source.toBlockKey() -> {
						// check if it actually fits later
						sideInventories.add(sideFace)
					}

					// skip if anything else
					else -> {
						continue@adjacentLoop
					}
				}
			}

			when {
				sideInventories.isNotEmpty() -> {
					sideInventories.shuffle()
					data.distance++

					val task = CheckInventoryTask(data, nx, ny, nz, sideInventories, sidePipes, filters, filterItems)
					inventoryCheckTasks.offer(task)
					pending.incrementAndGet()
					ended = false
				}

				sidePipes.isNotEmpty() -> {
					handlePipes(data, nextType, sidePipes, nx, ny, nz, filters, filterItems)
					ended = false
				}

				else -> {
					return // leaves ended as true
				}
			}
		} finally {
			if (ended) {
				queueNotBusy(data.extractor)
			}
		}
	}

	private fun handlePipes(
		data: PipeChainData,
		nextType: Material,
		sidePipes: List<BlockFace>,
		nx: Int,
		ny: Int,
		nz: Int,
		filters: MutableMap<BlockFace, FilterData>,
		filterItems: MutableMap<BlockFace, Set<FilterItemData>>
	) {
		val sidePipeFace: BlockFace = pickDirection(nextType, sidePipes, data.direction)
		data.x = nx
		data.y = ny
		data.z = nz
		data.direction = sidePipeFace
		val filterData = filters[sidePipeFace]
		if (filterData != null) {
			data.x += sidePipeFace.modX
			data.y += sidePipeFace.modY
			data.z += sidePipeFace.modZ
			data.direction = filterData.face
			data.accumulatedFilter = filterItems.getValue(sidePipeFace)
		}
		data.distance++
		step(data)
	}

	private fun handleFilter(
		data: PipeChainData,
		filterData: FilterData,
		sideX: Int,
		sideY: Int,
		sideZ: Int,
		sidePipes: MutableList<BlockFace>,
		sideFace: BlockFace,
		sideFilters: MutableMap<BlockFace, FilterData>,
		sideFilterItems: MutableMap<BlockFace, Set<FilterItemData>>
	) {
		val combinedFilter = data.accumulatedFilter.intersect(filterData.items)

		if (combinedFilter.isEmpty()) {
			return
		}

		val pipeX = sideX + filterData.face.modX
		val pipeY = sideY + filterData.face.modY
		val pipeZ = sideZ + filterData.face.modZ

		val pipeType = getBlockTypeSafe(data.world, pipeX, pipeY, pipeZ) ?: return

		if (!isAnyPipe(pipeType)) {
			return
		}

		sidePipes.add(sideFace)
		sideFilters[sideFace] = filterData
		sideFilterItems[sideFace] = combinedFilter
	}

	private fun cacheFilterAndReschedule(
		data: PipeChainData,
		sideX: Int,
		sideY: Int,
		sideZ: Int
	) {
		Tasks.sync {
			Filters.cache(data.world, sideX, sideY, sideZ)
			thread.submit {
				step(data)
			}
		}
	}

	data class CheckInventoryTask(
		val data: PipeChainData,
		val x: Int,
		val y: Int,
		val z: Int,
		val inventories: List<BlockFace>,
		val pipes: List<BlockFace>,
		val filters: MutableMap<BlockFace, FilterData>,
		val filterItems: MutableMap<BlockFace, Set<FilterItemData>>
	)

	private fun execute(task: CheckInventoryTask) {
		var ended = true

		val data = task.data

		try {
			// get the source inventory's block state, IF it's still loaded.
			val sourceInventoryBlock = getStateIfLoaded(data.world, data.source.x, data.source.y, data.source.z)
				?: return

			// make sure it's still one of the inventories that pipes extract from
			if (!isPipedInventory(sourceInventoryBlock.type)) {
				return
			}

			val sourceInventory: Inventory = (sourceInventoryBlock as InventoryHolder).inventory

			// go through all the inventories until there's no more items in the source inventory
			destinationLoop@
			for (face: BlockFace in task.inventories) {
				val dest = getStateIfLoaded(data.world, task.x + face.modX, task.y + face.modY, task.z + face.modZ)
					?: continue@destinationLoop

				if (!isPipedInventory(dest.type)) {
					continue
				}

				val destinationInventory: Inventory = (dest as InventoryHolder).inventory

				// ensure we're not attempting to deliver to the same inventory
				if (destinationInventory.location == sourceInventory.location) {
					continue@destinationLoop
				}

				var remainingItemStacks = 0

				val items: Iterable<IndexedValue<ItemStack?>> = when (sourceInventory) {
					// for furnaces, only remove from result slot
					is FurnaceInventory -> {
						val resultSlot = 2

						val result: ItemStack = sourceInventory.getItem(resultSlot)
							?: return // if there is no result item, there is nothing to extract from this inventory

						setOf(IndexedValue(resultSlot, result))
					}

					// move all the items by default
					else -> sourceInventory.contents!!.withIndex()
				}

				for ((index: Int, item: ItemStack?) in items) {
					if (item == null) {
						continue
					}

					if (!data.accumulatedFilter.contains(Filters.createFilterItemData(item))) {
						continue
					}

					val result: Int = when (destinationInventory) {
						is FurnaceInventory -> handleFurnaceMovement(item, destinationInventory)
						else -> destinationInventory.addItem(item).values.firstOrNull()?.amount ?: 0
					}

					if (result == 0) {
						// no items remaining
						sourceInventory.setItem(index, null)
					} else {
						// if the amount is different, update the item in the original slot
						if (result != item.amount) {
							sourceInventory.setItem(index, item.clone().apply { amount = result })
						}

						// only part of the item was moved, some items still remain
						remainingItemStacks++
					}
				}

				// if it's still 0, that means there are no more items in the source chest that are not air.
				// this is because remainingItemStacks is only incremented
				// when an item didn't fit in the destination inv
				if (remainingItemStacks == 0) {
					return
				}
			}

			// If we made it here, there were either no acceptable inventories
			// from the inventories parameter to accept the items,
			// or some of the items were put in inventories but there are more
			// So, we should continue the pipe chain!

			val pipeType: Material = getBlockTypeSafe(data.world, task.x, task.y, task.z)
				?: return // if it's null the pipe's not loaded

			val acceptablePipes: List<BlockFace> = task.pipes.filter { face ->
				val pipeX = task.x + face.modX
				val pipeY = task.y + face.modY
				val pipeZ = task.z + face.modZ
				val type: Material = getBlockTypeSafe(data.world, pipeX, pipeY, pipeZ)
					?: return@filter false

				if (type == Material.HOPPER) {
					return@filter true
				}

				return@filter canPipesTransfer(originType = pipeType, otherType = type)
			}

			if (acceptablePipes.isEmpty()) {
				return // something happened with those pipes, they are no longer valid
			}

			thread.submit {
				handlePipes(data, pipeType, acceptablePipes, task.x, task.y, task.z, task.filters, task.filterItems)
			}
			ended = false
		} finally {
			if (ended) {
				queueNotBusy(data.extractor)
			}
		}
	}

	private fun queueNotBusy(extractor: Vec3i) {
		Tasks.syncDelay(5) {
			Extractors.BUSY_PIPE_EXTRACTORS.remove(extractor)
		}
	}

	private fun pickDirection(type: Material, adjacentPipes: List<BlockFace>, direction: BlockFace): BlockFace = when {
		// go straight if possible when directional
		isDirectionalPipe(type) && adjacentPipes.contains(direction) -> direction

		// normally, pick a random direction to go in
		else -> adjacentPipes.randomEntry()
	}

	private val colorMap = EnumMap(
		MATERIALS.filter { it.isGlass || it.isGlassPane }.associateWith {
			return@associateWith when {
				it == Material.GLASS_PANE -> Material.GLASS
				it.isStainedGlassPane -> Material.getMaterial(it.name.removeSuffix("_PANE"))!!
				else -> it
			}
		}
	)

	private fun canPipesTransfer(originType: Material, otherType: Material): Boolean {
		return isAnyPipe(otherType) && // it has to be any of the valid pipe types
			(isColoredPipe(originType) == isColoredPipe(otherType)) && // both are either colored pipes or not
			colorMap[originType] == colorMap[otherType]
	}

	/**
	 * This method will move the item to the appropriate slot of the furnace.
	 *
	 * If it is a fuel item, it will go in the fuel slot, else it will go in the smelting slot.
	 * It should replicate vanilla hand item insertion for the most part.
	 */
	private fun handleFurnaceMovement(itemStack: ItemStack, destination: FurnaceInventory): Int {
		val toSlot = when {
			destination.smelting?.type == Material.PRISMARINE_CRYSTALS -> 1 // Smelting has crystals, put it in fuel
			destination.fuel?.type == Material.PRISMARINE_CRYSTALS -> 0 // Fuel has crystals, put it in smelting
			itemStack.type.isFuel || GeneratorFuel.getFuel(itemStack) != null -> 1 // slot 1 - fuel
			else -> 0 // slot 0 - smelting
		}

		val current: ItemStack? = destination.getItem(toSlot)

		val itemAmount: Int = itemStack.amount

		when {
			// if it's a free space, just put the item in
			current == null || current.type == Material.AIR -> {
				destination.setItem(toSlot, itemStack)
				return 0
			}
			// if it's similar attempt to merge
			current.isSimilar(itemStack) -> {
				val maxAmount: Int = current.maxStackSize
				val freeSpace: Int = maxAmount - current.amount
				when {
					// there is no space, just don't move the item
					freeSpace == 0 -> return itemAmount
					// if the free space is less than the item amount, move as much as possible
					freeSpace < itemAmount -> {
						current.amount = maxAmount

						return itemAmount - freeSpace
					}
					// there's space to move the whole item if the free space is >= the item's amount
					freeSpace >= itemAmount -> {
						current.amount += itemAmount
						return 0
					}
					// this shouldn't even be possible?
					else -> error("Unsupported furnace movement! Free space: $freeSpace, item amount: $itemAmount")
				}
			}
			// if it is not the same type of item, it cannot stack, do not attempt to move
			else -> return itemAmount
		}
	}
}
