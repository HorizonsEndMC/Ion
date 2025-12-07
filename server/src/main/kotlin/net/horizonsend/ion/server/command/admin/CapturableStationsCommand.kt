package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.nations.region.types.RegionKothZone
import net.horizonsend.ion.server.features.nations.sieges.KingOfTheHills
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.DayOfWeek
import java.time.ZonedDateTime

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

@CommandAlias("kotharena")
@CommandPermission("ion.core.capturablestation.create")
object KothStationCommand : SLCommand() {
	@Subcommand("create")
	fun kothStationCreation(sender: Player, stationName: String, x: Int, z: Int, siegehour: Int) {
		KothStation.findById(
			KothStation.create(
				stationName,
				sender.world.name,
				x,
				z,
				siegehour,
				DayOfWeek.values().toSet(),
				mutableMapOf<Oid<Nation>, Int>()
			)
		)
			?.let { RegionKothZone(it) }?.let { NationsMap.addKingOfTheHill(it) }
		sender.success(
			"Successfully created King of the Hill ($stationName), At {$x}, {$z}, SiegeHour is {$siegehour}"
		)
	}

	@Subcommand("inititate")
	@CommandCompletion("@koths")
	fun kothInitiation(sender: Player, kothName: String) {
		KingOfTheHills.forceActivateKoth(kothName)
		sender.success("Successfully initiated $kothName")
	}

	@Subcommand("listactive")
	fun listActiveKoths(sender: Player) {
		val kothLocations = Regions.getAllOf<RegionKothZone>()
			.filter { it.siegeHour == ZonedDateTime.now().hour }
		for (koth in kothLocations) {
			sender.success("Active Koth ${koth.name} at: ${koth.x}, ${koth.z}")
		}
		sender.success("WARNING: Above list will not include manually activated Koths! All active koth IDs: ${KingOfTheHills.getKOTHS()}")
	}

	@Subcommand("listall")
	fun listAllKoths(sender: Player) {
		val allKoths = Regions.getAllOf<RegionKothZone>()
		val allKothNames = allKoths.forEach { it.name }
		sender.success("All Koths: $allKothNames")
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
