package net.starlegacy.command.progression

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.progression.advancement.Advancements
import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.Tasks
import net.starlegacy.util.hasEnoughMoney
import net.starlegacy.util.msg
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.withdrawMoney
import org.bukkit.entity.Player

object AdvanceCommand : SLCommand() {
	private val lock = Any()

	@CommandAlias("advance")
	@CommandCompletion("@advancements @nothing")
	fun onExecute(sender: Player, advancement: SLAdvancement, @Optional cost: Int?) = asyncCommand(sender) {
		// lock to prevent spamming the command, charging them at least double
		synchronized(lock) {
			if (advancement.costMultiplier < 0) {
				throw ConditionFailedException("That advancement is a display-only category advancement.")
			}

			val errorMessage = checkPrerequisites(sender, advancement)

			if (errorMessage != null) {
				throw ConditionFailedException(errorMessage)
			}

			val realCost = advancement.getMoneyCost()

			if (cost != realCost) {
				throw InvalidCommandArgument(
					"You need to confirm by specifying the cost." +
						"This advancement costs ${realCost.toCreditsString()}. " +
						"Confirm that you wish to purchase it using /advance $advancement $realCost"
				)
			}

			Tasks.sync { sender.withdrawMoney(realCost) } // economy isn't thread safe :(

			// run blocking so this thread is blocked so they can't spam it
			Advancements.giveAdvancementBlocking(sender.uniqueId, advancement)
			sender msg "&2Purchased advancement &b$advancement &2for &e${realCost.toCreditsString()}&2"
		}
	}

	/**
	 * Prerequisites for unlocking an advancement:
	 * #1 Not already having the advancement
	 * #2 Having all of its required parents unlocked, if applicable
	 * #3 Having enough money to spend on it
	 * @return An error message if a prerequisite was not met, else null
	 */
	private fun checkPrerequisites(player: Player, advancement: SLAdvancement): String? {
		// #1
		if (Advancements.has(player, advancement)) {
			return "You already have the advancement $advancement!"
		}

		// #2
		if (!advancement.hasParents(player)) {
			return "You don't have all of that advancement's required advancements!"
		}

		// #3
		val cost = advancement.getMoneyCost()

		if (!player.hasEnoughMoney(cost)) {
			return "You don't have enough money to purchase that advancement! It costs ${cost.toCreditsString()}."
		}

		// all clear
		return null
	}
}
