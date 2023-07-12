package net.horizonsend.ion.proxy.chat.channels

import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import dev.minn.jda.ktx.generics.getChannel
import litebans.api.Database
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.horizonsend.ion.common.Colors
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.chat.Channel
import net.horizonsend.ion.proxy.discord
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import net.luckperms.api.node.NodeEqualityPredicate
import java.io.File

class GlobalChannel : Channel {
	override val name = "global"
	override val prefix = null
	override val displayName = "<dark_green>Global"
	override val commands = listOf("global", "g")
	override val color: TextColor = NamedTextColor.WHITE
	override val checkPermission = false

	private val racism by lazy {
		File(IonProxy.dataFolder, IonProxy.configuration.funnyWordsFile).readLines()
	}

	override fun processMessage(player: Player, event: PlayerChatEvent): Boolean {
		if (Database.get().isPlayerMuted(player.uniqueId, null)) {
			return false
		}

		if (event.message.split(" ").any { racism.contains(it) }) {
			player.userErrorAction(
				"You can't use that word! " +
					"<italic>Staff has been notified."
			)

			IonProxy.proxy.allPlayers.filter { luckPerms.userManager.getUser(it.uniqueId)?.nodes?.any { it.key == "ion.racism-alert" } == true }
				.forEach {
					it.sendMessage(
						text().append(
							text("${it.username} used a banned word!").color(color(Colors.USER_ERROR)),
							newline(),
							text("Message: ").color(color(Colors.USER_ERROR)),
							text(event.message).color(color(Colors.SPECIAL)),
							newline(),
							newline(),
							text("[Warn]").color(color(Colors.SPECIAL))
								.clickEvent(ClickEvent.runCommand("warn ${it.username} Slurs and NSFW content are not allowed!"))
						)
					)
				}

			return false
		}

		val group = luckPerms.groupManager.getGroup("noglobal")
		if (group != null) {
			val node = luckPerms.nodeBuilderRegistry.forInheritance().group(group).value(true).build()
			val user = luckPerms.userManager.getUser(player.uniqueId)

			if (user?.data()?.contains(node, NodeEqualityPredicate.IGNORE_EXPIRY_TIME)?.asBoolean() == true) {
				player.userErrorAction("You have gtoggle on! Use /gtoggle to disable.")
				return false
			}
		}

		try {
			discord?.getChannel<GuildMessageChannel>(IonProxy.configuration.globalChannel)
				?.sendMessage("${player.username} » ${event.message}")
		} catch (e: ClassNotFoundException) {
			// ignore, plugin just isn't loaded
		}

		return true
	}
}
