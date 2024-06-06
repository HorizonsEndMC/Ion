package net.horizonsend.ion.server.features.custom.items.mods

import net.horizonsend.ion.server.features.custom.items.mods.general.PowerCapacityIncrease
import net.horizonsend.ion.server.features.custom.items.mods.tool.FortuneModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.SilkTouchModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.drill.AOEDrillMod
import net.horizonsend.ion.server.features.custom.items.mods.tool.drill.VeinMinerMod

object ItemModRegistry {
	val mods: MutableMap<String, ItemModification> = mutableMapOf()

	val AOE_1 = registerMod(AOEDrillMod(radius = 1))
	val AOE_2 = registerMod(AOEDrillMod(radius = 2))

	val VEIN_MINER_1 = registerMod(VeinMinerMod(depth = 5))
	val VEIN_MINER_2 = registerMod(VeinMinerMod(depth = 10))
	val VEIN_MINER_3 = registerMod(VeinMinerMod(depth = 15))

	val SILK_TOUCH = registerMod(SilkTouchModifier)

	val FORTUNE_1 = registerMod(FortuneModifier(0))
	val FORTUNE_2 = registerMod(FortuneModifier(1))
	val FORTUNE_3 = registerMod(FortuneModifier(2))

	val POWER_CAPACITY_25 = registerMod(PowerCapacityIncrease(25_000))
	val POWER_CAPACITY_50 = registerMod(PowerCapacityIncrease(50_000))

	fun <T: ItemModification> registerMod(mod: T): T {
		mods[mod.identifier] = mod
		return mod
	}

	operator fun get(identifier: String): ItemModification? = mods[identifier]
}
