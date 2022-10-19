package net.starlegacy.command.economy

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import kotlin.system.measureTimeMillis
import net.starlegacy.cache.trade.CargoCrates
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.economy.CargoCrate
import net.starlegacy.feature.economy.cargotrade.CrateItems
import net.starlegacy.feature.economy.cargotrade.ShipmentBalancing
import net.starlegacy.feature.economy.cargotrade.ShipmentManager
import net.starlegacy.feature.economy.cargotrade.UnclaimedShipment
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.economy.city.TradeCityData
import net.starlegacy.feature.economy.collectors.CollectionMissions
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.util.aqua
import net.starlegacy.util.darkAqua
import net.starlegacy.util.darkPurple
import net.starlegacy.util.getNBTInt
import net.starlegacy.util.gold
import net.starlegacy.util.gray
import net.starlegacy.util.green
import net.starlegacy.util.lightPurple
import net.starlegacy.util.msg
import net.starlegacy.util.plus
import net.starlegacy.util.text
import net.starlegacy.util.yellow
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("tradedebug|tdebug|tbug")
@CommandPermission("trade.debug")
object TradeDebugCommand : SLCommand() {
	@Subcommand("rebalance")
	@Description("Reload the balance config and regenerate shipments")
	fun onRebalance(sender: CommandSender) {
		ShipmentBalancing.reload()
		onShipmentRegenerate(sender)
		sender msg green("Reloaded the shipment balancing config & regenerated shipments.")

		CollectionMissions.rebalance()
		sender msg "Reloaded the collection mission balancing config & regenerated collection missions."
	}

	@Subcommand("crate reload")
	fun onCrateReload(sender: CommandSender) {
		sender msg "Reloading data...".text().yellow()
		CargoCrates.reloadData()
		val crates = CargoCrates.crates
		sender msg green("Reloaded. Crates (${crates.size}): ${crates.map { it.name }}")
	}

	@Subcommand("crate give")
	@CommandCompletion("@crates 1|9|27")
	fun onCrateGive(sender: Player, crate: CargoCrate, amount: Int) {
		repeat(amount) { sender.world.dropItemNaturally(sender.eyeLocation, CrateItems[crate]) }
		sender msg "Gave $amount of ${crate.name}".text().green()
	}

	@Subcommand("crate shipment")
	fun onCrateShipment(sender: Player) = asyncCommand(sender) {
		val item = sender.inventory.itemInMainHand

		CargoCrates[item] ?: throw InvalidCommandArgument("You aren't holding a crate!")

		val shipmentId = item.getNBTInt("shipment_id")
			?: throw InvalidCommandArgument("That crate isn't part of a shipment!")

		sender msg gold("Shipment ID: ") + yellow("$shipmentId")
	}

	@Subcommand("crate list")
	fun onCrateList(sender: CommandSender) = sender msg gray(CargoCrates.crates.joinToString { it.name })

	@Subcommand("shipment regenerate")
	fun onShipmentRegenerate(sender: CommandSender) = asyncCommand(sender) {
		ShipmentManager.regenerateShipmentsAsync {
			sender msg "Regenerated shipments for ".text().green() +
				"${TradeCities.getAll().size}".text().aqua() +
				" cities.".text().green()
			asyncCommand(sender) {
				sender msg darkPurple("Shipments (cities to importing cities): ") + lightPurple(
					"(${
						ShipmentManager.getShipmentMap().map { (territoryId, shipments) ->
							val exporterName: String = TradeCities.getIfCity(Regions[territoryId])?.displayName
								?: "[ERR:$territoryId]"
							val importing = shipments.map { it.to.territoryId }.toSet().size
							return@map "$exporterName: $importing"
						}.joinToString()
					})"
				)
			}
		}
	}

	@Subcommand("shipment list")
	fun onShipmentList(sender: CommandSender) = asyncCommand(sender) {
		for (city: TradeCityData in TradeCities.getAll()) {
			val shipments: List<UnclaimedShipment> = ShipmentManager.getShipmentMap()[city.territoryId] ?: listOf()

			val territory: RegionTerritory = Regions[city.territoryId]

			sender msg "${city.displayName} on ${territory.world} (${shipments.size}):".text().darkAqua()

			shipments.sortedByDescending { it.routeValue }.forEach { shipment ->
				sender msg "  Crate: ".text().gold() + CargoCrates[shipment.crate].name.text().aqua() +
					" To: ".text().gold() + shipment.to.displayName.text().darkPurple() +
					" Value: ".text().gold() + shipment.routeValue.toString().text().yellow()
			}
		}
	}

	@Subcommand("databasefreeze")
	fun onDatabaseFreeze(sender: CommandSender, seconds: Int) = asyncCommand(sender) {
		val elapsed = measureTimeMillis {
			for (i in 1..seconds) {
				Thread.sleep(1000)
				sender msg "Froze a database thread for $i seconds..."
			}
		}

		sender msg "Done! Elapsed time: $elapsed"
	}

	@Subcommand("npc fix")
	fun onNpcFix(sender: CommandSender) {
		CityNPCs.synchronizeNPCsAsync { sender msg "done" }
	}
}
