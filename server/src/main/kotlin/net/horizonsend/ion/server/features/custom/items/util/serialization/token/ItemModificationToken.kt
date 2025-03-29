package net.horizonsend.ion.server.features.custom.items.util.serialization.token

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification

class ItemModificationToken : SerializationToken<IonRegistryKey<ItemModification, out ItemModification>>() {
	override fun deserialize(serialized: String): IonRegistryKey<ItemModification, out ItemModification> {
		return ItemModKeys[serialized]!!
	}

	override fun validateValue(value: String): ValidationResult {
		ItemModKeys[value] ?: return ValidationResult.Failure("Invalid item modification: $value")
		return ValidationResult.Success
	}

	override fun storeValue(value: IonRegistryKey<ItemModification, out ItemModification>): String {
		return value.key
	}
}
