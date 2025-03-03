package net.horizonsend.ion.server.features.custom.items.type.tool.mods

import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
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
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.CollectorModifier
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.chainsaw.ExtendedBar
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.drill.VeinMinerMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.hoe.FertilizerDispenser

object ItemModRegistry {
	val mods: MutableMap<String, ItemModification> = mutableMapOf()

	// Collects a square one deep based off the face of the clicked block
	val AOE_1 = registerMod(AOEDMod(radius = 1, modItem = CustomItemKeys.TOOL_MODIFICATION_RANGE_1))
	val AOE_2 = registerMod(AOEDMod(radius = 2, modItem = CustomItemKeys.TOOL_MODIFICATION_RANGE_2))

	// Mines groups of connected blocks of the same type
	val VEIN_MINER_25 = registerMod(VeinMinerMod(depth = 25, modItem = CustomItemKeys.TOOL_MODIFICATION_VEIN_MINER_25))

	// Silk touch enchantment
	val SILK_TOUCH = registerMod(SilkTouchSource)

	// Send drops through virtual furnace
	val AUTO_SMELT = registerMod(AutoSmeltModifier)

	// Fortune enchantment
	val FORTUNE_1 = registerMod(FortuneModifier(1, "#E196E1", CustomItemKeys.TOOL_MODIFICATION_FORTUNE_1))
	val FORTUNE_2 = registerMod(FortuneModifier(2, "#E164E1", CustomItemKeys.TOOL_MODIFICATION_FORTUNE_2))
	val FORTUNE_3 = registerMod(FortuneModifier(3, "#E132E1", CustomItemKeys.TOOL_MODIFICATION_FORTUNE_3))

	// Boost power capacity (x1000)
	val POWER_CAPACITY_25 = registerMod(PowerCapacityIncrease(25_000, CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_25))
	val POWER_CAPACITY_50 = registerMod(PowerCapacityIncrease(50_000, CustomItemKeys.TOOL_MODIFICATION_POWER_CAPACITY_50))

	// Auto replant crops, saplings
	val AUTO_REPLANT = registerMod(AutoReplantModifier)

	// Send drops to a virtual composter
	val AUTO_COMPOST = registerMod(AutoCompostModifier)

	//
	val COLLECTOR = registerMod(CollectorModifier)

	// AOE 3 is just for power hoes
	val AOE_3 = registerMod(AOEDMod(radius = 3, applicationPredicates = arrayOf(ApplicationPredicate.ClassPredicate(PowerHoe::class)), CustomItemKeys.TOOL_MODIFICATION_RANGE_3))

	// Longer chainsaw reach
	val EXTENDED_BAR = registerMod(ExtendedBar)

	// Dispenses bonemeal on crops
	val FERTILIZER_DISPENSER = registerMod(FertilizerDispenser)

	val ENVIRONMENT = registerMod(EnvironmentMod)
	val NIGHT_VISION = registerMod(NightVisionMod)
	val PRESSURE_FIELD = registerMod(PressureFieldMod)
	val ROCKET_BOOSTING = registerMod(RocketBoostingMod)
	val SHOCK_ABSORBING = registerMod(ShockAbsorbingMod)
	val SPEED_BOOSTING = registerMod(SpeedBoostingMod)

	private fun <T: ItemModification> registerMod(mod: T): T {
		mods[mod.identifier] = mod
		return mod
	}

	operator fun get(identifier: String): ItemModification? = mods[identifier]
}
