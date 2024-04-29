package net.horizonsend.ion.server.features.achievements

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.GUI_HEADER_MARGIN
import net.horizonsend.ion.common.utils.text.GUI_MARGIN
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.pull
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

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
		openAchievementWindow(sender)
	}

	@Default
	fun onAchievementsList(sender: Player, target: String) {
		val targetPlayer = Bukkit.getPlayer(target)
		if (targetPlayer == null) {
			sender.userError("Player not found or not online")
			return
		}
		openAchievementWindow(sender, targetPlayer)
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
	fun test(sender: Player) {
		val gui = Gui.normal()
			.setStructure(
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .")
			.build()

		//val originalText = Component.text("Welcome to space. What were you expecting? It's a dangerous place. Thank you for investing - Go there for your rota, there for your orders, fill up these quotas, we'll bill for your quarters.")
		val originalText = ofChildren(
			Component.text("Welcome to space. ", NamedTextColor.RED),
			Component.text("What were you expecting? ", NamedTextColor.GOLD),
			Component.text("It's a dangerous place. ", NamedTextColor.YELLOW),
			Component.text("Thank you for investing - Go there for your rota, there for your orders, ", NamedTextColor.GREEN),
			Component.text("fill up these quotas, we'll bill for your quarters.", NamedTextColor.DARK_AQUA)
		)
		println("ORIGINAL TEXT: $originalText")
		val text = GuiText("WE WORK TO EARN THE RIGHT")
		val componentList = originalText.wrap(DEFAULT_GUI_WIDTH - GUI_MARGIN)
		for ((index, component) in componentList.withIndex()) {
			text.add(component, line = index, verticalShift = GUI_HEADER_MARGIN)
		}

		val window = Window.single()
			.setViewer(sender)
			.setTitle(AdventureComponentWrapper(text.build()))
			.setGui(gui)
			.build()

		window.open()
	}

	private fun openAchievementWindow(viewer: Player, player: Player = viewer) {
		val gui = Achievements.createAchievementGui()

		val window = Window.single()
			.setViewer(viewer)
			.setTitle(AdventureComponentWrapper(Achievements.createAchievementText(player, 0)))
			.setGui(gui)
			.build()

		fun updateTitle(): (Int, Int) -> Unit {
			return { _, currentPage ->
				window.changeTitle(AdventureComponentWrapper(Achievements.createAchievementText(player, currentPage)))
			}
		}

		gui.addPageChangeHandler(updateTitle())

		window.open()
	}
}
