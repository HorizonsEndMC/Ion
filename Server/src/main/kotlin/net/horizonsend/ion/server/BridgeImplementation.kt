package net.horizonsend.ion.server

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Ranktrack
import net.horizonsend.ion.core.Bridge
import net.horizonsend.ion.server.utilities.calculateRank
import org.bukkit.entity.Player

class BridgeImplementation: Bridge {
	override val isIonPresent: Boolean = true
	override fun getLevelEquivalentForPlayer(player: Player): Int {
		val playerData = PlayerData[player.uniqueId]
		val levelValue = when(calculateRank(playerData)){
			Ranktrack.Refugee.REFUGEE -> 1

			Ranktrack.Outlaw.PIRATE -> 1
			Ranktrack.Outlaw.RAIDER -> 10
			Ranktrack.Outlaw.CORSAIR -> 20
			Ranktrack.Outlaw.MARAUDER -> 40
			Ranktrack.Outlaw.WARLORD -> 80

			Ranktrack.Privateer.PRIVATEER -> 1
			Ranktrack.Privateer.CAPTAIN-> 10
			Ranktrack.Privateer.COMMANDER -> 20
			Ranktrack.Privateer.BATTLEMASTER -> 40
			Ranktrack.Privateer.ADMIRAL -> 	80

			Ranktrack.Industrialist.COLONIST -> 1
			Ranktrack.Industrialist.BARON -> 10
			Ranktrack.Industrialist.VIZIER -> 20
			Ranktrack.Industrialist.VICEROY -> 40
			Ranktrack.Industrialist.MAGISTRATE -> 80
			else -> 1
		}
		return levelValue
	}

	override fun getRanktrackDisplayName(player: Player): String = calculateRank(PlayerData[player.uniqueId]).displayName
}