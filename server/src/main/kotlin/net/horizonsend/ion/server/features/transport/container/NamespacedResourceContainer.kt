package net.horizonsend.ion.server.features.transport.container

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.RESOURCE_AMOUNT
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.RESOURCE_CAPACITY_MAX
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.RESOURCE_CAPACITY_MIN
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER
import java.util.concurrent.ConcurrentHashMap

class NamespacedResourceContainer(
	val resourceData: Map<NamespacedKey, ResourceData>
) : ResourceContainer<NamespacedKey> {
	val stored: MutableMap<NamespacedKey, Int> = ConcurrentHashMap()

	fun getCapacity(resource: NamespacedKey): Int {
		verifyKey(resource)

		return resourceData[resource]!!.maxAmount
	}

	fun getMinimumAmount(resource: NamespacedKey): Int {
		verifyKey(resource)

		return resourceData[resource]!!.minAmount
	}

	override fun getContents(): Map<NamespacedKey, Int> {
		return stored
	}

	override fun getAmount(resource: NamespacedKey): Int {
		verifyKey(resource)

		return stored.getOrPut(resource) { 0 }
	}

	override fun setAmount(resource: NamespacedKey, amount: Int) {
		verifyKey(resource)

		val corrected = coerce(resource, amount)

		stored[resource] = corrected
	}

	/**
	 * Returns the amount of power that could not be added
	 **/
	override fun add(resource: NamespacedKey, amount: Int): Int {
		verifyKey(resource)

		val current = getAmount(resource)

		val newAmount = current + amount

		setAmount(resource, newAmount)

		val maximum = getCapacity(resource)

		return if (newAmount > maximum) maximum - newAmount else 0
	}

	/**
	 * Returns the amount of power that could not be removed
	 **/
	override fun remove(resource: NamespacedKey, amount: Int): Int {
		verifyKey(resource)

		val newAmount = getAmount(resource) - amount

		setAmount(resource, newAmount)

		val minimum = getMinimumAmount(resource)

		return if (newAmount < minimum) newAmount else minimum
	}

	override fun canFit(resource: NamespacedKey, amount: Int): Boolean {
		verifyKey(resource)

		val current = getAmount(resource)
		val maximum = getCapacity(resource)

		return current + amount <= maximum
	}

	override fun canRemove(resource: NamespacedKey, amount: Int): Boolean {
		verifyKey(resource)

		val current = getAmount(resource)
		val minimum = getMinimumAmount(resource)

		return current - amount >= minimum
	}

	private fun coerce(resource: NamespacedKey, amount: Int): Int {
		verifyKey(resource)

		val max = getCapacity(resource)
		val min = getCapacity(resource)

		return amount.coerceIn(min..max)
	}

	/**
	 * Verifies that the provided key is part of this container
	 **/
	private fun verifyKey(key: NamespacedKey) {
		if (!resourceData.containsKey(key)) throw IllegalArgumentException("Container does not contain resource $key")
	}

	override fun serialize(context: PersistentDataAdapterContext): PersistentDataContainer {
		val pdc = context.newPersistentDataContainer()

		for ((key, data) in resourceData) {
			val resourcePDC = context.newPersistentDataContainer()

			resourcePDC.set(RESOURCE_CAPACITY_MAX, INTEGER, data.maxAmount)
			resourcePDC.set(RESOURCE_CAPACITY_MIN, INTEGER, data.minAmount)
			resourcePDC.set(RESOURCE_AMOUNT, INTEGER, stored.getOrDefault(key, 0))

			pdc.set(key, TAG_CONTAINER, resourcePDC)
		}

		return pdc
	}

	companion object {
		fun deserialize(data: PersistentDataContainer): NamespacedResourceContainer {
			val resourcePDCs = data.keys.associateWith { resource ->
				data.get(resource, TAG_CONTAINER)!!
			}

			val resourceData = resourcePDCs.mapValues {  (_, pdc) ->
				ResourceData(
					maxAmount = pdc.get(RESOURCE_CAPACITY_MAX, INTEGER)!!,
					minAmount = pdc.get(RESOURCE_CAPACITY_MIN, INTEGER)!!
				)
			}

			val container = NamespacedResourceContainer(resourceData)

			resourcePDCs.entries.forEach { (resource) ->
				container.setAmount(resource, resourcePDCs[resource]!!.get(RESOURCE_AMOUNT, INTEGER)!!)
			}

			return container
		}
	}

	data class ResourceData(
		val maxAmount: Int,
		val minAmount: Int = 0,
	)

	class Builder {
		private val resources: MutableMap<NamespacedKey, ResourceData> = mutableMapOf()

		fun addResource(key: NamespacedKey, max: Int, min: Int = 0) : Builder {
			resources[key] = ResourceData(max, min)

			return this
		}

		fun build(): NamespacedResourceContainer = NamespacedResourceContainer(resources)
	}
}
