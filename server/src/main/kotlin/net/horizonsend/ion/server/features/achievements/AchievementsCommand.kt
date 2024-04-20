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
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.screens.GuiText
import net.horizonsend.ion.server.features.screens.TextScreen
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
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
		val guiText = GuiText(name)
		guiText.add(0, GuiText.TextAlignment.LEFT, 0, Component.text("Weekend chores", NamedTextColor.RED))
		guiText.add(0, GuiText.TextAlignment.RIGHT, 0, Component.text("Bruh", NamedTextColor.RED))
		guiText.add(1, GuiText.TextAlignment.LEFT, 21, Component.text("- Mop floors", NamedTextColor.GOLD))
		guiText.add(1, GuiText.TextAlignment.RIGHT, 0, Component.text("DONE", NamedTextColor.GREEN))
		guiText.add(2, GuiText.TextAlignment.LEFT, 21, Component.text("- Do laundry", NamedTextColor.YELLOW))
		guiText.add(2, GuiText.TextAlignment.RIGHT, 0, Component.text("NOT DONE", NamedTextColor.RED))
		guiText.add(3, GuiText.TextAlignment.LEFT, 21, Component.text("- Be inconspicuous", NamedTextColor.GREEN))
		guiText.add(4, GuiText.TextAlignment.LEFT, 21, Component.text("- Sleep for 8 hrs", NamedTextColor.BLUE))
		guiText.add(3, GuiText.TextAlignment.RIGHT, 0, Component.text("IN PROGRESS", NamedTextColor.GOLD))
		guiText.add(4, GuiText.TextAlignment.RIGHT, 0, Component.text("CANCELLED", NamedTextColor.DARK_RED))
		guiText.add(5, GuiText.TextAlignment.RIGHT, -26, Component.text("TEST RIGHT", NamedTextColor.DARK_PURPLE))
		guiText.add(6, GuiText.TextAlignment.CENTER, 0, Component.text("CANCELLED", NamedTextColor.DARK_RED))
		guiText.add(8, GuiText.TextAlignment.RIGHT, -8, Component.text("RIGHT", NamedTextColor.BLACK))
		guiText.add(8, GuiText.TextAlignment.CENTER, 8, Component.text("CENTER", NamedTextColor.BLACK))
		guiText.add(9, GuiText.TextAlignment.LEFT, 0, Component.text("LEFT", NamedTextColor.DARK_GRAY))
		guiText.add(9, GuiText.TextAlignment.CENTER, -21, Component.text("CTR", NamedTextColor.DARK_GRAY))
		guiText.add(10, GuiText.TextAlignment.RIGHT, 0, Component.text("R", NamedTextColor.DARK_GRAY))
		guiText.add(10, GuiText.TextAlignment.LEFT, 0, Component.text("L", NamedTextColor.DARK_GRAY))
		guiText.add(10, GuiText.TextAlignment.CENTER, 0, Component.text("CTR", NamedTextColor.DARK_GRAY))
		guiText.add(9, GuiText.TextAlignment.CENTER, 0, Component.text("CTR", NamedTextColor.DARK_GRAY))
		guiText.add(10, GuiText.TextAlignment.CENTER, 11, Component.text("CTR", NamedTextColor.DARK_GRAY))
		guiText.removeColumn(GuiText.TextAlignment.CENTER)

		sender.openScreen(TextScreen(guiText.build() as TextComponent))
	}
}
