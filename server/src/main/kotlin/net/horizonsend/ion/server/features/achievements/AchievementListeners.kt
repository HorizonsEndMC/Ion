package net.horizonsend.ion.server.features.achievements

import net.horizonsend.ion.server.features.starship.event.EnterPlanetEvent
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.features.starship.controllers.PlayerController
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.features.starship.event.StarshipDetectEvent
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Bukkit.getPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent

class AchievementListeners : SLEventListener() {
	@EventHandler
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		val killer = event.entity.killer ?: return // Only player kills
		val victim = event.player

		if (killer !== victim) killer.rewardAchievement(Achievement.KILL_PLAYER) // Kill a Player Achievement
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onDetectShip(event: StarshipDetectEvent) {
		event.player.rewardAchievement(Achievement.DETECT_SHIP)
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerAttemptPickupItemEvent(event: PlayerAttemptPickupItemEvent) {
		event.player.rewardAchievement(
			when (event.item.itemStack) {
				CustomItems.MINERAL_TITANIUM.singleItem() -> Achievement.ACQUIRE_TITANIUM
				CustomItems.MINERAL_ALUMINUM.singleItem() -> Achievement.ACQUIRE_ALUMINIUM
				CustomItems.MINERAL_CHETHERITE.singleItem() -> Achievement.ACQUIRE_CHETHERITE
				CustomItems.MINERAL_URANIUM.singleItem() -> Achievement.ACQUIRE_URANIUM
				else -> return
			}
		)
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerAttemptPickupItemEvent(event: InventoryDragEvent) {
		val item = event.cursor ?: return
		val player = getPlayer(event.view.player.uniqueId) ?: return

		player.rewardAchievement(
			when (item) {
				CustomItems.MINERAL_TITANIUM.singleItem() -> Achievement.ACQUIRE_TITANIUM
				CustomItems.MINERAL_ALUMINUM.singleItem() -> Achievement.ACQUIRE_ALUMINIUM
				CustomItems.MINERAL_CHETHERITE.singleItem() -> Achievement.ACQUIRE_CHETHERITE
				CustomItems.MINERAL_URANIUM.singleItem() -> Achievement.ACQUIRE_URANIUM
				else -> return
			}
		)
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onEnterPlanetEvent(event: EnterPlanetEvent) {
		val player = (event.controller as? PlayerController)?.player ?: return
		val playerData = SLPlayer[player.uniqueId]!!

		player.rewardAchievement(
			when (event.newWorld.name.lowercase()) {
				"chimgara" -> Achievement.PLANET_CHIMGARA
				"chandra" -> Achievement.PLANET_CHANDRA
				"damkoth" -> Achievement.PLANET_DAMKOTH
				"vask" -> Achievement.PLANET_VASK
				"gahara" -> Achievement.PLANET_GAHARA
				"isik" -> Achievement.PLANET_ISIK
				"krio" -> Achievement.PLANET_KRIO
				"herdoli" -> Achievement.PLANET_HERDOLI
				"ilius" -> Achievement.PLANET_ILIUS
				"aerach" -> Achievement.PLANET_AERACH
				"rubaciea" -> Achievement.PLANET_RUBACIEA
				"aret" -> Achievement.PLANET_ARET
				"luxiterna" -> Achievement.PLANET_LUXITERNA
				"turms" -> Achievement.PLANET_TURMS
				"lioda" -> Achievement.PLANET_LIODA
				"qatra" -> Achievement.PLANET_QATRA
				"kovfefe" -> Achievement.PLANET_KOVFEFE
				else -> return
			}
		)

		val achievements = playerData.achievements.map { Achievement.valueOf(it) }

		if (achievements.containsAll(
				listOf(
					Achievement.PLANET_CHANDRA,
					Achievement.PLANET_LUXITERNA,
					Achievement.PLANET_HERDOLI,
					Achievement.PLANET_RUBACIEA,
					Achievement.PLANET_ILIUS
				)
			)
		) {
			player.rewardAchievement(Achievement.SYSTEM_ASTERI)
		}

		if (achievements.containsAll(
				listOf(
					Achievement.PLANET_ARET,
					Achievement.PLANET_VASK,
					Achievement.PLANET_AERACH,
					Achievement.PLANET_GAHARA
				)
			)
		) {
			player.rewardAchievement(Achievement.SYSTEM_REGULUS)
		}

		if (achievements.containsAll(
				listOf(
					Achievement.PLANET_QATRA,
					Achievement.PLANET_LIODA,
					Achievement.PLANET_KOVFEFE,
					Achievement.PLANET_TURMS
				)
			)
		) {
			player.rewardAchievement(Achievement.SYSTEM_SIRIUS)
		}

		if (achievements.containsAll(
				listOf(
					Achievement.PLANET_DAMKOTH,
					Achievement.PLANET_CHIMGARA,
					Achievement.PLANET_KRIO,
					Achievement.PLANET_ISIK
				)
			)
		) {
			player.rewardAchievement(Achievement.SYSTEM_ILIOS)
		}
	}
}
