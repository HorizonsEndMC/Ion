package net.horizonsend.ion.server.features.achievements

import net.horizonsend.ion.server.miscellaneous.vaultEconomy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.starlegacy.SETTINGS
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.progression.SLXP
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import org.litote.kmongo.addToSet

fun Player.rewardAchievement(achievement: Achievement) = transaction {
	if (!SETTINGS.master) return@transaction

	val playerData = SLPlayer[this@rewardAchievement]
	if (playerData.achievements.find { it == achievement } != null) return@transaction

	SLPlayer.updateById(playerData._id, addToSet(SLPlayer::achievements, achievement))

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
