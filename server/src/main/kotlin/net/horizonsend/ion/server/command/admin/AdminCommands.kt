package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.DayOfWeek

@CommandAlias("capturablestation")
@CommandPermission("ion.core.capturablestation.create")
object AdminCommands : SLCommand() {
	@Suppress("unused")
	@Default
	fun capturableStationCreation(sender: Player, stationname: String, x: Int, z: Int, siegehour: Int) {
		CapturableStation.findById(
			CapturableStation.create(
				stationname,
				sender.world.name,
				x,
				z,
				siegehour,
				DayOfWeek.values().toSet()
			)
		)
			?.let { RegionCapturableStation(it) }?.let { NationsMap.addCapturableStation(it) }
		sender.success(
			"Successfully created Capturable Station ($stationname), At {$x}, {$z}, SiegeHour is {$siegehour}"
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
