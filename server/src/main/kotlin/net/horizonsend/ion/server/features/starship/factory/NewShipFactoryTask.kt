package net.horizonsend.ion.server.features.starship.factory

import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.task.MultiblockEntityTask
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactorySettings
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories.missingMaterialsCache
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.miscellaneous.registrations.ShipFactoryMaterialCosts
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getMoneyBalance
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.setNMSBlockData
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.starlegacy.javautil.SignUtils.SignData
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger

class NewShipFactoryTask(
	blueprint: Blueprint,
	settings: ShipFactorySettings,
	entity: ShipFactoryEntity,
	private val inventories: Set<ShipFactoryEntity.InventoryReference>,
	private val player: Player
) : ShipFactoryBlockProcessor(blueprint, settings, entity), MultiblockEntityTask<ShipFactoryEntity> {
	override val taskEntity: ShipFactoryEntity get() = entity
	private val missingMaterials = mutableMapOf<PrintItem, AtomicInteger>()
	private var skippedBlocks = 0

	override fun disable() {
		entity.disable()
	}

	override fun tick() {
		// Blocks that are gonna be printed
		val toPrint = mutableListOf<BlockKey>()

		// Per multiblock print limit
		val printLimit = entity.multiblock.blockPlacementsPerTick

		// All items available in inventories
		val availableItems = getAvailableItems()

		var availableCredits = player.getMoneyBalance()

		// Find the first blocks that can be placed with the available resources, up to the limit
		for (key: BlockKey in blockQueue) {
			if (toPrint.size >= printLimit) break
			val blockData = blockMap[key] ?: continue

			val vec3i = toVec3i(key)
			val worldBlockData = entity.world.getBlockData(vec3i.x, vec3i.y, vec3i.z)
			if (worldBlockData == blockData) continue

			val printItem = PrintItem[blockData]
			if (printItem == null) {
				IonServer.slF4JLogger.warn("$blockData has no print item!")
				continue
			}
			val requiredAmount = StarshipFactories.getRequiredAmount(blockData)

			// Already know it is not air
			if (!worldBlockData.material.isAir) {
				if (settings.markObstrcutedBlocksAsComplete) continue

				// Mark missing
				skippedBlocks++

				markItemMissing(printItem, requiredAmount)
				continue
			}

			val price = ShipFactoryMaterialCosts.getPrice(blockData)

			val anyAvailable = areResourcesAvailable(availableItems, printItem, requiredAmount) { result: Boolean, resources ->
				if (result && availableCredits >= price) {
					// Mark as available to print
					toPrint.add(key)

					// Decrement available resources
					resources.amount.addAndGet(-requiredAmount)

					// If it were missing before, and is not now, it was likely added to the inventory
					if (missingMaterials.containsKey(printItem)) {
						val atomic = missingMaterials[printItem]

						if (atomic != null) {
							if (atomic.get() >= requiredAmount) atomic.addAndGet(-requiredAmount)
						}
					}

					availableCredits -= price
					return@areResourcesAvailable
				}

				markItemMissing(printItem, requiredAmount)
			}

			if (!anyAvailable) {

				markItemMissing(printItem, requiredAmount)
			}
		}

		if (toPrint.isEmpty()) {
			if (missingMaterials.isNotEmpty()) {
				sendMissing(missingMaterials)
			}

			entity.disable()
		}

		Tasks.sync {
			printBlocks(availableItems, missingMaterials, toPrint)
		}

		if (blockMap.isEmpty()) {
			if (missingMaterials.isNotEmpty()) {
				sendMissing(missingMaterials)
			}

			player.success("Ship factory has finished printing.")
			entity.disable()
		}
	}

	override fun onEnable() {
		loadBlockQueue()
	}

	override fun onDisable() {
		println("Disabled task")
	}

	private fun printBlocks(availableItems: Map<PrintItem, AvailableItemInformation>, missingMaterials: MutableMap<PrintItem, AtomicInteger>, blocks: List<BlockKey>) {
		var consumedMoney = 0.0

		for (entry in blocks) {
			blockQueue.remove(entry)
			val blockData = blockMap.remove(entry) ?: continue
			val signData = signMap.remove(entry)

			val price = ShipFactoryMaterialCosts.getPrice(blockData)
			if (!player.hasEnoughMoney(consumedMoney + price)) continue
			consumedMoney += price

			val printItem = PrintItem[blockData] ?: continue
			val availabilityInfo = availableItems[printItem] ?: continue

			val references = availabilityInfo.references
			val missing = consumeItemFromReferences(references, StarshipFactories.getRequiredAmount(blockData))

			if (missing > 0) {
				missingMaterials.getOrPut(printItem) { AtomicInteger() }.addAndGet(missing)
				continue
			}

			// If all good, place the block
			placeBlock(entry, blockData, signData)
		}

		player.withdrawMoney(consumedMoney)
	}

	private fun placeBlock(location: BlockKey, data: BlockData, signData: SignData?) {
		val (x, y, z) = toVec3i(location)
		val world = entity.world
		val block = world.getBlockAt(x, y, z)

		world.setNMSBlockData(x, y, z, getRotatedBlockData(data))

		val state = block.state as? Sign
		if (state != null) {
			signData?.applyTo(state)
		}
	}

	private fun getAvailableItems(): Map<PrintItem, AvailableItemInformation> {
		val items = mutableMapOf<PrintItem, AvailableItemInformation>()

		for (inventoryReference in inventories) {
			for ((index, item: ItemStack?) in inventoryReference.inventory.contents.withIndex()) {
				if (item == null || item.type.isEmpty) continue
				val printItem = PrintItem(item)

				if (!inventoryReference.isAvailable(item)) {
					continue
				}

				val information = items.getOrPut(printItem) { AvailableItemInformation(AtomicInteger(), mutableListOf()) }

				information.amount.addAndGet(item.amount)
				information.references.add(ItemReference(inventoryReference.inventory, index))
			}
		}

		// If asked to leave one remaining, reduce the available items in each slot by 1 so they won't be consumed
//		if (settings.leaveItemRemaining) {
//			for ((_, information) in availableItems) {
//				if (information.amount.get() > 0) information.amount.decrementAndGet()
//			}
//		}

		return items
	}

	private fun areResourcesAvailable(
		availableItems: Map<PrintItem, AvailableItemInformation>,
		printItem: PrintItem,
		requiredAmount: Int,
		resultConsumer: (Boolean, AvailableItemInformation) -> Unit = { _, _ -> }
	): Boolean {
		val information = availableItems[printItem] ?: return false

		if (information.amount.get() < requiredAmount) {
			resultConsumer.invoke(false, information)
			return false
		}

		resultConsumer.invoke(true, information)
		return true
	}

	private data class AvailableItemInformation(val amount: AtomicInteger, val references: MutableList<ItemReference>)

	private fun sendMissing(missingMaterials: MutableMap<PrintItem, AtomicInteger>) {
		missingMaterialsCache[player.uniqueId] = missingMaterials.mapValues { it.value.get() }

		val sorted = missingMaterials.entries.toList().sortedBy { it.value.get() }

		player.userError("Missing Materials: ")

		player.sendMessage(
			formatPaginatedMenu(
			entries = sorted.size,
			command = "/shipfactory listmissing",
			currentPage = 1,
		) { index ->
			val (item, count) = sorted[index]

			return@formatPaginatedMenu ofChildren(
				item.toComponent(color = RED),
				text(": ", DARK_GRAY),
				text(count.get(), WHITE)
			)
		})
		if (skippedBlocks > 0) player.userError("$skippedBlocks were skipped due to being obstructed.")
		player.userError("Use <italic><underlined><click:run_command:/shipfactory listmissing all>/shipfactory listmissing all</click></italic> to list all missing materials in one message.")
	}

	private fun consumeItemFromReferences(references: Collection<ItemReference>, amount: Int): Int {
		var remaining = amount

		for (reference in references) {
			val item = reference.get() ?: continue
			val stackAmount = item.amount

			if (remaining >= stackAmount) {
				remaining -= stackAmount
				reference.inventory.setItem(reference.index, null)
			} else {
				val toRemove = minOf(stackAmount, remaining)
				item.amount -= toRemove
			}
		}

		return remaining
	}

	private fun markItemMissing(printItem: PrintItem, amount: Int): Int {
		return missingMaterials.getOrPut(printItem) { AtomicInteger() }.addAndGet(amount)
	}
}
