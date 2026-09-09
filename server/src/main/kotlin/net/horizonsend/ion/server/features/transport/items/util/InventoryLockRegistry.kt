package net.horizonsend.ion.server.features.transport.items.util

import com.google.common.util.concurrent.Striped
import org.bukkit.craftbukkit.inventory.CraftInventory
import java.util.concurrent.locks.ReentrantLock

object InventoryLockRegistry {
	// Limits the total pool of locks while preventing memory leaks.
	// 1024 or 2048 stripes are usually sufficient for heavy concurrency.
	private val lockStripes = Striped.lock(2048)

	fun tryLockAll(inventories: Collection<CraftInventory>): List<ReentrantLock>? {
		if (inventories.isEmpty()) return emptyList()

		// Striped locks guarantee consistent ordering via internal index sorting
		val keys = inventories.map { keyFor(it) }.distinct()
		val locksToAcquire = lockStripes.bulkGet(keys).map { it as ReentrantLock }

		val acquired = mutableListOf<ReentrantLock>()
		for (lock in locksToAcquire) {
			if (!lock.tryLock()) {
				acquired.forEach { it.unlock() }
				return null
			}
			acquired += lock
		}
		return acquired
	}

	private fun keyFor(inventory: CraftInventory): Any {
		// Return your underlying unique identifier (e.g. BlockKey or inventory holder)
		return inventory.inventory
	}
}
