package net.horizonsend.ion.server.features.starship.factory

import io.papermc.paper.registry.RegistryAccess.registryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.task.MultiblockEntityTask
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.Companion.formatProgress
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.multiblock.type.economy.RemotePipeMultiblock.InventoryReference
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryParent
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryGui
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactorySettings
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories.missingMaterialsCache
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.miscellaneous.registrations.ShipFactoryMaterialCosts
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.getMoneyBalance
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.horizonsend.ion.server.miscellaneous.utils.setNMSBlockData
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.starlegacy.javautil.SignUtils.SignData
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.`in`
import org.litote.kmongo.nin
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class ShipFactoryPrintTask(
	blueprint: Blueprint,
	settings: ShipFactorySettings,
	entity: ShipFactoryEntity,
	private val bazaarIntegration: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity?,
	val gui: ShipFactoryGui?,
	private val inventories: Set<InventoryReference>,
	private val player: Player
) : ShipFactoryBlockProcessor(blueprint, settings, entity), MultiblockEntityTask<ShipFactoryEntity> {
	override val taskEntity: ShipFactoryEntity get() = entity

	/** Counts of missing materials */
	private val missingMaterials = mutableMapOf<PrintItem, AtomicInteger>()

	/**
	 * Stores a list of references to bazaar items that may be purchased from for this blueprint.
	 * It is loaded at the start of processing to avoid slow database lookups during the printing process
	 **/
	private val bazaarReferences = multimapOf<PrintItem, BazaarReference>()

	/** Total number of blocks that were skipped due to obstruction */
	private var skippedBlocks = 0
	/** Total number of blocks that placed successfully */
	private var printedBlocks = 0

	/** Total number of credits used while printing */
	private var consumedCredits = 0.0
	/** Total number of credits used while buying from the bazaar */
	private var bazaarConsumedCredits = 0.0
	/** Collection of responses from attempts to buy from the bazaar */
	private val bazaarPurchaseMessages = mutableListOf<Component>()

	/** Holds whether the queue has been fully loaded. Prevents ticking while loading. */
	private var queueLoaded = false
	/** Stores the number of blocks that were initially in the queue, can be used to obtain a percentage completion. */
	private var startBlocks = 0

	override fun onEnable() {
		loadBlockQueue()
		startBlocks = blockQueue.size

		if (isBazaarIntegrationEnabled()) {
			blockQueue = ConcurrentLinkedQueue(blockQueue.sortedBy { blockMap[it]?.material })
			loadBazaarReferences()
		}

		queueLoaded = true
	}

	/** Signal that the rest of the processing loop may use to tell when it is disabled. */
	override var isDisabled = false

	override fun onDisable() {
		isDisabled = true
		player.userError("Disabled ship factory.")
	}

	override fun disable() {
		entity.disable()
	}

	/** Prevents multiple ticks from running concurrently, this might happen if a large blueprint is being done, and the execution takes more than 5 ms */
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

	private fun runTick() {
		if (!queueLoaded) return
		missingMaterials.clear()

		// Blocks that are gonna be printed
		val toPrint = mutableListOf<BlockKey>()

		// Per multiblock print limit
		val printLimit = entity.multiblock.blockPlacementsPerTick

		// All items available in inventories
		val availableItems = getAvailableItems()

		val availableCredits = player.getMoneyBalance()

		// Check if the player has any credits
		checkAvailablecredits(availableCredits, 0.001)

		var consumedPower = 0
		val bazaarTransaction = mutableMapOf<Oid<BazaarItem>, MutableMap<BlockKey, Int>>()

		// Find the first blocks that can be placed with the available resources, up to the limit
		val keyIterator = blockQueue.iterator()
		while (keyIterator.hasNext()) {
			if (isDisabled) {
				isDisabled = false
				break
			}

			val printPosition: BlockKey = keyIterator.next()
			if (toPrint.size >= printLimit) break

			val blockData = blockMap[printPosition] ?: continue

			val vec3i = toVec3i(printPosition)
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

			// Check if the position is obstructed, if it is, skip the block and try the next.
			if (checkObstruction(printItem, blockData, requiredAmount)) {
				skippedBlocks++
				continue
			}

			// Check for power consumption. This check will only apply if it is an advanced ship factory.
			if (!checkPowerConsumption(consumedPower)) break

			val price = ShipFactoryMaterialCosts.getPrice(blockData)
			if (!checkAvailablecredits(availableCredits, price)) break

			val success = checkAvailableItems(printPosition, availableItems, printItem, requiredAmount, bazaarTransaction)
			if (success) {
				toPrint.add(printPosition)
				printedBlocks++

				consumedCredits += price
				consumedPower += 10
			}
			if (isDisabled) break
		}

		// If the block map is empty, printing has finished
		// If the total number of skipped blocks and printed blocks equals the size of the block queue, it is
		val hasFinished = blockQueue.isEmpty() || (startBlocks - (skippedBlocks + printedBlocks) == 0)

		// Premature failure condition - out of materials
		if (toPrint.isEmpty() && !hasFinished) {
			if (missingMaterials.isNotEmpty()) {
				sendMissing(missingMaterials, skippedBlocks)
				updateGuiButton(InputResult.FailureReason(listOf(
					text("Missing Materials!", RED),
					template(text("Printing consumed {0}", GREEN), consumedCredits.toCreditComponent())
				)))
			}

			sendCreditConsumption()
			sendBazaarReport()
			entity.disable()
		}

		val bazaarConsumptionFailures = commitBazaarTransactions(bazaarTransaction)

		Tasks.sync {
			printBlocks(toPrint.minus(bazaarConsumptionFailures.toSet()))
		}

		if (hasFinished) {
			if (missingMaterials.isNotEmpty()) {
				sendMissing(missingMaterials, skippedBlocks)
			}

			player.success("Ship factory has finished printing.")
			sendCreditConsumption()
			updateGuiButton(InputResult.SuccessReason(listOf(
				text("Ship factory has finished printing.", GREEN),
				template(text("Printing consumed {0}", GREEN), consumedCredits.roundToHundredth().toCreditComponent())
			)))

			sendBazaarReport()
			entity.disable()
		} else {
			updatePercentageStatus()
		}

		gui?.refreshAll()
	}

	//<editor-fold desc="Region Block Placement">
	/**
	 * Checks for obstructions when printing.
	 *
	 * This checks a variety of settings in the ship factory including:
	 * 	* overrideReplaceableBlocks
	 * 	* placeBlocksUnderwater
	 * 	* markObstrcutedBlocksAsComplete
	 *
	 * It returns whether the block can be placed in this position, and handles
	 * marking it missing if the user has that setting checked.
	 **/
	@Suppress("UnstableApiUsage")
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
				if (settings.markObstrcutedBlocksAsComplete) {
					return false
				}

				// Mark missing
				markItemMissing(printItem, requiredAmount)

				// Move onto next block
				return false
			}
		}

		return true
	}

	private fun printBlocks(blocks: List<BlockKey>) {
		var consumedMoney = 0.0

		var placements = 0
		for (entry in blocks) {
			blockQueue.remove(entry)
			val blockData = blockMap.remove(entry) ?: continue
			val signData = signMap.remove(entry)

			val price = ShipFactoryMaterialCosts.getPrice(blockData)
			if (!player.hasEnoughMoney(consumedMoney + price) && ConfigurationFiles.featureFlags().economy) continue
			consumedMoney += price

			// If all good, place the block
			placements++
			placeBlock(entry, blockData, signData)
		}

		if (entity is AdvancedShipFactoryParent.AdvancedShipFactoryEntity) {
			entity.powerStorage.removePower(placements * 10)
		}

		if (ConfigurationFiles.featureFlags().economy) player.withdrawMoney(consumedMoney)
	}

	private fun placeBlock(printPosition: BlockKey, data: BlockData, signData: SignData?) {
		var placedData = data

		val (x, y, z) = toVec3i(printPosition)
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
	//</editor-fold>

	//<editor-fold desc="Region Misc Consumption">
	/**
	 * Returns if the machine has enough power to proceed
	 * Disables the process and updates the status if the check does not pass
	 **/
	private fun checkPowerConsumption(consumedPower: Int): Boolean {
		if (entity !is AdvancedShipFactoryParent.AdvancedShipFactoryEntity) return true

		val power = entity.powerStorage.getPower()
		if (power >= consumedPower) return true

		updateAll(text("Ship Factory has Insufficient Power!", RED))
		disable()
		return false
	}

	/**
	 * Returns if the player has enough credits to proceed
	 * Disables the process and updates the status if the check does not pass
	 **/
	private fun checkAvailablecredits(availableCredits: Double, price: Double): Boolean {
		if (ConfigurationFiles.featureFlags().economy) return true

		if (availableCredits >= price) return true

		updateAll(text("Insufficent Credits!", RED))
		disable()
		return false
	}
	//</editor-fold>

	//<editor-fold desc="Region Bazaar Integration">
	/**
	 * Returns true if enough items could be purchased from the bazaar to cover the amount needed.
	 * The @param printPosition is needed to mark if there were sufficient items, as the bazaar transactions are bundled at the end to avoid multiple database checks.
	 **/
	private fun consumeItemFromBazaarReferences(printItem: PrintItem, printPosition: BlockKey, amount: Int, bazaarTransaction: MutableMap<Oid<BazaarItem>, MutableMap<BlockKey, Int>>): Boolean {
		val references = bazaarReferences[printItem]
		if (references.isEmpty()) return false

		var toConsume = amount
		val referencesFor = bazaarReferences[printItem].toMutableSet()

		val newTransactions = mutableMapOf<Oid<BazaarItem>, MutableMap<BlockKey, Int>>()

		while (toConsume > 0 && referencesFor.isNotEmpty()) {
			val idealReference = references.minBy { it.price }
			val removeStock = minOf(toConsume, idealReference.amount.get())

			referencesFor.remove(idealReference)

			if (!idealReference.consume(removeStock)) continue

			newTransactions.getOrPut(idealReference.id) { mutableMapOf() }[printPosition] = removeStock
			toConsume -= removeStock
		}

		// If the references are not sufficient, abandon the transaction.
		if (toConsume > 0) return false

		// Merge the new transactions into the map if they are sufficient to meet the needs.
		newTransactions.forEach { (reference, blockMap) ->
			bazaarTransaction.getOrPut(reference) { mutableMapOf() }.putAll(blockMap)
		}
		return true
	}

	private fun loadBazaarReferences() {
		val partner = bazaarIntegration ?: return

		val types = blockMap.values.mapNotNullTo(mutableSetOf()) { PrintItem[it]?.itemString }
		val matchingTypesCheck = BazaarItem::itemString.`in`(types)

		val territory = Regions.findFirstOf<RegionTerritory>(entity.location)

		val matchingItems = BazaarItem.find(
			and(
				BazaarItem::stock gt 0,
				matchingTypesCheck,
				if (!partner.shipFactoryAllowRemote) BazaarItem::cityTerritory eq territory?.id else EMPTY_BSON,
				if (partner.shipFactoryWhitelistMode) BazaarItem::itemString `in` partner.shipFactoryItemRestriction.toList() else BazaarItem::itemString nin partner.shipFactoryItemRestriction.toList()
			)
		)

		for (document in matchingItems) {
			val maxPrice = partner.shipFactoryMaxUnitPrice[document.itemString]
			if (maxPrice != null && document.price > maxPrice) continue

			if (!TradeCities.isCity(Regions[document.cityTerritory])) continue

			val printItem = PrintItem(document.itemString)

			bazaarReferences[printItem].add(loadBazaarReference(document))
		}
	}

	private fun isBazaarIntegrationEnabled(): Boolean {
		val partner = bazaarIntegration ?: return false
		return partner.enableShipFactoryIntegration
	}

	/**
	 * Will return null if there was a failure in purchasing items.
	 **/
	private fun commitBazaarTransactions(transaction: Map<Oid<BazaarItem>, Map<BlockKey, Int>>): List<BlockKey> {
		if (transaction.isEmpty()) return listOf()

		val failures = mutableListOf<BlockKey>()

		for ((referenceId, amountMap) in transaction) {
			val printPrice = amountMap.keys.sumOf { ShipFactoryMaterialCosts.getPrice(blockMap[it]!!) }
			val count = amountMap.values.sum()

			kotlin.runCatching {
				val document = BazaarItem.findById(referenceId) ?: return@runCatching null
				val remote = !Regions.get<RegionTerritory>(document.cityTerritory).contains(entity.location)

				val purchaseFutureResult = Bazaars.tryBuyFromSellOrder(player, document, count, remote) { itemStack, amount, cost, priceMult ->
					{
						consumedCredits += cost
						bazaarConsumedCredits += cost

						val maxStackSize = itemStack.maxStackSize
						val fullStacks = amount / maxStackSize
						val remainder = amount % maxStackSize

						val quantityMessage = if (itemStack.maxStackSize == 1) "{0}" else "{0} stack${if (fullStacks == 1) "" else "s"} and {1} item${if (remainder == 1) "" else "s"}"

						var fullMessage = template(
							text("Bought $quantityMessage ({2}) of {3} for {4}", GREEN),
							fullStacks,
							remainder,
							amount,
							itemStack.displayNameComponent,
							cost.toCreditComponent(),
						)

						if (priceMult > 1) {
							val priceMultiplicationMessage = template(text("(Price multiplied by {0} due to browsing remotely)", YELLOW), priceMult)
							fullMessage = ofChildren(fullMessage, space(), priceMultiplicationMessage)
						}

						// Once it is sucessful, count it as consumed credits.
						consumedCredits += printPrice

						bazaarPurchaseMessages.add(fullMessage)
						InputResult.InputSuccess
					}
				}.get()

				purchaseFutureResult.withResult { result ->
					if (result.isSuccess()) return@withResult // Condition already handled

					val reason = result.getReason() ?: return@withResult

					failures.addAll(amountMap.keys)

					val reference = getBazaarReference(referenceId)

					bazaarPurchaseMessages.add(
						ofChildren(
							template(
								text("Could not buy {0} of {1}: ", RED),
								count,
								reference.string,
							), reason.join(separator = space())
						)
					)
				}
			}.onFailure {
				it.printStackTrace()
			}
		}

		return failures
	}

	private fun sendBazaarReport() {
		if (bazaarPurchaseMessages.isEmpty()) return

		val buyReport = ofChildren(
			template(
				text("{0} purchases were attempted on the bazaar to cover missing materials for a total cost of {1}", YELLOW),
				bazaarPurchaseMessages.size,
				bazaarConsumedCredits.toCreditComponent()
			),
			newline(),
			text("Hover over this message to see the full list.", YELLOW),
		)

		val hoverText = bazaarPurchaseMessages.join(separator = newline())

		val message = text()
			.hoverEvent(HoverEvent.showText(hoverText))
			.clickEvent(ClickEvent.callback { player.sendMessage(hoverText) })
			.append(buyReport)
			.build()

		sendPlayerMessage(message)
	}

	private val bazaarReferenceCache = mutableMapOf<Oid<BazaarItem>, BazaarReference>()
	private fun loadBazaarReference(document: BazaarItem): BazaarReference {
		bazaarReferenceCache[document._id]?.let { return it }

		val ref = BazaarReference(document.itemString, document.price, AtomicInteger(document.stock), document._id)
		bazaarReferenceCache[document._id] = ref
		return ref
	}

	private fun getBazaarReference(id: Oid<BazaarItem>): BazaarReference {
		return bazaarReferenceCache[id]!!
	}
	//</editor-fold>

	//<editor-fold desc="Region Item Consumption">
	private data class AvailableItemInformation(val amount: AtomicInteger, val references: MutableList<ItemReference>)

	/**
	 * Returns a map of all available items, using a print item as a key,
	 * and the value holding a sum and references to those items in available inventories.
	 **/
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

	private fun checkAvailableItems(
		printPosition: BlockKey,
		availableItems: Map<PrintItem, AvailableItemInformation>,
		printItem: PrintItem,
		requiredAmount: Int,
		bazaarTransaction: MutableMap<Oid<BazaarItem>, MutableMap<BlockKey, Int>>
	): Boolean {
		val resourceInformation = availableItems[printItem]
			?: if (isBazaarIntegrationEnabled() && consumeItemFromBazaarReferences(printItem, printPosition, requiredAmount, bazaarTransaction)) return true
			else {
				markItemMissing(printItem, requiredAmount)
				// Don't break loop
				return false
			}

		if (resourceInformation.amount.get() < requiredAmount) {
			val missing = requiredAmount - resourceInformation.amount.get()

			// Try and make a partial purchase with the missing amount
			if (!isBazaarIntegrationEnabled() || !consumeItemFromBazaarReferences(printItem, printPosition, missing, bazaarTransaction)) {
				markItemMissing(printItem, missing)
				return false
			} else {
				resourceInformation.amount.addAndGet(-resourceInformation.amount.get())

				val missingFromInventories = consumeItemFromReferences(resourceInformation.references, missing)

				if (missingFromInventories == 0) return true
				else {
					markItemMissing(printItem, missingFromInventories)
					return false
				}
			}
		}

		resourceInformation.amount.addAndGet(-requiredAmount)

		if (missingMaterials.containsKey(printItem)) {
			val atomic = missingMaterials[printItem]

			if (atomic != null) {
				if (atomic.get() >= requiredAmount) atomic.addAndGet(-requiredAmount)
			}
		}

		val references = resourceInformation.references
		val missing = consumeItemFromReferences(references, requiredAmount)

		if (missing > 0) {
			if (!isBazaarIntegrationEnabled() || !consumeItemFromBazaarReferences(printItem, printPosition, missing, bazaarTransaction)) {
				markItemMissing(printItem, missing)
				return false
			}
		}

		return true
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
	//</editor-fold>

	//<editor-fold desc="Region Communication">
	private fun updateGuiButton(text: Component) {
		gui?.enableButton?.updateWith(InputResult.FailureReason(listOf(text)))
	}

	private fun updateGuiButton(inputResult: InputResult) {
		gui?.enableButton?.updateWith(inputResult)
	}

	private fun updateMultiblockDisplay(text: Component) {
		entity.statusManager.setStatus(text)
	}

	private fun sendPlayerMessage(text: Component) {
		player.sendMessage(text)
	}

	private fun updateAll(text: Component) {
		updateGuiButton(text)
		updateMultiblockDisplay(text)
		sendPlayerMessage(text)
	}

	private fun sendMissing(missingMaterials: MutableMap<PrintItem, AtomicInteger>, skippedBlocks: Int) {
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

	private fun updatePercentageStatus() {
		val blueprintName = text(entity.blueprintName)
		val percentValue = ((startBlocks - (skippedBlocks + printedBlocks)) / startBlocks.toDouble())
		val percent = formatProgress(WHITE, percentValue)

		val formatted = ofChildren(blueprintName, text(": ", HEColorScheme.HE_DARK_GRAY), percent)
		entity.setStatus(formatted)
	}

	private fun sendCreditConsumption() {
		sendPlayerMessage(template(text("Printing Consumed {0}", NamedTextColor.BLUE), consumedCredits.toCreditComponent()))
	}
	//</editor-fold>
}
