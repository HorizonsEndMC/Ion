package net.horizonsend.ion.server.legacy.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import java.time.DayOfWeek
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.SUCCESS
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.database.schema.nations.CapturableStation
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.region.types.RegionCapturableStation
import org.bukkit.entity.Player

@CommandAlias("capturablestation")
@CommandPermission("ion.core.capturablestation.create")
object AdminCommands : BaseCommand() {
	@Default
	fun captuablestationcreation(sender: Player, stationname: String, x: Int, z: Int, siegehour: Int) {
		CapturableStation.findById(
			CapturableStation.create(
				stationname,
				sender.world.toString(),
				x,
				z,
				siegehour,
				DayOfWeek.values().toSet()
			)
		)
			?.let { RegionCapturableStation(it) }?.let { NationsMap.addCapturableStation(it) }
		sender.sendFeedbackMessage(
			SUCCESS,
			"Successfully created Capturable Station ({0}), At {1}, {2}, SiegeHour is {3}",
			stationname,
			x,
			z,
			siegehour
		)
	}
}