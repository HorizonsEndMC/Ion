package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.bounties.Bounties
import net.horizonsend.ion.server.features.bounties.BountiesMenu
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.toCreditsString
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.setValue

@CommandAlias("bounty")
object BountyCommand : SLCommand() {
	@Default
	@Subcommand("menu")
	@Description("Open the bounty menu")
	@Suppress("unused")
	fun menu(sender: Player) = BountiesMenu.openMenuAsync(sender)

	@Subcommand("put")
	@Description("Put a bounty on a player")
	@CommandCompletion("@players")
	@Default
	@Suppress("unused")
	fun put(sender: Player, targetName: String, amount: Double) = asyncCommand(sender) {
		if (Bounties.isNotSurvival()) fail { "You can only do that on the Survival server!" }
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }
		if (target._id == sender.slPlayerId) fail { "You can't place a bounty on yourself!" }

		val bounty = target.bounty

		Tasks.sync {
			if (VAULT_ECO.getBalance(sender) < amount) fail { "Insufficient funds." }

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
		}

		SLPlayer.updateById(target._id, inc(SLPlayer::bounty, amount))
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

	@Subcommand("set")
	@CommandPermission("ion.bounty.modify")
	@CommandCompletion("@players")
	@Suppress("unused")
	fun set(sender: Player, targetName: String, newBounty: Double) = asyncCommand(sender) {
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }

		SLPlayer.updateById(target._id, setValue(SLPlayer::bounty, newBounty))
		sender.success("$targetName's bounty was set to $newBounty")
	}

	@Subcommand("add")
	@CommandPermission("ion.bounty.modify")
	@CommandCompletion("@players")
	@Suppress("unused")
	fun add(sender: Player, name: String, newBounty: Double) = asyncCommand(sender) {
		val player = SLPlayer[name] ?: fail { "Player $name not found!" }

		SLPlayer.updateById(player._id, inc(SLPlayer::bounty, newBounty))
		sender.success("$name's bounty was set to $newBounty")
	}

	@Subcommand("find")
	@CommandPermission("ion.bounty.modify")
	@Description("Find a past or current claimed bounty")
	@CommandCompletion("@players @players")
	@Suppress("unused")
	fun find(sender: Player, hunterName: String, targetName: String) = asyncCommand(sender) {
		val hunter = SLPlayer[hunterName] ?: fail { "Player $hunterName not found!" }
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }

		val bounties = ClaimedBounty.find(and(ClaimedBounty::hunter eq hunter._id, ClaimedBounty::target eq target._id))

		sender.information(bounties.joinToString { "$hunterName claimed bounty on $targetName on ${it.claimTime}. Completed: ${it.completed}" })
	}

	@Subcommand("clear")
	@CommandPermission("ion.bounty.modify")
	@Description("Clear a player's current claimed bounty")
	@CommandCompletion("@players @players")
	@Suppress("unused")
	fun clear(sender: Player, hunterName: String, targetName: String) = asyncCommand(sender) {
		val hunter = SLPlayer[hunterName] ?: fail { "Player $hunterName not found!" }
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }

		val claimedBounty = ClaimedBounty.findOne(and(ClaimedBounty::hunter eq hunter._id, ClaimedBounty::target eq target._id)) ?: fail {
			"Bounty claimed by $hunterName on $targetName not found!"
		}

		ClaimedBounty.col.deleteOneById(claimedBounty._id)
	}
}
