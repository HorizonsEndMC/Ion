package net.horizonsend.ion.server.features.starship.factory

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.miscellaneous.registrations.ShipFactoryMaterialCosts
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.setNMSBlockData
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.javautil.SignUtils
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class StarshipFactoryPrinter(
	private val world: World,
	private val inventory: Inventory,
	private val blocks: Long2ObjectOpenHashMap<BlockData>,
	private val signs: Long2ObjectOpenHashMap<SignUtils.SignData>,
	private var availableCredits: Double = 0.0
) {
	private val availableItems = mutableMapOf<PrintItem, Int>()

	private val usedItems = mutableMapOf<PrintItem, Int>()

	var usedCredits = 0.0
	val missingItems = mutableMapOf<PrintItem, Int>()
	var missingCredits = 0.0

	private val queue = Long2ObjectOpenHashMap<BlockState>()

	fun print() {
		calculateMaterialCounts()
		processBlocks()
		placeBlocks()
		removeItems()
	}

	private fun calculateMaterialCounts() {
		countMaterials()
		ignoreOneItemOfEachMaterial()
	}

	private fun countMaterials() {
		for (item: ItemStack? in inventory.contents) {
			if (item == null || item.type.isAir) {
				continue
			}

			val printItem = PrintItem(item)

			val oldAmount = availableItems.getOrDefault(printItem, 0)
			availableItems[printItem] = oldAmount + item.amount
		}
	}

	private fun ignoreOneItemOfEachMaterial() {
		for ((material, count) in availableItems) {
			if (count > 0) {
				availableItems[material] = count - 1
			}
		}
	}

	private fun processBlocks() {
		for ((key, data) in blocks) {
			processBlock(key, data)
		}
	}

	private fun processBlock(key: Long, data: BlockData) {
		val x = blockKeyX(key)
		val y = blockKeyY(key)
		val z = blockKeyZ(key)
		val oldData = getBlockDataSafe(world, x, y, z) ?: return

		if (!oldData.material.isAir) {
			return
		}

		if (!tryPrintBlock(data)) {
			return
		}

		queue[key] = data.nms
	}

	private fun tryPrintBlock(data: BlockData): Boolean {
		val item = PrintItem[data] ?: return false
		val amount = StarshipFactories.getRequiredAmount(data)

		val count = getAvailable(item)

		if (count < amount) {
			incrementMissing(item, amount)
			return false
		}

		if (availableCredits < ShipFactoryMaterialCosts.getPrice(data) && ConfigurationFiles.featureFlags().economy) return false

		decrementAvailable(item, count, amount)
		incrementUsed(item, amount)
		return true
	}

	private fun getAvailable(item: PrintItem) = availableItems.getOrDefault(item, 0)

	private fun incrementMissing(item: PrintItem, delta: Int) {
		missingItems[item] = missingItems.getOrDefault(item, 0) + delta
	}

	private fun decrementAvailable(item: PrintItem, old: Int, delta: Int) {
		// old is passed for efficiency
		availableItems[item] = old - delta
	}

	private fun incrementUsed(item: PrintItem, delta: Int) {
		usedItems[item] = usedItems.getOrDefault(item, 0) + delta
	}

	private fun tryCreditCost(price: Double): Boolean {
		if (price > availableCredits) {
			missingCredits += price
			return false
		}

		availableCredits -= price
		usedCredits += price
		return true
	}

	private fun removeItems() {
		for ((printItem, count) in usedItems) {
			var remainingCount = count

			for (item: ItemStack? in inventory.contents) {
				if (item == null || item.type.isAir) {
					continue
				}

				if (printItem != PrintItem(item)) {
					continue
				}

				val amount = min(remainingCount, item.amount)
				item.amount -= amount
				remainingCount -= amount

				if (remainingCount == 0) {
					break
				}
			}

			if (remainingCount > 0) {
				Throwable("$remainingCount missing of $printItem").printStackTrace()
			}
		}
	}

	private fun placeBlocks() {
		flushBlockQueue()
		fillSigns()
	}

	private fun flushBlockQueue() {
		for ((key, data) in queue) {
			val price = ShipFactoryMaterialCosts.getPrice(data.createCraftBlockData())
			if (ConfigurationFiles.featureFlags().economy) tryCreditCost(price)

			val blockX = blockKeyX(key)
			val blockY = blockKeyY(key)
			val blockZ = blockKeyZ(key)

			world.setNMSBlockData(blockX, blockY, blockZ, data)
			if (ExtractorManager.isExtractorData(CraftBlockData.fromData(data))) {
				NewTransport.addExtractor(world, blockX, blockY, blockZ)
			}
		}
	}

	private fun fillSigns() {
		for ((key, data) in signs) {
			if (!queue.containsKey(key)) {
				continue
			}

			val sign = world.getBlockAtKey(key).state as Sign

			data.applyTo(sign)
		}
	}
}
