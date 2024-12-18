package net.horizonsend.ion.server.miscellaneous.registrations.legacy

import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.LegacyPowerArmorModule.values
import java.util.Locale
import java.util.function.Supplier

enum class LegacyPowerArmorModule(
    val modern: Supplier<ItemModification>,
) {
	ROCKET_BOOSTING(ItemModRegistry::ROCKET_BOOSTING),
	SPEED_BOOSTING(ItemModRegistry::SPEED_BOOSTING),

	SHOCK_ABSORBING(ItemModRegistry::SHOCK_ABSORBING),
	NIGHT_VISION(ItemModRegistry::NIGHT_VISION),
	PRESSURE_FIELD(ItemModRegistry::PRESSURE_FIELD),
	ENVIRONMENT(ItemModRegistry::ENVIRONMENT);

	companion object {
		private val nameMap = values().associateBy { it.name }

		operator fun get(name: String?): LegacyPowerArmorModule? {
			return nameMap[name?.uppercase(Locale.getDefault())]
		}
	}
}
