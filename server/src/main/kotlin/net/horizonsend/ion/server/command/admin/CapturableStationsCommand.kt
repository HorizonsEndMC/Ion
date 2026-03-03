package net.horizonsend.ion.server.command.admin

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.kyori.adventure.text.Component.newline
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.SiegeTerritory
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.misc.KothStationCache
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.nations.region.types.RegionKothZone
import net.horizonsend.ion.server.features.nations.region.types.RegionSiegeTerritory
import net.horizonsend.ion.server.features.nations.sieges.KingOfTheHills
import net.horizonsend.ion.server.miscellaneous.utils.ServerStage.getServerStage
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import java.time.DayOfWeek
import java.time.ZonedDateTime

@CommandAlias("capturablestation")
@CommandPermission("ion.core.capturablestation.create")
object CapturableStationsCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("koths") {
			return@registerAsyncCompletion KothStationCache.stations.map { it.name }
		}

		manager.commandCompletions.registerAsyncCompletion("kothtype") {
			return@registerAsyncCompletion KothType.entries.map { it.name }
		}
	}

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

@CommandAlias("kotharena")
@CommandPermission("ion.core.capturablestation.create")
object KothStationCommand : SLCommand() {
	@Subcommand("create")
	@CommandCompletion("@kothtype")
	fun kothStationCreation(sender: Player, type: KothType, stationName: String, x: Int, z: Int, siegehour: Int) = asyncCommand(sender) {
		val kothType = when (type) {
			KothType.MAJOR -> true
			KothType.MINOR -> false
		}

		KothStation.findById(
			KothStation.create(
				kothType,
				stationName,
				sender.world.name,
				x,
				z,
				siegehour,
				DayOfWeek.values().toSet(),
				mutableMapOf()
			)
		)?.let { RegionKothZone(it) }?.let { NationsMap.addKingOfTheHill(it) }

		sender.success(
			"Successfully created $type King of the Hill $stationName, At $x, $z, SiegeHour is $siegehour"
		)
	}

	@Subcommand("inititate")
	fun kothInitiation(sender: Player, kothName: String) {
		failIf(getServerStage() < 2) {"It's too early to start a KOTH!"}
		KingOfTheHills.forceActivateKoth(kothName)
		sender.success("Successfully initiated $kothName")
	}

	@Subcommand("listactive")
	fun listActiveKoths(sender: Player) {
		val koths = KingOfTheHills.getKOTHS()
		val kothLocations = mutableSetOf<RegionKothZone>()
		val message = text("Active Koths:")
		message.append(newline())
		for (koth in koths) {
			val region: RegionKothZone = Regions[koth.kothId]
			kothLocations.add(region)
		}
		for (koth in kothLocations) {
			message.append(text("Active Koth ${koth.name} at: ${koth.x}, ${koth.z}"))
			message.append(newline())
		}
		sender.sendMessage(message)
	}

	@Subcommand("listall")
	fun listAllKoths(sender: Player) {
		val allKoths = Regions.getAllOf<RegionKothZone>()
		val allKothNames = mutableSetOf<String>()
		for (koth in allKoths) {allKothNames.add(koth.name)}
		if (allKothNames.isEmpty()) sender.success("No KOTHs")
		else sender.success("All Koths: $allKothNames")
	}

	@Subcommand("delete")
	@CommandCompletion("@koths")
	fun deleteKothStation(sender: Player, stationName: String) {
		val station = KothStation.findOne(KothStation::name eq stationName)
		if (station == null) {
			sender.userError("Cannot find station $stationName")
			return
		}
		KothStation.delete(station._id)
		sender.success("Successfully deleted $stationName")
	}
}

enum class KothType {
	MAJOR,
	MINOR
}

@CommandAlias("siegeterritory")
@CommandPermission("ion.core.capturablestation.create")
object SiegeTerritoryCommand : SLCommand() {
	@Subcommand("create")
	fun create(sender: Player, territoryName: String, x: Int, z: Int, siegehour: Int) = asyncCommand(sender) {
		SiegeTerritory.findById(
			SiegeTerritory.create(
				territoryName,
				sender.world.name,
				x,
				z,
				siegehour,
				DayOfWeek.values().toSet(),
				mutableMapOf()
			)
			// TODO: add siege territories to map
		)?.let { RegionSiegeTerritory(it) }?.let { /*NationsMap.addKingOfTheHill(it)*/ }

		sender.success(
			"Successfully created Siege Territory King of the Hill $territoryName, At $x, $z, SiegeHour is $siegehour"
		)
	}

	@Subcommand("inititate")
	fun kothInitiation(sender: Player, kothName: String) {
		failIf(getServerStage() < 2) {"It's too early to start a KOTH!"}
		KingOfTheHills.forceActivateKoth(kothName)
		sender.success("Successfully initiated $kothName")
	}

	@Subcommand("listactive")
	fun listActiveKoths(sender: Player) {
		val koths = KingOfTheHills.getKOTHS()
		val kothLocations = mutableSetOf<RegionKothZone>()
		val message = text("Active Koths:")
		message.append(newline())
		for (koth in koths) {
			val region: RegionKothZone = Regions[koth.kothId]
			kothLocations.add(region)
		}
		for (koth in kothLocations) {
			message.append(text("Active Koth ${koth.name} at: ${koth.x}, ${koth.z}"))
			message.append(newline())
		}
		sender.sendMessage(message)
	}

	@Subcommand("listall")
	fun listAllKoths(sender: Player) {
		val allKoths = Regions.getAllOf<RegionKothZone>()
		val allKothNames = mutableSetOf<String>()
		for (koth in allKoths) {allKothNames.add(koth.name)}
		if (allKothNames.isEmpty()) sender.success("No KOTHs")
		else sender.success("All Koths: $allKothNames")
	}

	@Subcommand("delete")
	@CommandCompletion("@koths")
	fun deleteKothStation(sender: Player, stationName: String) {
		val station = KothStation.findOne(KothStation::name eq stationName)
		if (station == null) {
			sender.userError("Cannot find station $stationName")
			return
		}
		KothStation.delete(station._id)
		sender.success("Successfully deleted $stationName")
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
