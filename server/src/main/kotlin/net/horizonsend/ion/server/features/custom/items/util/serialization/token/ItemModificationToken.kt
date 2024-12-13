package net.horizonsend.ion.server.features.custom.items.util.serialization.token

import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification

class ItemModificationToken : SerializationToken<ItemModification>() {
	override fun deserialize(serialized: String): ItemModification {
		return ItemModRegistry[serialized]!!
	}

	override fun validateValue(value: String): ValidationResult {
		ItemModRegistry[value] ?: return ValidationResult.Failure("Invalid item modification: $value")
		return ValidationResult.Success
	}

	override fun storeValue(value: ItemModification): String {
		return value.identifier
	}
}
