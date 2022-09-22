package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.core.events.EnterPlanetEvent
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("Unused")
class EnterPlanetListener: Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onEnterPlanetEvent(event: EnterPlanetEvent) {
		val playerData = transaction { PlayerData[event.player.uniqueId] }

		event.player.rewardAchievement(
			when (event.newworld.name.lowercase()) {
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

		val achievements = playerData.achievements

		if (achievements.containsAll(listOf(
			Achievement.PLANET_CHANDRA,
			Achievement.PLANET_LUXITERNA,
			Achievement.PLANET_HERDOLI,
			Achievement.PLANET_RUBACIEA,
			Achievement.PLANET_ILIUS
		))) event.player.rewardAchievement(Achievement.SYSTEM_ASTERI)

		if (achievements.containsAll(listOf(
			Achievement.PLANET_ARET,
			Achievement.PLANET_VASK,
			Achievement.PLANET_AERACH,
			Achievement.PLANET_GAHARA
		))) event.player.rewardAchievement(Achievement.SYSTEM_REGULUS)

		if (achievements.containsAll(listOf(
			Achievement.PLANET_QATRA,
			Achievement.PLANET_LIODA,
			Achievement.PLANET_KOVFEFE,
			Achievement.PLANET_TURMS
		))) event.player.rewardAchievement(Achievement.SYSTEM_SIRIUS)

		if (achievements.containsAll(listOf(
			Achievement.PLANET_DAMKOTH,
			Achievement.PLANET_CHIMGARA,
			Achievement.PLANET_KRIO,
			Achievement.PLANET_ISIK
		))) event.player.rewardAchievement(Achievement.SYSTEM_ILIOS)
	}
}