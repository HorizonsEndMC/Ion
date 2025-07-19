package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterTextSpecificWidth
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.gui.invui.misc.BountyGui
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.inc

@CommandAlias("bounty")
object BountyCommand : SLCommand() {
	private fun requireBountiesEnabled() = failIf(!ConfigurationFiles.featureFlags().bounties) { "Bounties are disabled on this server!" }

	@Default
	@Subcommand("menu|gui|list")
	@Description("Open the bounty menu")
	fun menu(sender: Player) {
		requireBountiesEnabled()
		BountyGui(sender).openGui()
	}

	@Subcommand("put")
	@Description("Put a bounty on a player")
	@CommandCompletion("@players")
	@Default
	fun put(sender: Player, targetName: String, amount: Double) = asyncCommand(sender) {
		requireBountiesEnabled()
		requireEconomyEnabled()

		if (amount <= 50) fail { "The minimum amount is C50" }
		if (!ConfigurationFiles.featureFlags().bounties) fail { "Bounties are not enabled on this server!" }
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }
		if (target._id == sender.slPlayerId) fail { "You can't place a bounty on yourself!" }
		requireMoney(sender, amount)

		val hasProtection = Bukkit.getPlayer(target._id.uuid)?.hasProtection()
		if (hasProtection == true) fail { "You cannot place a bounty on a player with new player protection!" }

		// If they're offline do the more slow check
//		if (hasProtection == null) {
//			if (target._id.uuid.hasProtection().get() == true) fail { "You cannot place a bounty on a player with new player protection!" }
//		}

		val bounty = target.bounty

		Tasks.sync {
			VAULT_ECO.withdrawPlayer(sender, amount)

			Tasks.async {
				val message = template(
					text("{0} has placed a bounty of {1} on {2}. Their bounty is now {3}", HE_MEDIUM_GRAY),
					paramColor = PIRATE_SATURATED_RED,
					sender.name,
					amount.toCreditComponent(),
					targetName,
					(bounty + amount).toCreditComponent()
				)

				SLPlayer.updateById(target._id, inc(SLPlayer::bounty, amount))

				Notify.chatAndGlobal(message)
			}
		}
	}

	@Subcommand("top")
	@Description("List your active bounties")
	fun top(sender: Player, @Optional page: Int? = null) = asyncCommand(sender) {
		if ((page ?: 1) <= 0) return@asyncCommand sender.userError("Page must not be less than or equal to zero!")

		val builder = text()

		builder.append(lineBreakWithCenterTextSpecificWidth(text("Most Wanted", PIRATE_SATURATED_RED), width = 240), newline())

		val bounties = SLPlayer.all()
			.toList()
			.sortedByDescending { it.bounty }

		val body = formatPaginatedMenu(
			bounties.size,
			"/bounty top",
			page ?: 1,
			color = HE_MEDIUM_GRAY,
			footerSeparator = lineBreak(40)
		) {
			val player = bounties[it]

			template(
				text("{0}: {1}.", HE_MEDIUM_GRAY),
				paramColor = PIRATE_SATURATED_RED,
				player.lastKnownName,
				player.bounty.toCreditComponent()
			)
		}

		builder.append(body)

		sender.sendMessage(builder.build())
	}

	@Subcommand("get")
	@Description("Get a player's bounty")
	@CommandCompletion("@players")
	@Default
	fun get(sender: Player, targetName: String) = asyncCommand(sender) {
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }

		val bounty = target.bounty

		if (target.bounty > 0) {
			sender.sendMessage(template(
				text("{0}'s bounty is {1}.", HE_MEDIUM_GRAY),
				paramColor = PIRATE_SATURATED_RED,
				targetName,
				bounty.toCreditComponent()
			))
		} else {
			sender.sendMessage(template(
				text("{0} doesn't have a bounty!", HE_MEDIUM_GRAY),
				paramColor = PIRATE_SATURATED_RED,
				targetName
			))
		}
	}
}
