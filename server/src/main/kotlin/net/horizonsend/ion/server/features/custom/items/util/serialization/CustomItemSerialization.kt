package net.horizonsend.ion.server.features.custom.items.util.serialization

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry

object CustomItemSerialization : IonServerComponent() {
	fun getCompletions(input: String): List<String>? {

		val customItem = CustomItemRegistry.getByIdentifier(input.substringBefore('[')) ?: return CustomItemRegistry.identifiers.filter { it.startsWith(input) }

		return getCompletions(customItem, input.substringAfter(customItem.identifier))
	}

	private fun getCompletions(customItem: CustomItem, input: String): List<String> {
		if (input.isEmpty()) {
			return listOf("[")
		}

		return customItem.serializationManager.parameterKeys().toList()
	}

	/**
	 * Ensures custom item data is valid
	 **/
	fun valiate(customItem: CustomItem, input: String) {

	}
}
