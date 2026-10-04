package net.horizonsend.ion.server.features.starship.factory.integration

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.starship.factory.BazaarReference
import net.horizonsend.ion.server.features.starship.factory.PrintItem
import net.horizonsend.ion.server.features.starship.factory.ShipFactoryPrintTask
import net.horizonsend.ion.server.miscellaneous.registrations.ShipFactoryMaterialCosts
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.`in`
import org.litote.kmongo.nin
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class BazaarTerminalIntegration(
	taskEntity: ShipFactoryEntity,
	terminal: BazaarTerminalMultiblockEntity
) : ShipFactoryIntegration<BazaarTerminalMultiblockEntity>(taskEntity, terminal) {
	private fun isIntegrationEnabled(): Boolean {
		return integratedEntity.enableShipFactoryIntegration
	}

	/**
	 * Stores a list of references to bazaar items that may be purchased from for this blueprint.
	 * It is loaded at the start of processing to avoid slow database lookups during the printing process
	 **/
	private val bazaarReferences = multimapOf<PrintItem, BazaarReference>()

	/** Total number of credits used while buying from the bazaar */
	private var bazaarConsumedCredits = 0.0

	/** Collection of responses from attempts to buy from the bazaar */
	private val bazaarPurchaseMessages = mutableListOf<Component>()

	override fun asyncSetup(task: ShipFactoryPrintTask) {
		task.blockQueue = ConcurrentLinkedQueue(task.blockQueue.sortedBy { task.blockMap[it]?.material })
		loadBazaarReferences(task)
	}

	private fun loadBazaarReferences(task: ShipFactoryPrintTask) {
		if (!isIntegrationEnabled()) return

		val types = task.blockMap.values.mapNotNullTo(mutableSetOf()) { PrintItem[it]?.itemString }
		val matchingTypesCheck = BazaarItem::itemString.`in`(types)

		val territory = Regions.findFirstOf<RegionTerritory>(task.entity.location)

		val matchingItems = BazaarItem.find(
			and(
				BazaarItem::stock gt 0,
				matchingTypesCheck,
				if (!integratedEntity.shipFactoryAllowRemote) BazaarItem::cityTerritory eq territory?.id else EMPTY_BSON,
				if (integratedEntity.shipFactoryWhitelistMode) BazaarItem::itemString `in` integratedEntity.shipFactoryItemRestriction.toList() else BazaarItem::itemString nin integratedEntity.shipFactoryItemRestriction.toList()
			)
		)

		for (document in matchingItems) {
			val maxPrice = integratedEntity.shipFactoryMaxUnitPrice[document.itemString]
			if (maxPrice != null && document.price > maxPrice) continue

			if (!TradeCities.isCity(Regions[document.cityTerritory])) continue

			val printItem = PrintItem(document.itemString)

			bazaarReferences[printItem].add(loadBazaarReference(document))
		}
	}

	private var transaction: MutableMap<Oid<BazaarItem>, MutableMap<BlockKey, Int>>? = null

	override fun startNewTransaction(task: ShipFactoryPrintTask) {
		transaction = mutableMapOf()
	}

	override fun commitTransaction(task: ShipFactoryPrintTask): List<BlockKey> {
		val transaction = this.transaction ?: return listOf()
		if (transaction.isEmpty()) return listOf()

		val failures = mutableListOf<BlockKey>()

		for ((referenceId, amountMap) in transaction) {
			val printPrice = amountMap.keys.sumOf { ShipFactoryMaterialCosts.getPrice(task.blockMap[it]!!) }
			val count = amountMap.values.sum()

			kotlin.runCatching {
				val document = BazaarItem.findById(referenceId) ?: return@runCatching null
				val remote = !Regions.get<RegionTerritory>(document.cityTerritory).contains(task.entity.location)

				val purchaseFutureResult = Bazaars.tryBuyFromSellOrder(task.player, document, count, remote) { itemStack, amount, cost, priceMult ->
					{
						task.consumedCredits += cost
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
						task.consumedCredits += printPrice

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


	/**
	 * Returns true if enough items could be purchased from the bazaar to cover the amount needed.
	 * The @param printPosition is needed to mark if there were sufficient items, as the bazaar transactions are bundled at the end to avoid multiple database checks.
	 **/
	override fun canAddTransaction(printItem: PrintItem, printPosition: BlockKey, requiredAmount: Int): Boolean {
		val bazaarTransaction = transaction ?: return false

		val references = bazaarReferences[printItem].toMutableSet()
		if (references.isEmpty()) return false

		var toConsume = requiredAmount

		val newTransactions = mutableMapOf<Oid<BazaarItem>, MutableMap<BlockKey, Int>>()

		while (toConsume > 0 && references.isNotEmpty()) {
			val idealReference = references.minBy { it.price }
			val removeStock = minOf(toConsume, idealReference.amount.get())

			references.remove(idealReference)

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

	override fun sendReport(task: ShipFactoryPrintTask, hasFinished: Boolean) {
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
			.clickEvent(ClickEvent.callback { task.player.sendMessage(hoverText) })
			.append(buyReport)
			.build()

		task.sendPlayerMessage(message)
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
}
