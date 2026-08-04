package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.progression.Bounties
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.inc
import org.litote.kmongo.setValue

@CommandAlias("bountyadmin")
object BountyAdminCommand : SLCommand() {
	@Subcommand("set")
	@CommandPermission("ion.bounty.modify")
	@CommandCompletion("@players")
	fun set(sender: Player, targetName: String, newBounty: Double) = asyncCommand(sender) {
		val target = SLPlayer[targetName] ?: fail { "Player $targetName not found!" }

		SLPlayer.updateById(target._id, setValue(SLPlayer::bounty, newBounty))
		sender.success("$targetName's bounty was set to $newBounty")
	}

	@Subcommand("add")
	@CommandPermission("ion.bounty.modify")
	@CommandCompletion("@players")
	fun add(sender: Player, name: String, amount: Double) = asyncCommand(sender) {
		val player = SLPlayer[name] ?: fail { "Player $name not found!" }

		SLPlayer.updateById(player._id, inc(SLPlayer::bounty, amount))
		sender.success("$name's bounty was increased by $amount")
	}

	@Subcommand("collect")
	@CommandPermission("ion.bounty.modify")
	@Description("Force a bounty to be rewarded")
	@CommandCompletion("@players @players")
	fun collect(sender: Player, hunterName: String, targetName: String) = asyncCommand(sender) {
		val hunter = Bukkit.getPlayer(hunterName) ?: fail { "Player $hunterName not found!" }
		val target = Bukkit.getPlayer(targetName) ?: fail { "Player $targetName not found!" }

		Bounties.collectBounty(hunter, target.slPlayerId, targetName)
	}
}

