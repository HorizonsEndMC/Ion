package net.horizonsend.ion.server.utilities

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Ranktrack
import net.horizonsend.ion.common.database.update
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
fun calculateRank(playerData: PlayerData): Ranktrack.Rank = playerData.ranktracktype.ranks.filter { it.experienceRequirement>=playerData.xp }.first()

fun Player.addRanktrackXP(givenxp: Int) {
	val playerData = PlayerData[this.uniqueId]
	val oldRank = calculateRank(playerData)

	playerData.update {
		xp =+ givenxp
	}
	sendRichMessage("<light_purple>Recieved <aqua>$givenxp<aqua> Ranktrack xp<light_purple>")

	val currentRank = calculateRank(playerData)
	if (currentRank != oldRank && oldRank.experienceRequirement<currentRank.experienceRequirement){
			server.sendMessage(
				MiniMessage.miniMessage().deserialize(
					"<dark_gray><red>$name<red> has leveled up to <hover:show_text:'<aqua> placeholder <aqua>'><gold> ${currentRank.displayName} <gold><reset>,<yellow> everyone congratulate them!<yellow>".trimIndent()
				)
			)
			this.sendRichMessage(
				"                        <gold>Congratulations!<gold>\n" +
           "<blue> You have leveled up and you have received <hover:show_text:'<aqua> placeholder <aqua>'><dark_purple> ${currentRank.displayName} <dark_purple><reset>\n"+
		   "<blue> Good luck to you, in your coming adventures!"
			)
		}
}