package net.starlegacy.command.nations.money

import co.aikar.commands.annotation.Optional
import net.starlegacy.command.SLCommand
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.MoneyHolder
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.msg
import net.starlegacy.util.toCreditsString
import org.bukkit.entity.Player

internal abstract class MoneyCommand<Parent : MoneyHolder> : SLCommand() {
    abstract fun requireCanDeposit(sender: Player, parent: Oid<Parent>)
    abstract fun requireCanWithdraw(sender: Player, parent: Oid<Parent>)

    abstract fun requireDefaultParent(sender: Player): Oid<Parent>

    abstract fun resolveParent(name: String): Oid<Parent>

    abstract fun getBalance(parent: Oid<Parent>): Int

    abstract fun deposit(parent: Oid<Parent>, amount: Int)
    abstract fun withdraw(parent: Oid<Parent>, amount: Int)

    private fun requireParent(sender: Player, suppliedName: String?): Oid<Parent> =
        if (suppliedName == null) requireDefaultParent(sender)
        else resolveParent(suppliedName)

    open fun onBalance(sender: Player, @Optional parentName: String?) = asyncCommand(sender) {
        val parent: Oid<Parent> = when (parentName) {
            null -> requireDefaultParent(sender)
            else -> resolveParent(parentName)
        }

        sender msg "&7Balance${parentName?.let { " of $it" } ?: ""}:&6 ${getBalance(parent)}"
    }

    open fun onDeposit(sender: Player, amount: Int) = asyncCommand(sender) {
        val parent: Oid<Parent> = requireDefaultParent(sender)
        requireCanDeposit(sender, parent)

        failIf(!VAULT_ECO.has(sender, amount.toDouble()))
        { "You don't have ${amount.toCreditsString()}! You only have ${VAULT_ECO.getBalance(sender).toCreditsString()}" }

        deposit(parent, amount)
        VAULT_ECO.withdrawPlayer(sender, amount.toDouble())

        sender msg "&aDeposited ${amount.toCreditsString()}"
    }

    open fun onWithdraw(sender: Player, amount: Int) = asyncCommand(sender) {
        failIf(amount <= 0) { "Amount must be greater than 0" }

        val parent: Oid<Parent> = requireDefaultParent(sender)
        requireCanWithdraw(sender, parent)

        val balance = getBalance(parent)
        failIf(balance < amount)
        { "There's not enough money to withdraw ${amount.toCreditsString()}. Balance is ${balance.toCreditsString()}" }

        withdraw(parent, amount)
        VAULT_ECO.depositPlayer(sender, amount.toDouble())

        sender msg "&aWithdrew ${amount.toCreditsString()}"
    }
}
