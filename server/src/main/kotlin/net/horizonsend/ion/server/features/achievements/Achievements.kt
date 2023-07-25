package net.horizonsend.ion.server.features.achievements

import net.horizonsend.ion.server.miscellaneous.utils.vaultEconomy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.LegacySettings
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.progression.SLXP
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.litote.kmongo.addToSet

fun Player.rewardAchievement(achievement: Achievement) {
	if (!LegacySettings.master) return

	val playerData = SLPlayer[this]
	if (playerData.achievements.map { Achievement.valueOf(it) }.find { it == achievement } != null) return

	SLPlayer.updateById(playerData._id, addToSet(SLPlayer::achievements, achievement.name))

	vaultEconomy?.depositPlayer(this@rewardAchievement, achievement.creditReward.toDouble())

	SLXP.addAsync(this@rewardAchievement, achievement.experienceReward, false)

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

	playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
}
