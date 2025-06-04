package net.horizonsend.ion.server.features.starship.factory

import io.papermc.paper.registry.RegistryAccess.registryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.task.MultiblockEntityTask
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.Companion.formatProgress
import net.horizonsend.ion.server.features.multiblock.type.economy.RemotePipeMultiblock.InventoryReference
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryParent
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryGui
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
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.starlegacy.javautil.SignUtils.SignData
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.atomic.AtomicInteger

class ShipFactoryPrintTask(
	blueprint: Blueprint,
	settings: ShipFactorySettings,
	entity: ShipFactoryEntity,
	val gui: ShipFactoryGui?,
	private val inventories: Set<InventoryReference>,
	private val player: Player
) : ShipFactoryBlockProcessor(blueprint, settings, entity), MultiblockEntityTask<ShipFactoryEntity> {

	override val taskEntity: ShipFactoryEntity get() = entity
	private val missingMaterials = mutableMapOf<PrintItem, AtomicInteger>()

	private var skippedBlocks = 0
	private var totalBlocks = 0

	override fun disable() {
		entity.disable()
	}

	// Prevents multiple ticks from running concurrently, this might happen if a large blueprint is being done, and the execution takes more than 5 ms
	private var isTicking = false

	override fun tick() {
		if (isTicking) return

		isTicking = true
		try {
		    runTick()
		} finally {
		    isTicking = false
		}
	}

	private var disabledSignal = false
	private var consumedCredits: Double = 0.0

	private fun runTick() {
		if (!queueLoaded) return
		missingMaterials.clear()

		// Blocks that are gonna be printed
		val toPrint = mutableListOf<BlockKey>()

		// Per multiblock print limit
		val printLimit = entity.multiblock.blockPlacementsPerTick

		// All items available in inventories
		val availableItems = getAvailableItems()

		var availableCredits = player.getMoneyBalance()

		if (!ConfigurationFiles.featureFlags().economy) {
			availableCredits = Double.MAX_VALUE
		}

		if (availableCredits <= 0.0) {
			player.userError("You don't have enough credits to print!")
			gui?.enableButton?.updateWith(InputResult.FailureReason(listOf(text("You don't have enough credits to print!", RED))))
			disable()
		}

		var usedPower = 0

		// Find the first blocks that can be placed with the available resources, up to the limit
		val keyIterator = blockQueue.iterator()
		while (keyIterator.hasNext()) {
			if (disabledSignal) {
				disabledSignal = false
				break
			}

			val key: BlockKey = keyIterator.next()
			if (toPrint.size >= printLimit) break

			val blockData = blockMap[key] ?: continue

			val vec3i = toVec3i(key)
			val worldBlockData = entity.world.getBlockData(vec3i.x, vec3i.y, vec3i.z)
			if (worldBlockData == blockData) {
				// Save an iteration
				keyIterator.remove()
				continue
			}

			val printItem = PrintItem[blockData]
			if (printItem == null) {
				IonServer.slF4JLogger.warn("$blockData has no print item!")
				continue
			}

			val requiredAmount = StarshipFactories.getRequiredAmount(blockData)

			if (checkObstruction(printItem, blockData, requiredAmount)) continue

			var toBreak = false

			val breakLoop = {
				toBreak = true
				disable()
			}

			val consumeCredits = { credits: Double -> consumedCredits += credits; usedPower += 10 }

			checkResource(key, availableItems, printItem, blockData, requiredAmount, availableCredits, toPrint, consumeCredits, breakLoop)
			if (toBreak) break
		}

		if (toPrint.isEmpty()) {
			if (missingMaterials.isNotEmpty()) {
				sendMissing(missingMaterials)
				player.information("Printing consumed C${consumedCredits.roundToHundredth()}")
				gui?.enableButton?.updateWith(InputResult.FailureReason(listOf(
					text("Missing Materials!", RED),
					template(text("Printing consumed {0}", GREEN), consumedCredits.toCreditComponent())
				)))
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
			player.information("Printing consumed C${consumedCredits.roundToHundredth()}")
			gui?.enableButton?.updateWith(InputResult.SuccessReason(listOf(
				text("Ship factory has finished printing.", GREEN),
				template(text("Printing consumed {0}", GREEN), consumedCredits.toCreditComponent())
			)))
			entity.disable()
		} else {
			updateStatus()
		}

		gui?.refreshAll()
	}

	private var queueLoaded = false
	private var startBlocks = 0

	override fun onEnable() {
		loadBlockQueue()
		startBlocks = blockQueue.size
		queueLoaded = true
	}

	override fun onDisable() {
		disabledSignal = true

		player.userError("Disabled ship factory.")
	}

	private fun printBlocks(availableItems: Map<PrintItem, AvailableItemInformation>, missingMaterials: MutableMap<PrintItem, AtomicInteger>, blocks: List<BlockKey>) {
		var consumedMoney = 0.0

		var placements = 0
		for (entry in blocks) {
			blockQueue.remove(entry)
			val blockData = blockMap.remove(entry) ?: continue
			val signData = signMap.remove(entry)

			val price = ShipFactoryMaterialCosts.getPrice(blockData)
			if (!player.hasEnoughMoney(consumedMoney + price) && ConfigurationFiles.featureFlags().economy) continue
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
			placements++
			placeBlock(entry, blockData, signData)
		}

		if (entity is AdvancedShipFactoryParent.AdvancedShipFactoryEntity) {
			entity.powerStorage.removePower(placements * 10)
		}

		if (ConfigurationFiles.featureFlags().economy) player.withdrawMoney(consumedMoney)
	}

	private fun placeBlock(location: BlockKey, data: BlockData, signData: SignData?) {
		var placedData = data

		val (x, y, z) = toVec3i(location)
		val world = entity.world
		val block = world.getBlockAt(x, y, z)

		if (placedData is Waterlogged && block.type == Material.WATER) {
			placedData = data.clone() // Don't affect placements if moved
			(placedData as Waterlogged).isWaterlogged = true
		}

		world.setNMSBlockData(x, y, z, getRotatedBlockData(placedData))

		val state = block.state as? Sign
		if (state != null) {
			signData?.applyTo(state)
			Tasks.sync {
				MultiblockEntities.loadFromSign(state)
			}
		}
	}

	private fun getAvailableItems(): Map<PrintItem, AvailableItemInformation> {
		val items = mutableMapOf<PrintItem, AvailableItemInformation>()

		for (inventoryReference in inventories) {
			for ((index, item: ItemStack?) in inventoryReference.inventory.contents.withIndex()) {
				if (item == null || item.type.isEmpty) continue
				val printItem = runCatching { PrintItem(item) }.getOrNull() ?: continue

				if (!inventoryReference.isAvailable(item)) {
					continue
				}

				val information = items.getOrPut(printItem) { AvailableItemInformation(AtomicInteger(), mutableListOf()) }

				information.amount.addAndGet(item.amount)
				information.references.add(ItemReference(inventoryReference.inventory, index))
			}
		}

		// If asked to leave one remaining, reduce the available items in each slot by 1 so they won't be consumed
		if (settings.leaveItemRemaining) {
			for ((_, information) in items) {
				if (information.amount.get() > 0) information.amount.decrementAndGet()
			}
		}

		return items
	}

	private fun checkObstruction(
		printItem: PrintItem,
		worldBlockData: BlockData,
		requiredAmount: Int
	): Boolean {
		val replaceableTag = registryAccess().getRegistry(RegistryKey.BLOCK).getTag(BlockTypeTagKeys.REPLACEABLE)
		val replaceable = replaceableTag.contains(TypedKey.create(RegistryKey.BLOCK, worldBlockData.material.asBlockType()!!.key))

		// Whether to skip the obstruction check because it replaceable and that setting is enabled
		val isAllowedReplaceable = replaceable && settings.overrideReplaceableBlocks

		// Whether to skip the obstruction check because it water and that setting is enabled
		val isAllowedWater = worldBlockData.material == Material.WATER && settings.placeBlocksUnderwater

		// If it is air then it can be placed.
		// If it is not air, AND not replaceable (if replaceables are marked as obstructing), OR in water (if water placement is not allowed)
		// then placement is obstructed
		if (!worldBlockData.material.isAir) {
			// Air is replacable, so the check should only be done if it is not air
			if (!isAllowedReplaceable && !isAllowedWater) {
				// Continue if it should just be marked as complete, not missing
				if (settings.markObstrcutedBlocksAsComplete) return false

				// Mark missing
				skippedBlocks++
				markItemMissing(printItem, requiredAmount)

				// Move onto next block
				return false
			}
		}

		return true
	}

	private fun checkResource(
		position: BlockKey,
		availableItems: Map<PrintItem, AvailableItemInformation>,
		printItem: PrintItem,
		printItemBlockData: BlockData,
		requiredAmount: Int,
		availableCredits: Double,
		toPrint: MutableList<BlockKey>,
		consumeResourcesFunction: (Double) -> Unit,
		breakLoopFunction: () -> Unit
	) {
		val price = ShipFactoryMaterialCosts.getPrice(printItemBlockData)

		if (availableCredits < price) {
			gui?.enableButton?.updateWith(InputResult.FailureReason(listOf(text("Insufficent Credits!", RED))))
			entity.statusManager.setStatus(text("Insufficient Credits!", RED))
			player.userError("Insufficient Credits!")

			breakLoopFunction.invoke()
			disable()
			return
		}

		val resourceInformation = availableItems[printItem]

		if (resourceInformation == null) {
			markItemMissing(printItem, requiredAmount)
			// Don't break loop
			return
		}

		if (resourceInformation.amount.get() < requiredAmount) {
			markItemMissing(printItem, requiredAmount - resourceInformation.amount.get())
			// Don't break loop
			return
		}

		toPrint.add(position)

		if (entity is AdvancedShipFactoryParent.AdvancedShipFactoryEntity) {
			val power = entity.powerStorage.getPower()
			if (power < toPrint.size * 10) {
				gui?.enableButton?.updateWith(InputResult.FailureReason(listOf(text("Insufficient Power!", RED))))
				entity.statusManager.setStatus(text("Insufficient Power!", RED))
				player.userError("Ship factory has insufficient power")

				breakLoopFunction.invoke()
				disable()
				return
			}
		}

		resourceInformation.amount.addAndGet(-requiredAmount)

		if (missingMaterials.containsKey(printItem)) {
			val atomic = missingMaterials[printItem]

			if (atomic != null) {
				if (atomic.get() >= requiredAmount) atomic.addAndGet(-requiredAmount)
			}
		}

		consumeResourcesFunction.invoke(price)
		consumedCredits += price
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
				remaining -= toRemove
			}
		}

		return remaining
	}

	private fun markItemMissing(printItem: PrintItem, amount: Int): Int {
		return missingMaterials.getOrPut(printItem) { AtomicInteger() }.addAndGet(amount)
	}

	private fun updateStatus() {
		val blueprintName = text(entity.blueprintName)
		val percentValue = 1.0 - (blockQueue.size / startBlocks.toDouble())
		val percent = formatProgress(WHITE, percentValue)

		val formatted = ofChildren(blueprintName, text(": ", HEColorScheme.HE_DARK_GRAY), percent)
		entity.setStatus(formatted)
	}
}
