package net.starlegacy.feature.gear.powerarmor

import net.starlegacy.feature.misc.CustomItems
import org.bukkit.inventory.ItemStack
import java.util.Locale

enum class PowerArmorModule(
	private val customItem: CustomItems.PowerModuleItem,
	vararg compatibleTypes: PowerArmorType
) {
	ROCKET_BOOSTING(CustomItems.POWER_MODULE_ROCKET_BOOSTING, PowerArmorType.BOOTS),
	SPEED_BOOSTING(CustomItems.POWER_MODULE_SPEED_BOOSTING, PowerArmorType.LEGGINGS),

	//	SHOCK_ABSORBING(CustomItems.POWER_MODULE_SHOCK_ABSORBING, PowerArmorType.CHESTPLATE),
	NIGHT_VISION(CustomItems.POWER_MODULE_NIGHT_VISION, PowerArmorType.HELMET),
	PRESSURE_FIELD(CustomItems.POWER_MODULE_PRESSURE_FIELD, PowerArmorType.HELMET),
	ENVIRONMENT(CustomItems.POWER_MODULE_ENVIRONMENT, PowerArmorType.HELMET);

	private val compatibleTypes = compatibleTypes.toSet()

	fun isCompatible(type: PowerArmorType?): Boolean {
		return type != null && compatibleTypes.contains(type)
	}

	companion object {
		private val customitemMap = values().associateBy { it.customItem }
		private val nameMap = values().associateBy { it.name }

		operator fun get(item: ItemStack?): PowerArmorModule? {
			return customitemMap[CustomItems[item]]
		}

		operator fun get(name: String?): PowerArmorModule? {
			return nameMap[name?.uppercase(Locale.getDefault())]
		}
	}
}
