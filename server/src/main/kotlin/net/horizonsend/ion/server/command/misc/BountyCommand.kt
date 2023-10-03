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
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.bounties.Bounties
import net.horizonsend.ion.server.features.bounties.BountiesMenu
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.repeatString
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.inc

@CommandAlias("bounty")
object BountyCommand : SLCommand() {
	@Default
	@Subcommand("menu")
	@Description("Open the bounty menu")
	@Suppress("unused")
	fun menu(sender: Player) = BountiesMenu.openMenuAsync(sender)

	@Subcommand("list")
	@Description("List your active bounties")
	@Suppress("unused")
	fun list(sender: Player) = asyncCommand(sender) {
		if (Bounties.isNotSurvival()) fail { "You can only do that on the Survival server!" }

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
	@Suppress("unused")
	fun put(sender: Player, targetName: String, amount: Double) = asyncCommand(sender) {
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

			Notify.online(
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
	@Suppress("unused")
	fun claim(sender: Player, targetName: String, @Optional amount: Double? = null) = asyncCommand(sender) {
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
	@Suppress("unused")
	fun top(sender: Player, @Optional page: Int? = null) = asyncCommand(sender) {
		if ((page ?: 1) <= 0) return@asyncCommand sender.userError("Page must not be less than or equal to zero!")

		val lineBreak = text(repeatString("=", 25)).decorate(TextDecoration.STRIKETHROUGH).color(NamedTextColor.DARK_GRAY)

		val bountiesText = text()
			.append(text("The Galaxy's Most Wanted:", NamedTextColor.RED).decorate(TextDecoration.BOLD))
			.append(newline())
			.append(lineBreak)
			.append(newline())

		val bounties = SLPlayer.all()
			.toList()
			.sortedByDescending { it.bounty }
			.map { player ->
			text()
				.append(text(player.lastKnownName, NamedTextColor.DARK_RED))
				.append(text(": ", NamedTextColor.RED))
				.append(text(player.bounty.toCreditsString(), NamedTextColor.GOLD))
				.append(newline())
				.build()
		}

		val min = minOf(bounties.size, 0 + (10 * ((page ?: 1) - 1)))
		val max = minOf(bounties.size, 10 + (10 * ((page ?: 1) - 1)))

		val sublist = bounties
			.subList(min, max)
			.toTypedArray()

		val entriesText = text()
			.append(text("Showing Entries ", NamedTextColor.RED))
			.append(text(min, NamedTextColor.GOLD))
			.append(text(" through ", NamedTextColor.RED))
			.append(text(max, NamedTextColor.GOLD))
			.append(text(" of ", NamedTextColor.RED))
			.append(text(bounties.size, NamedTextColor.GOLD))
			.append(text(".", NamedTextColor.RED))
			.build()

		val pageText = text()
			.append(lineBreak)
			.append(newline())
			.append(entriesText)
			.append(newline())
			.append(
				text("Previous", NamedTextColor.RED)
					.clickEvent(ClickEvent.runCommand("/bounty top ${maxOf(1, (page ?: 1) - 1)}"))
					.hoverEvent(text("/bounty top ${maxOf(1, (page ?: 1) - 1)}"))
			)
			.append(text("  |  ", NamedTextColor.DARK_GRAY))
			.append(
				text("Next", NamedTextColor.RED)
					.clickEvent(ClickEvent.runCommand("/bounty top ${(page ?: 1) + 1}"))
					.hoverEvent(text("/bounty top ${(page ?: 1) + 1}"))
			)


		bountiesText.append(*sublist) // Every one has a newline at the end
		bountiesText.append(pageText.build())

		sender.sendMessage(bountiesText.build())
	}

	@Subcommand("get")
	@Description("Get a player's bounty")
	@CommandCompletion("@players")
	@Default
	@Suppress("unused")
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
