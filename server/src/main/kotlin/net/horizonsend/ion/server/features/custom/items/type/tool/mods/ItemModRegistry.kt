package net.horizonsend.ion.server.features.custom.items.type.tool.mods

import net.horizonsend.ion.server.core.registries.Registry
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registries.keys.ItemModKeys
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.EnvironmentMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.NightVisionMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.PressureFieldMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.ShockAbsorbingMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.SpeedBoostingMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops.AutoSmeltModifier
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops.FortuneModifier
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops.SilkTouchSource
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.general.AOEDMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.general.AutoCompostModifier
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.general.AutoReplantModifier
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.general.PowerCapacityIncrease
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.chainsaw.ExtendedBar
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.drill.VeinMinerMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.hoe.FertilizerDispenser

class ItemModRegistry() : Registry<ItemModification>("ITEM_MODIFICATIONS") {
	override fun boostrap() {
		bootstrapToolMods()
		bootstrapArmorMods()
	}

	fun bootstrapToolMods() {
		register(ItemModKeys.AOE_1, AOEDMod(radius = 1, modItem = CustomItemKeys.TOOL_MODIFICATION_RANGE_1))
		register(ItemModKeys.AOE_2, AOEDMod(radius = 2, modItem = CustomItemKeys.TOOL_MODIFICATION_RANGE_2))
		register(ItemModKeys.AOE_3, AOEDMod(radius = 3, applicationPredicates = arrayOf(ApplicationPredicate.ClassPredicate(PowerHoe::class)), CustomItemKeys.TOOL_MODIFICATION_RANGE_3))
		register(ItemModKeys.VEIN_MINER_25, VeinMinerMod(depth = 25, modItem = CustomItemKeys.TOOL_MODIFICATION_VEIN_MINER_25))

		register(ItemModKeys.SILK_TOUCH, SilkTouchSource)
		register(ItemModKeys.AUTO_SMELT, AutoSmeltModifier)

		register(ItemModKeys.FORTUNE_1, FortuneModifier(1, "#E196E1", CustomItemKeys.TOOL_MODIFICATION_FORTUNE_1))
		register(ItemModKeys.FORTUNE_2, FortuneModifier(2, "#E164E1", CustomItemKeys.TOOL_MODIFICATION_FORTUNE_2))
		register(ItemModKeys.FORTUNE_3, FortuneModifier(3, "#E132E1", CustomItemKeys.TOOL_MODIFICATION_FORTUNE_3))

		register(ItemModKeys.POWER_CAPACITY_25, PowerCapacityIncrease(25_000, CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_25))
		register(ItemModKeys.POWER_CAPACITY_50, PowerCapacityIncrease(50_000, CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_50))

		register(ItemModKeys.AUTO_REPLANT, AutoReplantModifier)
		register(ItemModKeys.AUTO_COMPOST, AutoCompostModifier)
		register(ItemModKeys.FERTILIZER_DISPENSER, FertilizerDispenser)

		register(ItemModKeys.EXTENDED_BAR, ExtendedBar)
	}

	fun bootstrapArmorMods() {
		register(ItemModKeys.ENVIRONMENT, EnvironmentMod)
		register(ItemModKeys.NIGHT_VISION, NightVisionMod)
		register(ItemModKeys.PRESSURE_FIELD, PressureFieldMod)
		register(ItemModKeys.ROCKET_BOOSTING, RocketBoostingMod)
		register(ItemModKeys.SHOCK_ABSORBING, ShockAbsorbingMod)
		register(ItemModKeys.SPEED_BOOSTING, SpeedBoostingMod)
	}
}
