package net.horizonsend.ion.server.features.achievements

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.miscellaneous.vaultEconomy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.starlegacy.SETTINGS
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.progression.SLXP
import org.bukkit.Sound
import org.bukkit.entity.Player

fun Player.rewardAchievement(achievement: Achievement) {
	if (!SETTINGS.master) return

	val playerData = PlayerData[this.uniqueId]
	if (playerData.achievements.contains(achievement)) return

	playerData.update {
		achievements.add(achievement)
	}

	vaultEconomy?.depositPlayer(this, achievement.creditReward.toDouble())

	SLXP.addAsync(this, achievement.experienceReward, false)

	if (achievement.chetheriteReward > 0) {
		inventory.addItem(CustomItems.MINERAL_CHETHERITE.itemStack(achievement.chetheriteReward))
	}

	showTitle(
		Title.title(
			Component.text(achievement.title).color(NamedTextColor.GOLD),
			Component.text("Achievement Granted: ${achievement.description}").color(NamedTextColor.GRAY)
		)
	)

	sendRichMessage(
		"""
		<gold>${achievement.title}
		<gray>Achievement Granted: ${achievement.description}<reset>
		Credits: ${achievement.creditReward}
		Experience: ${achievement.experienceReward}
		""".trimIndent() + if (achievement.chetheriteReward != 0) "\nChetherite: ${achievement.chetheriteReward}" else ""
	)

	this.playSound(this.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
}
