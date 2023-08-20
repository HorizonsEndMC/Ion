package net.horizonsend.ion.server.command.progression

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Optional
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.server.features.progression.LEVEL_BALANCING
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import org.bukkit.entity.Player

@Suppress("Unused")
object BuyXPCommand : net.horizonsend.ion.server.command.SLCommand() {
	@CommandAlias("buyxp")
	fun onExecute(sender: Player, amount: Int, @Optional cost: Double?) {
		failIf(amount <= 0) { "Amount must be more than zero" }

		val realCost = LEVEL_BALANCING.creditsPerXP * amount
		requireMoney(sender, realCost, "purchase $amount SLXP")

		if (realCost != cost) {
			sender.userError(
				"Purchase $amount SLXP for ${realCost.toCreditsString()}?\n" +
					"To confirm, do <u><click:run_command:/buyxp $amount $realCost>/buyxp $amount $realCost</click>"
			)
			return
		}

		VAULT_ECO.withdrawPlayer(sender, realCost)
		SLXP.addAsync(sender, amount)
	}
}
