package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.progression.bounties.Bounties
import net.horizonsend.ion.server.features.progression.bounties.BountiesMenu
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.inc

@CommandAlias("bounty")
object BountyCommand : SLCommand() {
	private fun requireBountiesEnabled() = failIf(!ConfigurationFiles.featureFlags().bounties) { "Bounties are disabled on this server!" }

	@Default
	@Subcommand("menu")
	@Description("Open the bounty menu")
	fun menu(sender: Player) {
		requireBountiesEnabled()
		BountiesMenu.openMenuAsync(sender)
	}

	@Subcommand("list")
	@Description("List your active bounties")
	fun list(sender: Player) = asyncCommand(sender) {
		requireBountiesEnabled()

		val bountiesText = text()

		val bounties = ClaimedBounty.find(and(ClaimedBounty::hunter eq sender.slPlayerId, ClaimedBounty::completed eq false)).toList().map {  bounty ->
			text()
				.append(text("Bounty for ", NamedTextColor.RED))
				.append(text(SLPlayer.getName(bounty.target)!!, NamedTextColor.DARK_RED))
				.append(text(" acquired on ", NamedTextColor.RED))
				.append(text(bounty.claimTime.toString(), NamedTextColor.GOLD))
				.append(newline())
				.build()
		}.toTypedArray()

		if (bounties.isEmpty()) sender.sendMessage(text("You don't have any bounties!", NamedTextColor.RED))

		bountiesText.append(*bounties)
		sender.sendMessage(bountiesText.build())
	}

	@Subcommand("put")
	@Description("Put a bounty on a player")
	@CommandCompletion("@players")
	@Default
	fun put(sender: Player, targetName: String, amount: Double) = asyncCommand(sender) {
		requireBountiesEnabled()
		requireEconomyEnabled()

		if (amount <= 50) fail { "The minimum amount is C50" }
		if (Bounties.isNotSurvival()) fail { "You can only do that on the Survival server!" }
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

			Notify.chatAndGlobal(
				text()
					.append(text(sender.name, NamedTextColor.DARK_RED))
					.append(text(" has placed a bounty of ", NamedTextColor.RED))
					.append(text(amount.toCreditsString(), NamedTextColor.GOLD))
					.append(text(" on ", NamedTextColor.RED))
					.append(text(targetName, NamedTextColor.DARK_RED))
					.append(text(". Their bounty is now ", NamedTextColor.RED))
					.append(text((bounty + amount).toCreditsString(), NamedTextColor.GOLD))
					.build()
			)

			SLPlayer.updateById(target._id, inc(SLPlayer::bounty, amount))
		}
	}

	@Subcommand("claim")
	@Description("Acquire a bounty")
	@CommandCompletion("@players")
	fun claim(sender: Player, targetName: String, @Optional amount: Double? = null) = asyncCommand(sender) {
		requireBountiesEnabled()
		requireEconomyEnabled()

		if (Bounties.isNotSurvival()) fail { "You can only do that on the Survival server!" }
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }
		if (target._id == sender.slPlayerId) fail { "You can't claim a bounty on yourself!" }

		if (target.bounty == 0.0) fail { "$targetName doesn't have a bounty!" }

		if (amount != null) {
			val message = text()
				.append(text(targetName, NamedTextColor.DARK_RED))
				.append(text(" has a bounty of ", NamedTextColor.RED))
				.append(text(target.bounty.toCreditsString(), NamedTextColor.GOLD))
				.append(text(". You must complete this bounty within 24 hours, you cannot claim a bounty on $targetName again until ${Bounties.nextClaim}\n", NamedTextColor.RED))
				.append(text("If you wish to proceed, enter /bounty claim $targetName ${target.bounty}", NamedTextColor.RED))
				.build()

			sender.sendMessage(message)
			return@asyncCommand
		}

		Bounties.claimBounty(sender, target._id, targetName, target.bounty)
	}

	@Subcommand("top")
	@Description("List your active bounties")
	fun top(sender: Player, @Optional page: Int? = null) = asyncCommand(sender) {
		if ((page ?: 1) <= 0) return@asyncCommand sender.userError("Page must not be less than or equal to zero!")

		val builder = text()

		builder.append(text("The Galaxy's Most Wanted:", NamedTextColor.RED).decorate(TextDecoration.BOLD), newline())

		val bounties = SLPlayer.all()
			.toList()
			.sortedByDescending { it.bounty }

		val body = formatPaginatedMenu(
			bounties.size,
			"/bounty top",
			page ?: 1,
			color = NamedTextColor.RED
		) {
			val player = bounties[it]

			ofChildren(text(player.lastKnownName, NamedTextColor.DARK_RED), text(": ", NamedTextColor.RED), player.bounty.toCreditComponent())
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

		if (target.bounty >= 0) {
			sender.sendMessage(
				text()
					.append(text(targetName, NamedTextColor.DARK_RED))
					.append(text("'s bounty is ", NamedTextColor.RED))
					.append(text(bounty.toCreditsString(), NamedTextColor.GOLD))
					.build()
			)
		} else {
			sender.sendMessage(
				text()
					.append(text(targetName, NamedTextColor.DARK_RED))
					.append(text(" does not have a bounty!", NamedTextColor.RED))
					.build()
			)
		}
	}
}
