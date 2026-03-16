package net.horizonsend.ion.server.features.player

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarBuyFromSellOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarCollectItemFromBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarCollectMoneyFromSellOrdersEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarCreateBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarDeleteBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarDepositItemToBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarDepositItemToSellOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarWithdrawItemFromSellOrderEvent
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.event.entity.PlayerDeathEvent

object EventLogger : IonServerComponent() {
	override fun onEnable() {
		val url = ConfigurationFiles.serverConfiguration().eventLoggerWebhook ?: return
		val builder = WebhookClientBuilder(url)

		val client: WebhookClient =  try {
			builder.setThreadFactory { job ->
				val thread = Thread(job)
				thread.name = "Hello"
				thread.isDaemon = true
				thread
			}

			builder.setWait(true)

			builder.build()
		} catch (e: Exception) {
			return
		}

		listen<PlayerDeathEvent> { event ->
			val player = event.player

			DutyModeMonitor.record(
				client,
				player,
				"**died**: killer: ${event.player.killer}, nearby players: ${event.player.location.getNearbyPlayers(50.0).joinToString { it.name }}"
			)
		}

		listen<StarshipPilotedEvent> { event ->
			val player = event.player

			val starship = event.starship
			DutyModeMonitor.record(client, player, "**piloted starship**: ${starship.type} (${starship.initialBlockCount} blocks)")
		}

		listen<StarshipUnpilotEvent> { event ->
			val controller = event.controller

			if (controller !is PlayerController) return@listen

			val starship = event.starship
			DutyModeMonitor.record(client, controller.player, "**unpiloted starship**: ${starship.type} (${starship.initialBlockCount} blocks), isLockEnabled: ${event.starship.data.isLockEnabled}")
		}

		listen<BazaarBuyFromSellOrderEvent> { event ->
			val player = event.player
			val item = event.item.itemString
			val city = Regions.get<RegionTerritory>(event.item.cityTerritory).settlement?.let { SettlementCache[it].name }
			val amount = event.amount
			val subtotal = event.subtotal
			val tax = event.tax
			val total = event.total
			val wasRemote = event.wasRemote

			DutyModeMonitor.record(
				client,
				player,
				"**bought from bazaar**: $amount of $item at $city (rev: $subtotal; tax: $tax; total: $total; wasRemote: $wasRemote)"
			)
		}

		listen<BazaarDepositItemToSellOrderEvent> { event ->
			val player = event.player
			val itemString = event.itemString
			val amount = event.amount
			val city = event.city

			DutyModeMonitor.record(
				client,
				player,
				"**deposited items to sell order on bazaar**: $amount of $itemString at $city"
			)
		}

		listen<BazaarWithdrawItemFromSellOrderEvent> { event ->
			val player = event.player
			val itemString = event.itemString
			val amount = event.amount
			val city = event.city

			DutyModeMonitor.record(
				client,
				player,
				"**withdrew items from sell order on bazaar**: $amount of $itemString at $city"
			)
		}

		listen<BazaarCollectMoneyFromSellOrdersEvent> { event ->
			val player = event.player
			val amount = event.amount

			DutyModeMonitor.record(
				client,
				player,
				"**collected credits from bazaar**: $amount"
			)
		}

		listen<BazaarCreateBuyOrderEvent> { event ->
			val player = event.player
			val item = event.itemString
			val amount = event.amount
			val cost = event.cost
			val city = event.city

			DutyModeMonitor.record(
				client,
				player,
				"**created a buy order on bazaar**: $amount of $item for $cost at $city"
			)
		}

		listen<BazaarDeleteBuyOrderEvent> { event ->
			val player = event.player
			val refund = event.refund

			DutyModeMonitor.record(
				client,
				player,
				"**deleted a buy order on bazaar**: refunded $refund"
			)
		}

		listen<BazaarDepositItemToBuyOrderEvent> { event ->
			val player = event.player
			val item = event.item.itemString
			val amount = event.amount
			val subtotal = event.subtotal
			val tax = event.tax
			val total = event.total
			val city = event.city

			DutyModeMonitor.record(
				client,
				player,
				"**sold to a buy order on bazaar**: $amount of $item at $city (rev: $subtotal; tax: $tax; total: $total)"
			)
		}

		listen<BazaarCollectItemFromBuyOrderEvent> { event ->
			val player = event.player
			val item = event.itemString
			val amount = event.amount
			val city = event.city

			DutyModeMonitor.record(
				client,
				player,
				"**collected items from a buy order on bazaar**: $amount of $item at $city"
			)
		}
	}
}
