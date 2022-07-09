package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import org.bukkit.Color
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

@CommandAlias("color|colour")
@CommandPermission("ion.cosmetic.color")
class BlasterColor : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onBlasterColorCommand(player: Player) {
		transaction { PlayerData.findById(player.uniqueId)?.cosmeticColor = null }

		player.sendFeedbackMessage(FeedbackType.SUCCESS, "Reset.")
	}

	@Default
	@Suppress("Unused")
	fun onBlasterColorCommand(player: Player, red: Int, green: Int, blue: Int) {
		if (0 > red || red > 255) return player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Red outside 0 - 255.")
		if (0 > green || green > 255) return player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Green outside 0 - 255.")
		if (0 > blue || blue > 255) return player.sendFeedbackMessage(FeedbackType.USER_ERROR, "Blue outside 0 - 255.")

		transaction { PlayerData.findById(player.uniqueId)?.cosmeticColor = Color.fromRGB(red, green, blue).asRGB() }

		player.sendFeedbackMessage(FeedbackType.SUCCESS, "Set to $red, $green, $blue.")
	}
}