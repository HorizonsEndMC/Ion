package net.starlegacy.command.progression

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.progression.LEVEL_BALANCING
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.toCreditsString
import org.bukkit.entity.Player

object BuyXPCommand : SLCommand() {
	@CommandAlias("buyxp")
	fun onExecute(sender: Player, amount: Int, @Optional cost: Double?) {
		failIf(amount <= 0) { "Amount must be more than zero" }

		val realCost = LEVEL_BALANCING.creditsPerXP * amount
		requireMoney(sender, realCost, "purchase $amount SLXP")

		failIf(realCost != cost) {
			"Purchase $amount SLXP for ${realCost.toCreditsString()}? To confirm, do /buyxp $amount $cost"
		}

		VAULT_ECO.withdrawPlayer(sender, realCost)
		SLXP.addAsync(sender, amount)
	}
}
