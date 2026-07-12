package net.horizonsend.ion.server.command.economy

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CHETHERITE
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.ZoneOffset

@CommandAlias("buycheth")
object BuyChetheriteCommand : SLCommand() {
	private const val PURCHASE_COST = 800.0
	private const val CHETHERITE_PER_PURCHASE = 8

	@Default
	fun onBuyChetherite(sender: Player) {
		requireEconomyEnabled()

		failIf(Levels[sender] !in 1..10) {
			"Only players level 1 through 10 can use /buycheth."
		}

		val chetherite = CHETHERITE.getValue().constructItemStack(CHETHERITE_PER_PURCHASE)

		failIf(!LegacyItemUtils.canFit(sender.inventory, chetherite)) {
			"You don't have enough inventory space"
		}

		requireMoney(sender, PURCHASE_COST, "purchase $CHETHERITE_PER_PURCHASE chetherite")

		val day = currentUtcDay()
		when (SLPlayer.attemptChetheritePurchase(sender.uniqueId.slPlayerId, day)) {
			SLPlayer.ChetheritePurchaseResult.DAILY_LIMIT_REACHED -> fail {
				"You have already purchased 32 chetherite today. Your limit resets at 00:00 UTC."
			}

			SLPlayer.ChetheritePurchaseResult.LIFETIME_LIMIT_REACHED -> fail {
				"You have reached your lifetime limit of 224 chetherite."
			}

			SLPlayer.ChetheritePurchaseResult.SUCCESS -> Unit
		}

		val withdrawal = VAULT_ECO.withdrawPlayer(sender, PURCHASE_COST)
		if (!withdrawal.transactionSuccess()) {
			if (!SLPlayer.releaseChetheritePurchase(sender.uniqueId.slPlayerId, day)) {
				log.error("Failed to release ${sender.name}'s /buycheth purchase reservation after a failed withdrawal")
			}

			fail { "Could not withdraw 800c from your balance. Please try again." }
		}

		check(LegacyItemUtils.addToInventory(sender.inventory, chetherite)) {
			"Inventory capacity changed after /buycheth validation for ${sender.name}"
		}

		sender.sendMessage(
			"Purchase successful, find cheaper chetherite on the /bazaar or obtain it yourself from asteroids."
		)
	}


	private fun currentUtcDay(): String = LocalDate.now(ZoneOffset.UTC).toString()
}
