package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.extensions.sendServerError
import net.horizonsend.ion.server.extensions.sendUserError
import net.horizonsend.ion.server.legacy.managers.ScreenManager.openScreen
import net.horizonsend.ion.server.legacy.screens.BountyScreen
import net.horizonsend.ion.server.vaultEconomy
import net.starlegacy.listener.misc.ProtectionListener
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("bounty")
class BountyCommands : BaseCommand() {
	@Default
	@Suppress("Unused")
	@CommandPermission("ion.bounty.gui")
	fun onBounty(sender: Player) {
		if (!ProtectionListener.isProtectedCity(sender.location)) {
			sender.sendUserError("You must be within a protected trade city to use this command!")
			return
		}

		sender.openScreen(BountyScreen())
	}

	@Subcommand("info")
	@Suppress("Unused")
	@CommandCompletion("@players")
	@CommandPermission("ion.bounty.info")
	fun onBountyInfo(sender: CommandSender, target: String) {
		val targetData = PlayerData[target]

		if (targetData == null) {
			sender.sendUserError("Target player has not played Horizon's End!")
			return
		}

		sender.sendRichMessage("$target<gray>'s bounty is </gray>${targetData.bounty}.")
	}

	@Subcommand("add")
	@Suppress("Unused")
	@CommandCompletion("@players")
	@CommandPermission("ion.bounty.add")
	fun onBountyAdd(sender: Player, target: String, amount: Int) {
		if (amount < 1) {
			sender.sendRichMessage("<hover:show_text:\"We aren't going to make that mistake again!\"><gray>Cannot place a bounty less than 1.</gray></hover>") // Star Legacy reference :)
			return
		}

		if (vaultEconomy == null) {
			sender.sendServerError("Vault economy is not loaded! Cannot place bounty.")
			return
		}

		val targetData = PlayerData[target]

		if (targetData == null) {
			sender.sendUserError("Target player has not played Horizon's End!")
			return
		}

		val withdrawalSucceeded = vaultEconomy.withdrawPlayer(sender, amount.toDouble()).transactionSuccess()

		if (!withdrawalSucceeded) {
			sender.sendUserError("You do not have enough credits!")
			return
		}

		targetData.update {
			bounty += amount
		}

		sender.sendRichMessage("<gray>Added </gray>$amount<gray> to </gray>$${targetData.minecraftUsername}'s<gray> bounty.")
	}

	@Subcommand("clear")
	@Suppress("Unused")
	@CommandCompletion("@players")
	@CommandPermission("ion.bounty.clear")
	fun onBountyClear(sender: Player) {
		PlayerData[sender.uniqueId].update {
			acceptedBounty = null
		}

		sender.sendRichMessage("<gray>Cleared accepted bounty.")
	}

	@Suppress("Unused")
	@Subcommand("admin gui")
	@CommandPermission("ion.bounty.admin.gui")
	fun onBountyAdminGui(sender: Player) {
		sender.openScreen(BountyScreen())
	}

	@Suppress("Unused")
	@Subcommand("admin set")
	@CommandCompletion("@players")
	@CommandPermission("ion.bounty.admin.set")
	fun onBountyAdminSet(sender: CommandSender, target: String, amount: Int) {
		val targetData = PlayerData[target]

		if (targetData == null) {
			sender.sendUserError("Target player has not played Horizon's End!")
			return
		}

		targetData.update {
			bounty = amount
		}

		sender.sendRichMessage("<gray>Set </gray>${targetData.minecraftUsername}'s<gray> bounty to </gray>$amount<gray>.")
	}

	@Suppress("Unused")
	@Subcommand("admin clear")
	@CommandPermission("ion.bounty.admin.clear")
	@CommandCompletion("@players")
	fun onBountyAdminClear(sender: CommandSender, target: String) {
		val targetData = PlayerData[target]

		if (targetData == null) {
			sender.sendUserError("Target player has not played Horizon's End!")
			return
		}

		targetData.update {
			acceptedBounty = null
		}

		sender.sendRichMessage("<gray>Cleared ${targetData.minecraftUsername}'s accepted bounty.")
	}
}
