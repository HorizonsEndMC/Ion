package net.horizonsend.ion.server.command.admin

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.GasDepot
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import java.time.DayOfWeek

@CommandAlias("capturablestation")
@CommandPermission("ion.core.capturablestation.create")
object CapturableStationsCommand : SLCommand() {
	@Subcommand("create normal")
	fun capturableStationCreation(sender: Player, stationName: String, x: Int, z: Int, siegehour: Int) {
		CapturableStation.findById(
			CapturableStation.create(
				stationName,
				sender.world.name,
				x,
				z,
				siegehour,
				DayOfWeek.values().toSet()
			)
		)
			?.let { RegionCapturableStation(it) }?.let { NationsMap.addCapturableStation(it) }
		sender.success(
			"Successfully created Capturable Station ($stationName), At {$x}, {$z}, SiegeHour is {$siegehour}"
		)
	}

	@Subcommand("create solar")
	fun capturableStationCreation(sender: Player, stationName: String, x: Int, z: Int) {
		val id = SolarSiegeZone.create(
			stationName,
			sender.world.name,
			x,
			z
		)

		sender.success("Successfully created Solar Siege Zone ({0}), At {1}, {2}", stationName, x, z)
	}
}

@CommandAlias("gasdepot")
@CommandPermission("ion.core.gasdepot.create")
object GasDepotCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("gasDepotNames") {
			return@registerAsyncCompletion GasDepot.col.find().toList().map { it.name }
		}
	}

	@Subcommand("create")
	fun createGasDepot(sender: Player, name: String, x: Int, z: Int) {
		val id = GasDepot.create(name, sender.world.name, x, z)
		val depot = GasDepot.findById(id) ?: return sender.userError("Failed to create gas depot")
		sender.success("Successfully created Gas Depot ($name) at $x, $z in ${sender.world.name}")
	}

	@Subcommand("delete")
	@CommandCompletion("@gasDepotNames")
	fun deleteGasDepot(sender: Player, name: String) {
		val depot = GasDepot.findOne(GasDepot::name eq name)
			?: return sender.userError("Cannot find gas depot $name")
		GasDepot.delete(depot._id)
		sender.success("Successfully deleted gas depot $name")
	}

	@Subcommand("list")
	fun listGasDepots(sender: Player) {
		val depots = GasDepot.col.find().toList()
		if (depots.isEmpty()) return sender.success("No gas depots found")
		sender.success("Gas Depots: ${depots.joinToString { "${it.name} (${it.world}: ${it.x}, ${it.z})" }}")
	}

	@Subcommand("setnation")
	@CommandCompletion("@gasDepotNames @nations")
	fun setNation(sender: Player, depotName: String, nationName: String?) {
		val depot = GasDepot.findOne(GasDepot::name eq depotName)
			?: return sender.userError("Cannot find gas depot $depotName")

		if (nationName == null) {
			GasDepot.setNation(depot._id, null)
			return sender.success("Cleared nation for gas depot $depotName")
		}

		val nation = Nation.findOne(Nation::name eq nationName)
			?: return sender.userError("Cannot find nation $nationName")

		GasDepot.setNation(depot._id, nation._id)
		sender.success("Set nation of gas depot $depotName to $nationName")
	}

	@Subcommand("info")
	@CommandCompletion("@gasDepotNames")
	fun infoGasDepot(sender: Player, name: String) {
		val depot = GasDepot.findOne(GasDepot::name eq name)
			?: return sender.userError("Cannot find gas depot $name")

		val nationName = depot.nation?.let { Nation.findById(it)?.name } ?: "None"

		sender.success(
			"Gas Depot: ${depot.name} | World: ${depot.world} | Pos: ${depot.x}, ${depot.z} | " +
				"Nation: $nationName | Last Sieged: ${depot.lastSieged ?: "Never"}"
		)
	}
}

@CommandAlias("graceperiodtoggle")
@CommandPermission("graceperiodtoggle")
object GracePeriod : SLCommand() {
	private val gracePeriodFile = IonServer.dataFolder.resolve("gracePeriod")

	var isGracePeriod = gracePeriodFile.exists()
		private set

	@Default
	@Suppress("unused", "unused_parameter")
	fun onToggle(sender: CommandSender) {
		if (isGracePeriod) {
			gracePeriodFile.delete()
		} else {
			gracePeriodFile.createNewFile()
		}

		isGracePeriod = gracePeriodFile.exists()
	}
}
