package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.core.events.EnterPlanetEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class EnterPlanetListener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onEnterPlanet(event: EnterPlanetEvent) {
		val playerData = transaction { PlayerData.get(event.player.uniqueId) }
			event.player.rewardAchievement(
				when(event.newworld.toString().lowercase()){
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
				if (playerData.achievements.contains(Achievement.PLANET_CHANDRA) && playerData.achievements.contains(Achievement.PLANET_LUXITERNA) && playerData.achievements.contains(Achievement.PLANET_HERDOLI) && playerData.achievements.contains(Achievement.PLANET_RUBACIEA) && playerData.achievements.contains(Achievement.PLANET_ILIUS)){
					event.player.rewardAchievement(Achievement.SYSTEM_ASTERI)
				}
				if (playerData.achievements.contains(Achievement.PLANET_ARET) && playerData.achievements.contains(Achievement.PLANET_VASK) && playerData.achievements.contains(Achievement.PLANET_AERACH) && playerData.achievements.contains(Achievement.PLANET_GAHARA)){
					event.player.rewardAchievement(Achievement.SYSTEM_REGULUS)
				}
				if (playerData.achievements.contains(Achievement.PLANET_QATRA) && playerData.achievements.contains(Achievement.PLANET_LIODA) && playerData.achievements.contains(Achievement.PLANET_KOVFEFE) && playerData.achievements.contains(Achievement.PLANET_TURMS)){
					event.player.rewardAchievement(Achievement.SYSTEM_SIRIUS)
				}
				if (playerData.achievements.contains(Achievement.PLANET_DAMKOTH) && playerData.achievements.contains(Achievement.PLANET_CHIMGARA) && playerData.achievements.contains(Achievement.PLANET_KRIO) && playerData.achievements.contains(Achievement.PLANET_ISIK)){
					event.player.rewardAchievement(Achievement.SYSTEM_ILIOS)
		}
	}
}