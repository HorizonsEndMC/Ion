package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Statistic
import org.bukkit.entity.Player
import java.time.Duration
import kotlin.math.pow

@CommandAlias("checkprotection")
object CheckProtectionCommand : SLCommand() {

	@Default
	@Suppress("Unused")
	fun onCheckProtection(sender: Player) {
		val baseMessage = text("Protection status: ").color(TextColor.fromHexString("#b8e0d4"))

		if (sender.hasProtection()) {
			val level = SLPlayer[sender].level

			// In hours
			val playTime = sender.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000.0
			val protectionTime = 48.0.pow((100.0 - level) * 0.01)

			val remainingTime = Duration.ofHours((protectionTime - playTime).toLong())

			sender.sendMessage(
				baseMessage
					.append(text("Active").color(GOLD).decorate(TextDecoration.BOLD))
			)

			sender.sendMessage(
				text("Time remaining: ").color(TextColor.fromHexString("#eac4d5"))
					.append(text("${remainingTime.toHours()} hours").color(GOLD))
			)

		} else {
			sender.sendMessage(
				baseMessage
					.append(text("Expired").color(RED).decorate(TextDecoration.BOLD))
			)
		}
	}
}
