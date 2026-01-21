package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SCORDITE_BLOCK
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.VANADIUM_BLOCK
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.player.ActivityRewardTables.activityRewards
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.utils.get
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.setValue
import java.util.concurrent.TimeUnit

object ActivityRewards: IonServerComponent() {
	override fun onEnable() {
		Tasks.syncRepeat(0L, 20 * 60 * 3) { //Repeat every 3 min
			doActivityRewards()
		}
	}


	fun doActivityRewards() { //Async because of DB reads
		log.info("Doing activity rewards.")
		for (player in Bukkit.getOnlinePlayers()) {
			Tasks.async {
				val data = SLPlayer[player]
				val timestamp = SLPlayer.getActivityTimestamp(data._id) ?: return@async
				val currentRewardLevel = SLPlayer.getActivityRewardLevel(data._id) ?: 0
				val timeOnline = System.currentTimeMillis() - timestamp

				val newRewardLevel = when {
					timeOnline > TimeUnit.MINUTES.toMillis(120L) -> 8
					timeOnline > TimeUnit.MINUTES.toMillis(105L) -> 7
					timeOnline > TimeUnit.MINUTES.toMillis(90L) -> 6
					timeOnline > TimeUnit.MINUTES.toMillis(75L) -> 5
					timeOnline > TimeUnit.MINUTES.toMillis(60L) -> 4
					timeOnline > TimeUnit.MINUTES.toMillis(45L) -> 3
					timeOnline > TimeUnit.MINUTES.toMillis(30L) -> 2
					timeOnline > TimeUnit.MINUTES.toMillis(15L) -> 1
					else -> 0
				}

				if (newRewardLevel == 8) { //If at max reward level, reset time and go back to reward 0
					SLPlayer.updateById(data._id, setValue(SLPlayer::activityRewardTime, System.currentTimeMillis()))
					SLPlayer.updateById(data._id, setValue(SLPlayer::activityRewardLevel, 0))
				} else {
					SLPlayer.updateById(data._id, setValue(SLPlayer::activityRewardLevel, newRewardLevel))
				}

				if (newRewardLevel == currentRewardLevel || newRewardLevel == 0) return@async //Don't give the same reward again and don't bother if level 0

				//I hate vand
				val actualRewardLevel = when (newRewardLevel) {
					in 6..7 -> 5
					8 -> 6
					else -> newRewardLevel
				}

				val rewards = activityRewards.firstOrNull { it.stage == actualRewardLevel } ?: return@async
				val chanceRewards = WeightedRandomList<ItemStack>().apply {
					for (reward in rewards.chanceRewards) {
						this.addEntry(reward.item, reward.chance)
					}
				}

				Tasks.sync {
					for (item in rewards.guaranteedRewards) {
						player.inventory.addItem(item.item)
					}
					if (!chanceRewards.isEmpty()) { //.random() on an empty list throws an error
						val chancedItem = chanceRewards.random()
						player.inventory.addItem(chancedItem)
					}
					player.information("Gained activity rewards.")
				}
			}
		}
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val player = event.player
		val data = PlayerCache[player]
		val playerId = data.id
		Tasks.async {
			val timeStamp = System.currentTimeMillis()
			SLPlayer.updateById(playerId, setValue(SLPlayer::activityRewardTime, timeStamp))
			SLPlayer.updateById(playerId, setValue(SLPlayer::activityRewardLevel, 0))
			log.info("player ${player.name} logged in, setting login time to ${SLPlayer.getActivityTimestamp(playerId)}, actual time $timeStamp")
		}
	}
}

object ActivityRewardTables {
	//More Vandrayk loot tables from hell

	data class ChancedActivityRewardItem(
		val item: ItemStack,
		val chance: Int //100 is 100% guaranteed 1 is 1%
	)

	data class ActivityRewardItem(
		val item: ItemStack
	)

	data class ActivityReward(
		val stage: Int,
		val chanceRewards: List<ChancedActivityRewardItem>,
		val guaranteedRewards: List<ActivityRewardItem>
	)

	val activityRewards :List<ActivityReward> = listOf(
		ActivityReward(
			1,
			listOf(),
			listOf(
				ActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack())
			)
		),
		ActivityReward(
			2,
			listOf(
				ChancedActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(1), 1),
				ChancedActivityRewardItem(ItemStack(Material.AIR), 99) //gets nothing
			),
			listOf()
		),
		ActivityReward(
			3,
			listOf(
				ChancedActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(2), 1),
				ChancedActivityRewardItem(ItemStack(Material.AIR), 99) //gets nothing
			),
			listOf(
				ActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack())
			)
		),
		ActivityReward(
			4,
			listOf(
				ChancedActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 5),
				ChancedActivityRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 5),
				ChancedActivityRewardItem(ItemStack(Material.AIR), 90) //gets nothing
			),
			listOf(
				ActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(5))
			)
		),
		ActivityReward(
			5,
			listOf(
				ChancedActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 5),
				ChancedActivityRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 5),
				ChancedActivityRewardItem(ItemStack(Material.AIR), 90) //gets nothing
			),
			listOf(
				ActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(2))
			)
		),
		ActivityReward(
			6,
			listOf(
				ChancedActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack(), 10),
				ChancedActivityRewardItem(VANADIUM_BLOCK.getValue().constructItemStack(), 10),
				ChancedActivityRewardItem(ItemStack(Material.AIR), 80) //gets nothing
			),
			listOf(
				ActivityRewardItem(SCORDITE_BLOCK.getValue().constructItemStack())
			)
		)
	)
}
