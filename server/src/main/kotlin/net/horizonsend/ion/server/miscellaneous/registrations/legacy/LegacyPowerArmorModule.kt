package net.horizonsend.ion.server.miscellaneous.registrations.legacy

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import java.util.Locale

enum class LegacyPowerArmorModule(
    val modern: IonRegistryKey<ItemModification, out ItemModification>,
) {
	ROCKET_BOOSTING(ItemModKeys.ROCKET_BOOSTING),
	SPEED_BOOSTING(ItemModKeys.SPEED_BOOSTING),

	SHOCK_ABSORBING(ItemModKeys.SHOCK_ABSORBING),
	NIGHT_VISION(ItemModKeys.NIGHT_VISION),
	PRESSURE_FIELD(ItemModKeys.PRESSURE_FIELD),
	ENVIRONMENT(ItemModKeys.ENVIRONMENT);

	companion object {
		private val nameMap = LegacyPowerArmorModule.entries.associateBy { it.name }

		operator fun get(name: String?): LegacyPowerArmorModule? {
			return nameMap[name?.uppercase(Locale.getDefault())]
		}
	}
}
