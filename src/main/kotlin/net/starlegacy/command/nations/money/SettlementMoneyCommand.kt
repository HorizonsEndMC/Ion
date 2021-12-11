package net.starlegacy.command.nations.money

import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.SettlementRole
import co.aikar.commands.annotation.*
import org.bukkit.entity.Player

@CommandAlias("settlement|s")
internal object SettlementMoneyCommand : MoneyCommand<Settlement>() {
    override fun requireCanDeposit(sender: Player, parent: Oid<Settlement>) {
        requireSettlementPermission(sender, parent, SettlementRole.Permission.MONEY_DEPOSIT)
    }

    override fun requireCanWithdraw(sender: Player, parent: Oid<Settlement>) {
        requireSettlementPermission(sender, parent, SettlementRole.Permission.MONEY_WITHDRAW)
    }

    override fun requireDefaultParent(sender: Player): Oid<Settlement> {
        return requireSettlementIn(sender)
    }

    override fun resolveParent(name: String): Oid<Settlement> = resolveSettlement(name)

    override fun getBalance(parent: Oid<Settlement>): Int {
        return Settlement.findPropById(parent, Settlement::balance) ?: fail { "Failed to retrieve settlement balance" }
    }

    override fun deposit(parent: Oid<Settlement>, amount: Int) {
        Settlement.deposit(parent, amount)
    }

    override fun withdraw(parent: Oid<Settlement>, amount: Int) {
        Settlement.withdraw(parent, amount)
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @Subcommand("balance|money|bal")
    @CommandCompletion("@settlements")
    @Description("Check how much money a settlement has")
    override fun onBalance(sender: Player, @Optional settlement: String?) = super.onBalance(sender, settlement)

    @Subcommand("deposit")
    @Description("Give money to your settlement")
    override fun onDeposit(sender: Player, amount: Int) = super.onDeposit(sender, amount)

    @Subcommand("withdraw")
    @Description("Take money from your settlement")
    override fun onWithdraw(sender: Player, amount: Int) = super.onWithdraw(sender, amount)
}
