package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.sendMessage
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.player.NewPlayerProtection.protectionTime
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player
import java.time.Duration
import kotlin.math.pow

@CommandAlias("checkprotection")
object CheckProtectionCommand : SLCommand() {

	@Default
	@Suppress("Unused")
	fun onCheckProtection(sender: Player) = asyncCommand(sender) {
		failIf(!ConfigurationFiles.legacySettings().master) { "Check your protection on the survival server!" }

		sender.sendMessage(protectionMessageResponse(sender))
	}

	@Subcommand("other")
	fun onCheckOtherProtection(sender: Player, target: String) = asyncCommand(sender) {
		failIf(!ConfigurationFiles.legacySettings().master) { "Check another player's protection on the survival server!" }

		val targetPlayer = Bukkit.getPlayer(target) ?: fail { "Player $target not online" }

		sender.sendMessage(protectionMessageResponse(targetPlayer))
	}

	private fun protectionMessageResponse(player: Player): Component {
		val baseMessage = template(
			text("Protection status for {0}: ", HE_LIGHT_BLUE),
			text(player.name, HE_LIGHT_BLUE),
		)

		if (player.hasProtection()) {
			val remainingTime = player.protectionTime()

			return ofChildren(
				baseMessage,
				text("Active", GOLD, TextDecoration.BOLD),
				Component.newline(),
				template(
					text("Time remaining: {0}", HE_LIGHT_GRAY),
					text("$remainingTime hours", GOLD)
				)
			)
		}

		return ofChildren(
			baseMessage,
			text("Expired", RED, TextDecoration.BOLD)
		)
	}
}
