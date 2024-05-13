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
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.Bukkit
import org.bukkit.Material.matchMaterial
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

		val originalText = ofChildren(
			Component.text("Welcome to space. ", NamedTextColor.RED),
			Component.text("What were you expecting? ", NamedTextColor.GOLD),
			Component.text("It's a dangerous place. ", NamedTextColor.YELLOW),
			Component.text("Thank you for investing - Go there for your rota, there for your orders, ", NamedTextColor.GREEN),
			Component.text("fill up these quotas, we'll bill for your quarters.", NamedTextColor.DARK_AQUA),
			Component.text("Report to your foreman, but watch for marauders, ", NamedTextColor.BLUE),
			Component.text("'cause if you get eaten, there's fees for your mourners!", NamedTextColor.LIGHT_PURPLE),
		)

		/*
		val originalText = ofChildren(
			Component.text("Prosperity's ", NamedTextColor.RED),
			Component.text("there ", NamedTextColor.GOLD),
			Component.text("in ", NamedTextColor.YELLOW),
			Component.text("the care of magnates, ", NamedTextColor.GREEN),
			Component.text("in Halcyon, ", NamedTextColor.DARK_AQUA),
			Component.text("heaven ", NamedTextColor.BLUE),
			Component.text("awaits!", NamedTextColor.LIGHT_PURPLE),

			Component.translatable(matchMaterial("minecraft:lapis_lazuli")!!.translationKey(), AQUA)
		)
		 */

		val text = GuiText("WE WORK TO EARN THE RIGHT")
		val componentList = originalText.wrap(DEFAULT_GUI_WIDTH - GUI_MARGIN)
		/*
		for ((index, component) in componentList.withIndex()) {
			text.add(component, line = index, verticalShift = GUI_HEADER_MARGIN)
		}
		 */
		text.addBackground()
		//text.addBackground(GuiText.GuiBackground(horizontalShift = DEFAULT_GUI_WIDTH))
		text.addBackground(GuiText.GuiBackground(horizontalShift = -DEFAULT_GUI_WIDTH - GUI_MARGIN))
		text.addBackground(GuiText.GuiBackground(horizontalShift = DEFAULT_GUI_WIDTH * 2 + GUI_MARGIN * 2))

		text.add(Component.text("WE WORK TO EARN THE RIGHT TO WORK"), line = 1, horizontalShift = 40)
		text.add(Component.text("TO EARN THE RIGHT TO WORK"), line = 2, horizontalShift = 80)
		text.add(Component.text("TO EARN THE RIGHT TO WORK"), line = 3, horizontalShift = 120)
		text.add(Component.text("TO EARN THE RIGHT TO WORK"), line = 4, horizontalShift = 160)
		text.add(Component.text("TO EARN THE RIGHT TO WORK"), line = 5, horizontalShift = 200)
		text.add(Component.text("TO EARN THE RIGHT TO WORK"), line = 6, horizontalShift = 240)
		text.add(Component.text("TO EARN THE RIGHT TO WORK"), line = 7, horizontalShift = 280)
		text.add(Component.text("TO EARN THE RIGHT TO WORK"), line = 8, horizontalShift = 320)

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
