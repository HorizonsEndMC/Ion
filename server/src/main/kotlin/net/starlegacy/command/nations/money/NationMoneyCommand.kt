package net.starlegacy.command.nations.money

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.NationRole
import net.starlegacy.command.nations.money.MoneyCommand
import org.bukkit.entity.Player

@CommandAlias("nation|n")
internal object NationMoneyCommand : MoneyCommand<Nation>() {
	override fun requireCanDeposit(sender: Player, parent: Oid<Nation>) {
		requireNationPermission(sender, parent, NationRole.Permission.MONEY_DEPOSIT)
	}

	override fun requireCanWithdraw(sender: Player, parent: Oid<Nation>) {
		requireNationPermission(sender, parent, NationRole.Permission.MONEY_WITHDRAW)
	}

	override fun requireDefaultParent(sender: Player): Oid<Nation> {
		return requireNationIn(sender)
	}

	override fun resolveParent(name: String): Oid<Nation> = resolveNation(name)

	override fun getBalance(parent: Oid<Nation>): Int {
		return Nation.findPropById(parent, Nation::balance) ?: fail { "Failed to retrieve nation balance" }
	}

	override fun deposit(parent: Oid<Nation>, amount: Int) {
		Nation.deposit(parent, amount)
	}

	override fun withdraw(parent: Oid<Nation>, amount: Int) {
		Nation.withdraw(parent, amount)
	}

	@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
	@Subcommand("balance|money|bal")
	@CommandCompletion("@nations")
	@Description("Check how much money a nation has")
	override fun onBalance(sender: Player, @Optional nation: String?) = super.onBalance(sender, nation)

	@Subcommand("deposit")
	@Description("Give money to your nation")
	override fun onDeposit(sender: Player, amount: Int) = super.onDeposit(sender, amount)

	@Subcommand("withdraw")
	@Description("Take money from your nation")
	override fun onWithdraw(sender: Player, amount: Int) = super.onWithdraw(sender, amount)
}
