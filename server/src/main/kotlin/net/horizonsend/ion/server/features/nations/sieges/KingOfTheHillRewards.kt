package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ATAVUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BLASTER_REVOLVER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ENERGY_GREATSWORD
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.LIGHT_MACHINE_BLASTER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.HEAVY_POWER_ARMOR_BOOTS
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.HEAVY_POWER_ARMOR_CHESTPLATE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.HEAVY_POWER_ARMOR_HELMET
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.HEAVY_POWER_ARMOR_LEGGINGS
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.KOTH_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.LARGE_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MEDIUM_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MINI_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SCORDITE_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SMALL_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.VANADIUM_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.ZIRCON_BLOCK
import org.bukkit.inventory.ItemStack

object KingOfTheHillRewards {

	enum class KothType{MAJOR, MINOR, MOON}
	enum class RewardType{MATERIALS, CORES, KOTHBLOCK, BUFFS, PVP}

	//Item and its chance, amount and type
	data class KothRewardItem(
		val item: ItemStack,
		val chance: Int,
		val amount: IntRange,
		val rewardType: RewardType
	)

	//Type of Koth, its stage and reward
	data class KothReward(
		val kothType: KothType,
		val stage: Int,
		val rewards: List<KothRewardItem>
	)

	val KothRewards: List<KothReward> = listOf(
		//Stage 2 Major KOTH
		KothReward(
			KothType.MAJOR,
			2,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 2..8, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 65, 32..96, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 16..48, RewardType.MATERIALS),
				//Cores
				KothRewardItem(MINI_REACTOR_CORE.getValue().constructItemStack(), 65, 1..1, RewardType.CORES),
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 35, 1..1, RewardType.CORES)
			)
		),
		//Stage 2 Minor KOTH
		KothReward(
			KothType.MINOR,
			2,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 2..8, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 65, 16..36, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 8..24, RewardType.MATERIALS),
				//Cores
				KothRewardItem(MINI_REACTOR_CORE.getValue().constructItemStack(), 65, 1..1, RewardType.CORES),
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 35, 1..1, RewardType.CORES)
			)
		),
		//Stage 2 Moon KOTH
		KothReward(
			KothType.MOON,
			2,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 2..8, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 65, 16..36, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 8..24, RewardType.MATERIALS),
				//Cores
				KothRewardItem(MINI_REACTOR_CORE.getValue().constructItemStack(), 65, 1..1, RewardType.CORES),
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 35, 1..1, RewardType.CORES),
				//PvP Gear Rewards
				KothRewardItem(HEAVY_POWER_ARMOR_HELMET.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_CHESTPLATE.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_LEGGINGS.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_BOOTS.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(ENERGY_GREATSWORD.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(LIGHT_MACHINE_BLASTER.getValue().constructItemStack(), 25, 1..1, RewardType.PVP),
				KothRewardItem(BLASTER_REVOLVER.getValue().constructItemStack(), 25, 1..1, RewardType.PVP)
			)
		),
		//Stage 3 Major KOTH
		KothReward(
			KothType.MAJOR,
			3,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 6..12, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 25, 64..192, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 24..64, RewardType.MATERIALS),
				KothRewardItem(ZIRCON_BLOCK.getValue().constructItemStack(), 30, 24..64, RewardType.MATERIALS),
				KothRewardItem(ATAVUM_BLOCK.getValue().constructItemStack(), 10, 2..8, RewardType.MATERIALS),
				//Cores
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 45, 1..1, RewardType.CORES),
				KothRewardItem(MEDIUM_REACTOR_CORE.getValue().constructItemStack(), 45, 1..1, RewardType.CORES),
				KothRewardItem(LARGE_REACTOR_CORE.getValue().constructItemStack(), 10, 1..1, RewardType.CORES)
			)
		),
		//Stage 3 Minor KOTH
		KothReward(
			KothType.MINOR,
			3,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 6..12, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 25, 32..96, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 8..24, RewardType.MATERIALS),
				KothRewardItem(ZIRCON_BLOCK.getValue().constructItemStack(), 30, 24..64, RewardType.MATERIALS),
				//Cores
				KothRewardItem(MINI_REACTOR_CORE.getValue().constructItemStack(), 20, 1..1, RewardType.CORES),
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 50, 1..1, RewardType.CORES),
				KothRewardItem(MEDIUM_REACTOR_CORE.getValue().constructItemStack(), 30, 1..1, RewardType.CORES)
			)
		),
		//Stage 3 Moon KOTH
		KothReward(
			KothType.MOON,
			3,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 6..12, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 35, 32..96, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 8..24, RewardType.MATERIALS),
				KothRewardItem(ZIRCON_BLOCK.getValue().constructItemStack(), 30, 16..48, RewardType.MATERIALS),
				//Cores
				KothRewardItem(MINI_REACTOR_CORE.getValue().constructItemStack(), 20, 1..1, RewardType.CORES),
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 50, 1..1, RewardType.CORES),
				KothRewardItem(MEDIUM_REACTOR_CORE.getValue().constructItemStack(), 30, 1..1, RewardType.CORES),
				//PvP Gear Rewards
				KothRewardItem(HEAVY_POWER_ARMOR_HELMET.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_CHESTPLATE.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_LEGGINGS.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_BOOTS.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(ENERGY_GREATSWORD.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(LIGHT_MACHINE_BLASTER.getValue().constructItemStack(), 25, 1..1, RewardType.PVP),
				KothRewardItem(BLASTER_REVOLVER.getValue().constructItemStack(), 25, 1..1, RewardType.PVP)
			)
		),
		//Stage 4 Major KOTH
		KothReward(
			KothType.MAJOR,
			4,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 6..12, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 15, 96..256, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 15, 48..128, RewardType.MATERIALS),
				KothRewardItem(ZIRCON_BLOCK.getValue().constructItemStack(), 35, 48..128, RewardType.MATERIALS),
				KothRewardItem(ATAVUM_BLOCK.getValue().constructItemStack(), 35, 4..16, RewardType.MATERIALS),
				//Cores
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 10, 1..1, RewardType.CORES),
				KothRewardItem(MEDIUM_REACTOR_CORE.getValue().constructItemStack(), 50, 1..1, RewardType.CORES),
				KothRewardItem(LARGE_REACTOR_CORE.getValue().constructItemStack(), 40, 1..1, RewardType.CORES)
			)
		),
		//Stage 4 Minor KOTH
		KothReward(
			KothType.MINOR,
			4,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 6..12, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 15, 64..192, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 8..24, RewardType.MATERIALS),
				KothRewardItem(ZIRCON_BLOCK.getValue().constructItemStack(), 35, 24..64, RewardType.MATERIALS),
				KothRewardItem(ATAVUM_BLOCK.getValue().constructItemStack(), 15, 2..8, RewardType.MATERIALS),
				//Cores
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 30, 1..1, RewardType.CORES),
				KothRewardItem(MEDIUM_REACTOR_CORE.getValue().constructItemStack(), 60, 1..1, RewardType.CORES),
				KothRewardItem(LARGE_REACTOR_CORE.getValue().constructItemStack(), 10, 1..1, RewardType.CORES)
			)
		),
		//Stage 4 Moon KOTH
		KothReward(
			KothType.MOON,
			4,
			listOf(
				//Koth Block
				KothRewardItem(KOTH_BLOCK.getValue().constructItemStack(), 100, 6..12, RewardType.KOTHBLOCK),
				//Ores
				KothRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 15, 64..192, RewardType.MATERIALS),
				KothRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 35, 24..64, RewardType.MATERIALS),
				KothRewardItem(ZIRCON_BLOCK.getValue().constructItemStack(), 35, 24..64, RewardType.MATERIALS),
				KothRewardItem(ATAVUM_BLOCK.getValue().constructItemStack(), 15, 2..8, RewardType.MATERIALS),
				//Cores
				KothRewardItem(SMALL_REACTOR_CORE.getValue().constructItemStack(), 30, 1..1, RewardType.CORES),
				KothRewardItem(MEDIUM_REACTOR_CORE.getValue().constructItemStack(), 60, 1..1, RewardType.CORES),
				KothRewardItem(LARGE_REACTOR_CORE.getValue().constructItemStack(), 10, 1..1, RewardType.CORES),
				//PvP Gear Rewards
				KothRewardItem(HEAVY_POWER_ARMOR_HELMET.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_CHESTPLATE.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_LEGGINGS.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(HEAVY_POWER_ARMOR_BOOTS.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(ENERGY_GREATSWORD.getValue().constructItemStack(), 10, 1..1, RewardType.PVP),
				KothRewardItem(LIGHT_MACHINE_BLASTER.getValue().constructItemStack(), 25, 1..1, RewardType.PVP),
				KothRewardItem(BLASTER_REVOLVER.getValue().constructItemStack(), 25, 1..1, RewardType.PVP)
			)
		)
	)

}
