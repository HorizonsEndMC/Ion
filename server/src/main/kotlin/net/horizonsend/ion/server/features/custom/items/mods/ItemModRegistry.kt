package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.mods.general.PowerCapacityIncrease
import net.horizonsend.ion.server.features.custom.items.mods.tool.drill.AOEDrillMod
import net.horizonsend.ion.server.features.custom.items.mods.tool.drill.VeinMinerMod
import net.horizonsend.ion.server.features.custom.items.mods.tool.drops.AutoSmeltModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.drops.FortuneModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.drops.SilkTouchModifier

@Suppress("unused")
object ItemModRegistry {
	val mods: MutableMap<String, ItemModification> = mutableMapOf()

	val AOE_1 = registerMod(AOEDrillMod(radius = 1) { CustomItems.DRILL_AOE_1 })
	val AOE_2 = registerMod(AOEDrillMod(radius = 2) { CustomItems.DRILL_AOE_2 })

	val VEIN_MINER_25 = registerMod(VeinMinerMod(depth = 25) { CustomItems.VEIN_MINER_25 })

	val SILK_TOUCH = registerMod(SilkTouchModifier)

	val AUTO_SMELT = registerMod(AutoSmeltModifier)

	val FORTUNE_1 = registerMod(FortuneModifier(0, "#E196E1") { CustomItems.FORTUNE_1 })
	val FORTUNE_2 = registerMod(FortuneModifier(1, "#E164E1") { CustomItems.FORTUNE_2 })
	val FORTUNE_3 = registerMod(FortuneModifier(2, "#E132E1") { CustomItems.FORTUNE_3 })

	val POWER_CAPACITY_25 = registerMod(PowerCapacityIncrease(25_000) { CustomItems.POWER_CAPACITY_25 })
	val POWER_CAPACITY_50 = registerMod(PowerCapacityIncrease(50_000) { CustomItems.POWER_CAPACITY_50 })

	fun <T: ItemModification> registerMod(mod: T): T {
		mods[mod.identifier] = mod
		return mod
	}

	operator fun get(identifier: String): ItemModification? = mods[identifier]
}
