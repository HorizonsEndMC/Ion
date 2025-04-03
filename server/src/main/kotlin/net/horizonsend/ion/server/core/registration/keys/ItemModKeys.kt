package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification

object ItemModKeys : KeyRegistry<ItemModification>(RegistryKeys.ITEM_MODIFICATIONS, ItemModification::class) {
	val AOE_1 = registerKey("AOE_1")
	val AOE_2 = registerKey("AOE_2")
	val VEIN_MINER_25 = registerKey("VEIN_MINER_25")
	val SILK_TOUCH = registerKey("SILK_TOUCH")
	val AUTO_SMELT = registerKey("AUTO_SMELT")
	val FORTUNE_1 = registerKey("FORTUNE_1")
	val FORTUNE_2 = registerKey("FORTUNE_2")
	val FORTUNE_3 = registerKey("FORTUNE_3")
	val POWER_CAPACITY_25 = registerKey("POWER_CAPACITY_25")
	val POWER_CAPACITY_50 = registerKey("POWER_CAPACITY_50")
	val AUTO_REPLANT = registerKey("AUTO_REPLANT")
	val AUTO_COMPOST = registerKey("AUTO_COMPOST")
	val AOE_3 = registerKey("AOE_3")
	val EXTENDED_BAR = registerKey("EXTENDED_BAR")
	val FERTILIZER_DISPENSER = registerKey("FERTILIZER_DISPENSER")
	val ENVIRONMENT = registerKey("ENVIRONMENT")
	val NIGHT_VISION = registerKey("NIGHT_VISION")
	val PRESSURE_FIELD = registerKey("PRESSURE_FIELD")
	val ROCKET_BOOSTING = registerKey("ROCKET_BOOSTING")
	val SHOCK_ABSORBING = registerKey("SHOCK_ABSORBING")
	val SPEED_BOOSTING = registerKey("SPEED_BOOSTING")
}
