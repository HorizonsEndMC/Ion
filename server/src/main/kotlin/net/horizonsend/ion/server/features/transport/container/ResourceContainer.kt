package net.horizonsend.ion.server.features.transport.container

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

/**
 * A container that holds transferable resources
 **/
interface ResourceContainer<T> {
	/**
	 * Get transferable resources and their amounts
	 **/
	fun getContents(): Map<T, Int>

	fun getAmount(resource: T): Int

	fun setAmount(resource: T, amount: Int)

	fun canRemove(resource: T, amount: Int): Boolean

	fun canFit(resource: T, amount: Int): Boolean

	fun add(resource: T, amount: Int): Int

	fun remove(resource: T, amount: Int): Int

	fun serialize(context: PersistentDataAdapterContext): PersistentDataContainer
}
