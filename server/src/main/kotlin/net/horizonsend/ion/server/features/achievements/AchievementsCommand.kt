package net.horizonsend.ion.server.features.achievements

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.screens.ScreenManager.openScreen
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.customGuiBackground
import net.horizonsend.ion.common.utils.text.customGuiHeader
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.rightJustify
import net.horizonsend.ion.common.utils.text.shiftToLeftOfComponent
import net.horizonsend.ion.common.utils.text.shiftToLine
import net.horizonsend.ion.common.utils.text.withRightShift
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.screens.TextScreen
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.pull

@CommandAlias("achievements")
@Suppress("Unused")
object AchievementsCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerStaticCompletion(
			manager,
			"achievements",
			Achievement.entries.joinToString("|") { it.name }
		)
	}

	@Default
	fun onAchievementsList(sender: Player) {
		sender.openScreen(AchievementsScreen(sender.name))
	}

	@Default
	fun onAchievementsList(sender: Player, target: String) {
		sender.openScreen(AchievementsScreen(target))
	}

	@Subcommand("grant")
	@CommandCompletion("@achievements @players")
	@CommandPermission("ion.achievements.grant")
	fun onAchievementGrant(sender: CommandSender, achievement: Achievement, target: String) {
		val player = Bukkit.getPlayer(target) ?: return sender.userError("Player $target must be online.")

		player.rewardAchievement(achievement)

		sender.success("Gave achievement ${achievement.name} to $target.")
	}

	@Subcommand("revoke")
	@CommandCompletion("@achievements @players")
	@CommandPermission("ion.achievements.revoke")
	fun onAchievementRevoke(sender: CommandSender, achievement: Achievement, target: String) {
		val playerData = SLPlayer[target] ?: return sender.userError("Player $target does not exist.")

		SLPlayer.updateById(playerData._id, pull(SLPlayer::achievements, achievement.name))

		sender.success("Took achievement ${achievement.name} from $target.")
	}

	@Subcommand("test")
	fun onMenuTest(sender: Player, name: String) {
		sender.openScreen(TextScreen(ofChildren(
			customGuiBackground(0xF8FF.toChar()),
			customGuiHeader(name),
			Component.text("Weekend chores", NamedTextColor.RED).withRightShift(21).shiftToLine(1)
				.rightJustify(Component.text("bruh")).shiftToLeftOfComponent(),
			Component.text("- Mop floors", NamedTextColor.GOLD).withRightShift(21).shiftToLine(2)
				.rightJustify(Component.text("DONE", NamedTextColor.GREEN)).shiftToLeftOfComponent(),
			Component.text("- Do laundry", NamedTextColor.YELLOW).withRightShift(21).shiftToLine(3)
				.rightJustify(Component.text("NOT DONE", NamedTextColor.RED)).shiftToLeftOfComponent(),
			Component.text("- Be inconspicuous", NamedTextColor.GREEN).withRightShift(21).shiftToLine(4)
				.rightJustify(Component.text("IN PROGRESS", NamedTextColor.GOLD)).shiftToLeftOfComponent(),
			Component.text("- Sleep for 8 hrs", NamedTextColor.BLUE).withRightShift(21).shiftToLine(5)
				.rightJustify(Component.text("CANCELLED", NamedTextColor.DARK_RED)).shiftToLeftOfComponent(),
		)))
	}
}
