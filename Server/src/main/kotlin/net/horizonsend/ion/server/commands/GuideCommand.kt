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
	fun onCommand(sender: Player, @Default("1") page: Int) {
		if (1 > page || page > 3) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "That page does not exist.")
			return
		}

		sender.sendFeedbackMessage(
			FeedbackType.SERVER_ERROR,
			"This guide is incomplete, and will be finished at a later date. However it has been left in intentionally as a sort of tech demo. If you are actually looking for a guide, please look at the Wiki: <u><click:open_url:'https://wiki.horizonsend.net/wiki/New_Player_Guide'>New Player Guide</click></u>."
		)

		val screen = TextScreen(
			miniMessage().deserialize(
				when (page) {
					1 -> "<color:#ffffff><font:horizonsend:special>\uE007\uF8FF\uE0A8</font></color><b>Welcome to <blue>Horizon’s End</blue>!</b>" +
						"<font:horizonsend:special>\uE098<font:horizonsend:y18>Here, you can take control of" +
						"<font:horizonsend:special>\uE096<font:horizonsend:y27>custom starships and use them" +
						"<font:horizonsend:special>\uE09B<font:horizonsend:y36>to fight, trade, and explore." +
						"<font:horizonsend:special>\uE08B<font:horizonsend:y54>Use the <b>/guide</b> command to" +
						"<font:horizonsend:special>\uE08B<font:horizonsend:y63>bring up this guide." +
						"<font:horizonsend:special>\uE05F<font:horizonsend:y72>Use <b>/links</b> for a list of" +
						"<font:horizonsend:special>\uE078<font:horizonsend:y81>resources, such as the official" +
						"<font:horizonsend:special>\uE09E<font:horizonsend:y90>Discord server." +
						"<font:horizonsend:special>\uE001<font:horizonsend:y106>1"

					2 -> "<color:#ffffff><font:horizonsend:special>\uE007\uF8FF\uE0A8</font></color>Upon spawning, you will find" +
						"<font:horizonsend:special>\uE088<font:horizonsend:y9>yourself aboard Prometheus " +
						"<font:horizonsend:special>\uE095<font:horizonsend:y18>Station located in the Asteri" +
						"<font:horizonsend:special>\uE08C<font:horizonsend:y27>system." +
						"<font:horizonsend:special>\uE023<font:horizonsend:y45>Ships are acquired from the ship" +
						"<font:horizonsend:special>\uE0A6<font:horizonsend:y54>dealer, which you can find by" +
						"<font:horizonsend:special>\uE093<font:horizonsend:y63>following the navigation signs." +
						"<font:horizonsend:special>\uE094<font:horizonsend:y81>Head there now, and run" +
						"<font:horizonsend:special>\uE07B<font:horizonsend:y90><b>/guide 3</b> when you arrive." +
						"<font:horizonsend:special>\uE03B<font:horizonsend:y106>2"

					3 -> "<color:#ffffff><font:horizonsend:special>\uE007\uF8FF\uE0A8</font></color>You will spawn in the rear" +
						"<font:horizonsend:special>\uE081<font:horizonsend:y9>section of the ship, so make" +
						"<font:horizonsend:special>\uE08B<font:horizonsend:y18>your way to the front. Then," +
						"<font:horizonsend:special>\uE08E<font:horizonsend:y27>using your clock (shown on the" +
						"<font:horizonsend:special>\uE09D<font:horizonsend:y36>right) left click on the Ship" +
						"<font:horizonsend:special>\uE086<font:horizonsend:y45>Computer (see below) and click" +
						"<font:horizonsend:special>\uE09A<font:horizonsend:y54>\"Redetect\"." +
						"<font:horizonsend:special>\uE035<font:horizonsend:y72>Once redetected, right click on" +
						"<font:horizonsend:special>\uE09B<font:horizonsend:y81>the Ship Computer to pilot the" +
						"<font:horizonsend:special>\uE094<font:horizonsend:y90>ship." +
						"<font:horizonsend:special>\uE000<font:horizonsend:y106>3"

					else -> throw IndexOutOfBoundsException()
				}
			) as TextComponent
		)

		screen.inventory.setItem(8, ItemStack(Material.CLOCK))
		screen.inventory.setItem(53, ItemStack(Material.STICK))

		sender.openScreen(screen)
	}
}