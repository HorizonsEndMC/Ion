package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.managers.ScreenManager.openScreen
import net.horizonsend.ion.server.screens.TextScreen
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("guide")
class GuideCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onCommand(sender: Player, @Default("0") page: Int) {
		if (0 > page || page > 0) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "That page does not exist.")
			return
		}

		val screen = TextScreen(miniMessage().deserialize(
			when (page) {
				0 -> "<color:#ffffff><font:horizonsend:special>\uE007\uF8FF\uE0A8</font></color><b>Welcome to <blue>Horizon’s End</blue>!</b>" +
				     "<font:horizonsend:special>\uE098<font:horizonsend:y18>Here, you can take control of" +
				     "<font:horizonsend:special>\uE096<font:horizonsend:y27>custom starships and use them" +
				     "<font:horizonsend:special>\uE09B<font:horizonsend:y36>to fight, trade, and explore." +
				     "<font:horizonsend:special>\uE08B<font:horizonsend:y54>Use the <b>/guide</b> command to" +
				     "<font:horizonsend:special>\uE08B<font:horizonsend:y63>bring up this guide." +
				     "<font:horizonsend:special>\uE05F<font:horizonsend:y72>Use <b>/links</b> for a list of" +
				     "<font:horizonsend:special>\uE078<font:horizonsend:y81>resources, such as the official" +
				     "<font:horizonsend:special>\uE09E<font:horizonsend:y90>Discord server." +
				     "<font:horizonsend:special>\uE001<font:horizonsend:y107>1"
				else -> throw IndexOutOfBoundsException()
			}) as TextComponent)

		screen.inventory.setItem(53, ItemStack(Material.STICK))

		sender.openScreen(screen)
	}
}