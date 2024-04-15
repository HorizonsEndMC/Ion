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
import net.horizonsend.ion.common.utils.text.shiftToLeftOfComponent
import net.horizonsend.ion.common.utils.text.shiftToLine
import net.horizonsend.ion.common.utils.text.shiftToRightGuiEdge
import net.horizonsend.ion.common.utils.text.withLeftShift
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
			Component.text("Weekend chores").color(NamedTextColor.RED).shiftToLine(1).shiftToRightGuiEdge()
				.append(Component.text("bruh").color(NamedTextColor.BLACK).withLeftShift().shiftToLeftOfComponent())
				.shiftToLeftOfComponent(),
			Component.text("- Mop floors").color(NamedTextColor.GOLD).withRightShift(21).shiftToLine(2).shiftToLeftOfComponent(),
			Component.text("- Do laundry").color(NamedTextColor.YELLOW).withRightShift(21).shiftToLine(3).shiftToLeftOfComponent(),
			Component.text("- Be inconspicuous").color(NamedTextColor.GREEN).withRightShift(21).shiftToLine(4).shiftToLeftOfComponent(),
			Component.text("- Sleep for 8 hrs").color(NamedTextColor.BLUE).withRightShift(21).shiftToLine(5).shiftToLeftOfComponent(),
		)))
	}
}
