package net.horizonsend.ion.server.features.custom.items.util.serialization

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.custom.items.CustomItem

object CustomItemSerialization : IonServerComponent() {
	fun getCompletions(input: String): List<String>? {

		val customItem = CustomItemKeys[input.substringBefore('[')]?.getValue() ?: return CustomItemKeys.allStrings().filter { it.startsWith(input) }

		return getCompletions(customItem, input.substringAfter(customItem.identifier))
	}

	private fun getCompletions(customItem: CustomItem, input: String): List<String> {
		if (input.isEmpty()) {
			return listOf("[")
		}

		return customItem.getParamaterKeys().toList()
	}

	/**
	 * Ensures custom item data is valid
	 **/
	fun valiate(customItem: CustomItem, input: String) {

	}
}
