package net.horizonsend.ion.server.features.custom.items.util.serialization.token

import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification

class ItemModificationToken : SerializationToken<ItemModification>() {
	private val stringToken = StringToken()

	override fun deserialize(serialized: String): ItemModification {
		val string = stringToken.deserialize(serialized)
		return ItemModRegistry[string]!!
	}

	override fun validateValue(value: String): ValidationResult {
		val stringValidationResult = stringToken.validateValue(value)
		if (stringValidationResult is ValidationResult.Failure) return stringValidationResult

		ItemModRegistry[value] ?: return ValidationResult.Failure("Invalid item modification: $value")
		return ValidationResult.Success
	}

	override fun storeValue(value: ItemModification): String {
		return value.identifier
	}

	override fun getValueRange(string: String): IntRange {
		return stringToken.getValueRange(string)
	}
}
