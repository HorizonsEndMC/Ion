package net.horizonsend.ion.server.features.custom.items.util.serialization

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.SerializationToken
import org.bukkit.inventory.ItemStack

class SerializationManager {
	private class StoredData<T>(
		val name: String,
		val token: SerializationToken<T>,
		val provider: (CustomItem, ItemStack) -> T,
		val application: (CustomItem, ItemStack, T) -> Unit
	) {
		fun serialize(customItem: CustomItem, itemStack: ItemStack): String {
			return "$name=${token.storeValue(provider.invoke(customItem, itemStack))}"
		}

		fun deSerialize(customItem: CustomItem, itemStack: ItemStack, data: String) {
			application.invoke(customItem, itemStack, token.deserialize(data.removePrefix("${name}=")))
		}

		override fun toString(): String {
			return "$name{$token}"
		}
	}

	private val parameters = mutableMapOf<String, StoredData<*>>()

	fun <T> addSerializedData(
		paramName: String,
		type: SerializationToken<T>,
		provider: (CustomItem, ItemStack) -> T,
		application: (CustomItem, ItemStack, T) -> Unit
	) {
		check(paramName.isNotEmpty())
		check(!paramName.contains("[\\t\\n ]".toRegex()))
		parameters[paramName] = StoredData(paramName, type, provider, application)
	}

	private fun serializeData(customItem: CustomItem, itemStack: ItemStack): Set<String> {
		return parameters.values.mapTo(mutableSetOf()) { it.serialize(customItem, itemStack) }
	}

	fun serialize(customItem: CustomItem, itemStack: ItemStack): String {
		val data = serializeData(customItem, itemStack)
		return "[${data.joinToString { it }}]"
	}

	fun deserialize(customItem: CustomItem, data: String): ItemStack {
		val preFormat = data.filterNot { it == ' ' }
		val serializedTokens = preFormat.removePrefix("[").removeSuffix("]")

		val keys = parameters.keys
		val maxKeyLength = keys.maxOf { it.length }
		val firstChars = keys.groupBy { it.first() }

		val splitPoints = mutableListOf<Pair<Int, StoredData<*>>>()

		for (index in serializedTokens.indices) {
			val char = serializedTokens[index]

			if (!firstChars.containsKey(char)) continue

			val startingWith = firstChars[char]!!
			val subString = serializedTokens.substring(index, index + maxKeyLength + 1)

			val key = startingWith.firstOrNull { subString.contains(it) } ?: continue
			splitPoints.add(index to parameters[key]!!)
		}

		val tokenData = mutableMapOf<StoredData<*>, String>()

		for ((entryIndex, value) in splitPoints.withIndex()) {
			val (charIndex, stored) = value

			val nextIndex = splitPoints.getOrNull(entryIndex + 1)?.first?.minus(2) ?: serializedTokens.lastIndex
			tokenData[stored] = serializedTokens.substring(charIndex, nextIndex + 1)
		}

		val item = customItem.constructItemStack()

		for ((stored, serialized) in tokenData) {
			stored.deSerialize(customItem, item, serialized)
		}

		return item
	}

	fun hasParameters(): Boolean = parameters.isNotEmpty()

	fun parameterKeys(): Set<String> = parameters.keys
}
