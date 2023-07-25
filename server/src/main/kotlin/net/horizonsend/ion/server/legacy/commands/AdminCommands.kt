package net.horizonsend.ion.server.legacy.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
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
