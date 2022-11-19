package net.starlegacy.command.progression

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.USER_ERROR
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.progression.LEVEL_BALANCING
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.toCreditsString
import org.bukkit.entity.Player

@Suppress("Unused")
object BuyXPCommand : SLCommand() {
	@CommandAlias("buyxp")
	fun onExecute(sender: Player, amount: Int, @Optional cost: Double?) {
		failIf(amount <= 0) { "Amount must be more than zero" }

		val realCost = LEVEL_BALANCING.creditsPerXP * amount
		requireMoney(sender, realCost, "purchase $amount SLXP")

		if (realCost != cost) {
			sender.sendFeedbackMessage(USER_ERROR,
				"Purchase {0} SLXP for {1}?\n" +
						"To confirm, do <u><click:run_command:/buyxp $amount $realCost>/buyxp $amount $realCost</click>",
				amount, realCost.toCreditsString())
			return
		}

		VAULT_ECO.withdrawPlayer(sender, realCost)
		SLXP.addAsync(sender, amount)
	}
}