package net.starlegacy.command.misc

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.misc.addPower
import net.starlegacy.feature.misc.isPowerable
import net.starlegacy.feature.misc.removePower
import net.starlegacy.feature.misc.setPower
import net.starlegacy.util.displayName
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("battery")
@CommandPermission("machinery.battery")
object BatteryCommand : SLCommand() {
	private fun getPowerableItemInHand(sender: Player): ItemStack {
		val item = sender.inventory.itemInMainHand

		if (item == null || !isPowerable(item)) {
			throw ConditionFailedException("You must be holding a powerable item to do this!")
		}

		return item
	}

	@Suppress("Unused")
	@Subcommand("set")
	@CommandCompletion("0|10|100|1000|10000")
	fun onSet(sender: Player, amount: Int) {
		val item = getPowerableItemInHand(sender)
		setPower(item, amount)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Set power of {0} to {1}", item.displayName, amount)
	}

	@Suppress("Unused")
	@Subcommand("add")
	fun onAdd(sender: Player, amount: Int) {
		val item = getPowerableItemInHand(sender)
		addPower(item, amount)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Added {0} power to {1}", amount, item.displayName)
	}

	@Suppress("Unused")
	@Subcommand("remove")
	fun onRemove(sender: Player, amount: Int) {
		val item = getPowerableItemInHand(sender)
		removePower(item, amount)
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Removed {0} power from {1}", amount, item.displayName)
	}
}
