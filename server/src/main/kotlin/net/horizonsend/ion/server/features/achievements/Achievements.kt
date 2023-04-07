package net.horizonsend.ion.server.features.achievements

import net.horizonsend.ion.common.database.PlayerAchievement
import net.horizonsend.ion.common.database.PlayerAchievement.Companion.new
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.server.miscellaneous.vaultEconomy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.starlegacy.SETTINGS
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.progression.SLXP
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

fun Player.rewardAchievement(achievement: Achievement) = transaction {
	if (!SETTINGS.master) return@transaction

	val playerData = PlayerData[uniqueId]!!
	if (transaction { playerData.achievements.find { it.achievement == achievement } } != null) return@transaction

	transaction {
		new(fun PlayerAchievement.() {
			player = playerData
			this.achievement = achievement
		})
	}

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
