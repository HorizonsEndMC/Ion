package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.ClaimedBounty
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.bounties.Bounties
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.setValue

object BountyAdminCommand : SLCommand() {
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
	fun add(sender: Player, name: String, amount: Double) = asyncCommand(sender) {
		val player = SLPlayer[name] ?: fail { "Player $name not found!" }

		SLPlayer.updateById(player._id, inc(SLPlayer::bounty, amount))
		sender.success("$name's bounty was increased by $amount")
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

		val result = ClaimedBounty.col.deleteOneById(claimedBounty._id)
		sender.success("Deleted ${result.deletedCount} bounty(ies)")
	}

	@Subcommand("collect")
	@CommandPermission("ion.bounty.modify")
	@Description("Force a bounty to be rewarded")
	@CommandCompletion("@players @players")
	@Suppress("unused")
	fun collect(sender: Player, hunterName: String, targetName: String) = asyncCommand(sender) {
		val hunter = Bukkit.getPlayer(hunterName) ?: fail { "Player $hunterName not found!" }
		val target = Bukkit.getPlayer(targetName) ?: fail { "Player $targetName not found!" }

		Bounties.collectBounty(hunter, target)
	}
}

