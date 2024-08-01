package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.mods.drops.AutoSmeltModifier
import net.horizonsend.ion.server.features.custom.items.mods.drops.FortuneModifier
import net.horizonsend.ion.server.features.custom.items.mods.drops.SilkTouchSource
import net.horizonsend.ion.server.features.custom.items.mods.general.AOEDMod
import net.horizonsend.ion.server.features.custom.items.mods.general.AutoCompostModifier
import net.horizonsend.ion.server.features.custom.items.mods.general.AutoReplantModifier
import net.horizonsend.ion.server.features.custom.items.mods.general.PowerCapacityIncrease
import net.horizonsend.ion.server.features.custom.items.mods.tool.chainsaw.ExtendedBar
import net.horizonsend.ion.server.features.custom.items.mods.tool.drill.VeinMinerMod
import net.horizonsend.ion.server.features.custom.items.mods.tool.hoe.FertilizerDispenser
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe

@Suppress("unused")
object ItemModRegistry {
	val mods: MutableMap<String, ItemModification> = mutableMapOf()

	// Collects a square one deep based off the face of the clicked block
	val AOE_1 = registerMod(AOEDMod(radius = 1) { CustomItems.RANGE_2 })
	val AOE_2 = registerMod(AOEDMod(radius = 2) { CustomItems.RANGE_1 })

	// Mines groups of connected blocks of the same type
	val VEIN_MINER_25 = registerMod(VeinMinerMod(depth = 25) { CustomItems.VEIN_MINER_25 })

	// Silk touch enchantment
	val SILK_TOUCH = registerMod(SilkTouchSource)

	// Send drops through virtual furnace
	val AUTO_SMELT = registerMod(AutoSmeltModifier)

	// Fortune enchantment
	val FORTUNE_1 = registerMod(FortuneModifier(1, "#E196E1") { CustomItems.FORTUNE_1 })
	val FORTUNE_2 = registerMod(FortuneModifier(2, "#E164E1") { CustomItems.FORTUNE_2 })
	val FORTUNE_3 = registerMod(FortuneModifier(3, "#E132E1") { CustomItems.FORTUNE_3 })

	// Boost power capacity (x1000)
	val POWER_CAPACITY_25 = registerMod(PowerCapacityIncrease(25_000) { CustomItems.POWER_CAPACITY_25 })
	val POWER_CAPACITY_50 = registerMod(PowerCapacityIncrease(50_000) { CustomItems.POWER_CAPACITY_50 })

	// Auto replant crops, saplings
	val AUTO_REPLANT = registerMod(AutoReplantModifier)

	// Send drops to a virtual composter
	val AUTO_COMPOST = registerMod(AutoCompostModifier)

	// AOE 3 is just for power hoes
	val AOE_3 = registerMod(AOEDMod(radius = 3, applicableTo = arrayOf(PowerHoe::class)) { CustomItems.RANGE_3 })

	// Longer chainsaw reach
	val EXTENDED_BAR = registerMod(ExtendedBar)

	// Dispenses bonemeal on crops
	val FERTILIZER_DISPENSER = registerMod(FertilizerDispenser)

	private fun <T: ItemModification> registerMod(mod: T): T {
		mods[mod.identifier] = mod
		return mod
	}

	operator fun get(identifier: String): ItemModification? = mods[identifier]
}
