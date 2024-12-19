package net.horizonsend.ion.server.command.economy

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.economy.CargoCrate
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.cache.trade.CargoCrates
import net.horizonsend.ion.server.features.economy.cargotrade.CrateItems
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentBalancing
import net.horizonsend.ion.server.features.economy.cargotrade.ShipmentManager
import net.horizonsend.ion.server.features.economy.cargotrade.UnclaimedShipment
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.economy.collectors.CollectionMissions
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.Locale
import kotlin.system.measureTimeMillis

@CommandAlias("tradedebug|tdebug|tbug")
@CommandPermission("trade.debug")
object TradeDebugCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandContexts.registerContext(CargoCrate::class.java) { c: BukkitCommandExecutionContext ->
			CargoCrates[c.popFirstArg().uppercase(Locale.getDefault())]
				?: throw InvalidCommandArgument("No such crate")
		}

		registerAsyncCompletion(manager, "crates") { _ -> CargoCrates.crates.map { it.name } }
	}

	@Suppress("Unused")
	@Subcommand("rebalance")
	@Description("Reload the balance config and regenerate shipments")
	fun onRebalance(sender: CommandSender) {
		ShipmentBalancing.reload()
		onShipmentRegenerate(sender)
		sender.success(
			"Reloaded the shipment balancing config & regenerated shipments."
		)

		CollectionMissions.rebalance()
		sender.success(
			"Reloaded the collection mission balancing config & regenerated collection missions."
		)
	}

	@Suppress("Unused")
	@Subcommand("crate reload")
	fun onCrateReload(sender: CommandSender) {
		sender.sendRichMessage("<yellow>Reloading data...")
		CargoCrates.reloadData()
		val crates = CargoCrates.crates
		sender.success(
			"Reloaded. Crates (${crates.size}): ${crates.map { it.name }}"
		)
	}

	@Suppress("Unused")
	@Subcommand("crate give")
	@CommandCompletion("@crates 1|9|27")
	fun onCrateGive(sender: Player, crate: CargoCrate, amount: Int) {
		repeat(amount) { sender.world.dropItemNaturally(sender.eyeLocation, CrateItems[crate]) }
		sender.sendRichMessage("<green>Gave $amount of ${crate.name}")
	}

	@Suppress("Unused")
	@Subcommand("crate list")
	fun onCrateList(sender: CommandSender) = sender.sendRichMessage("<gold>${CargoCrates.crates.joinToString { it.name }}")

	@Subcommand("shipment regenerate")
	fun onShipmentRegenerate(sender: CommandSender) = asyncCommand(sender) {
		ShipmentManager.regenerateShipmentsAsync {
			sender.sendRichMessage("<green>Regenerated shipments for <aqua>${TradeCities.getAll().size} <green>cities.")
			asyncCommand(sender) {
				sender.sendRichMessage(
					"<dark_purple>Shipments (cities to importing cities): " +
						"(<light_purple>${
						ShipmentManager.getShipmentMap().map { (territoryId, shipments) ->
							val exporterName: String = TradeCities.getIfCity(Regions[territoryId])?.displayName
								?: "[ERR:$territoryId]"
							val importing = shipments.map { it.to.territoryId }.toSet().size
							return@map "$exporterName: $importing"
						}.joinToString()
						}<dark_purple>)"
				)
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("shipment list")
	fun onShipmentList(sender: CommandSender) = asyncCommand(sender) {
		for (city: TradeCityData in TradeCities.getAll()) {
			val shipments: List<UnclaimedShipment> = ShipmentManager.getShipmentMap()[city.territoryId] ?: listOf()

			val territory: RegionTerritory = Regions[city.territoryId]

			sender.sendRichMessage("<dark_aqua>${city.displayName} on ${territory.world} (${shipments.size}):")

			shipments.sortedByDescending { it.routeValue }.forEach { shipment ->
				sender.sendRichMessage(
					"<gold>  Crate: " + "<aqua>${CargoCrates[shipment.crate].name}" +
						"<gold> To: " + "<dark_purple>${shipment.to.displayName}" +
						"<gold> Value: " + "<yellow>${shipment.routeValue}"
				)
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("databasefreeze")
	fun onDatabaseFreeze(sender: CommandSender, seconds: Int) = asyncCommand(sender) {
		val elapsed = measureTimeMillis {
			for (i in 1..seconds) {
				Thread.sleep(1000)
				sender.information("Froze a database thread for $i seconds...")
			}
		}

		sender.information("Done! Elapsed time: $elapsed")
	}

	@Suppress("Unused")
	@Subcommand("npc fix")
	fun onNpcFix(sender: CommandSender) {
		CityNPCs.synchronizeNPCsAsync { sender.information("done") }
	}
}
