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
			application.invoke(customItem, itemStack, token.deserialize(data))
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
		var remaining = data.filterNot { it == ' ' }.removePrefix("[").removeSuffix("]")

		val tokenData = mutableMapOf<StoredData<*>, String>()

		while (remaining.isNotEmpty()) {
			val endIndex = remaining.indexOfFirst { it == '=' }
			val paramKey = remaining.substring(0..< endIndex) // Exclude the '='
			val param = parameters[paramKey] ?: throw IllegalArgumentException("Param $paramKey not found!")

			val startOfData = (endIndex + 1) // Skip to the next char

			remaining = remaining.substring(startOfData.. remaining.lastIndex)
			val paramRange = param.token.getValueRange(remaining)
			val serializedTokenData = remaining.substring(paramRange)
			tokenData[param] = serializedTokenData

			remaining = remaining.substring(paramRange.last + paramRange.first..remaining.lastIndex)

			val nextParamStart = remaining.indexOfFirst { it == ',' }
			if (nextParamStart == -1) break

			remaining = remaining.substring(nextParamStart + 1..remaining.lastIndex)
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
