package net.horizonsend.ion.server.features.transport.items.transaction

import net.horizonsend.ion.server.features.transport.items.transaction.Change.ItemAddition
import net.horizonsend.ion.server.features.transport.items.transaction.Change.ItemRemoval
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.inventory.Inventory

class ItemTransaction(val originCacheHolder: CacheHolder<ItemTransportCache>) {
	val removals = multimapOf<BlockKey, ItemRemoval>()
	val additions = multimapOf<BlockKey, ItemAddition>()

	fun addRemoval(sourcePos: BlockKey, diff: ItemRemoval) {
		removals[sourcePos].add(diff)
	}

	fun addAddition(sourcePos: BlockKey, diff: ItemAddition) {
		additions[sourcePos].add(diff)
	}

	data class CommitContext(val key: BlockKey, val inventory: Inventory, val change: Change) {
		fun check() = change.check(inventory)
		fun commit() = change.execute(inventory)
	}

	fun commit() {
		val inventories = mutableMapOf<BlockKey, Inventory?>()

		val removalContexts = mutableListOf<CommitContext>()
		val additionContexts = mutableListOf<CommitContext>()

		val getInventory = fun(key: BlockKey): Inventory? {
			return inventories.getOrPut(key) { originCacheHolder.cache.getInventory(key) }
		}

		removals.entries().mapNotNullTo(removalContexts) { entry ->
			val inventory = getInventory(entry.key) ?: return@mapNotNullTo null
			CommitContext(entry.key, inventory, entry.value)
		}

		additions.entries().mapNotNullTo(additionContexts) { entry ->
			val inventory = getInventory(entry.key) ?: return@mapNotNullTo null
			CommitContext(entry.key, inventory, entry.value)
		}

		if (!checkAll(removalContexts)) return
		if (!checkAll(additionContexts)) return

		for (removal in removalContexts) {
			removal.commit()
		}

		for (addition in additionContexts) {
			addition.commit()
		}
	}

	fun checkAll(contexts: Collection<CommitContext>): Boolean {
		return contexts.all { context -> context.check() }
	}
}
