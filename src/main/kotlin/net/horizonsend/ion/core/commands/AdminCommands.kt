package net.horizonsend.ion.core.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.core.FeedbackType.SUCCESS
import net.horizonsend.ion.core.sendFeedbackMessage
import net.starlegacy.database.schema.nations.CapturableStation

import org.bukkit.entity.Player
import java.time.DayOfWeek

@CommandAlias("capturablestation")
@CommandPermission("ion.core.capturablestation.create")
object AdminCommands : BaseCommand(){
	@Default
	fun captuablestationcreation(sender: Player, stationname: String, x: Int, z: Int, siegehour: Int){
		CapturableStation.create(stationname,sender.world.toString(), x, z, siegehour, DayOfWeek.values().toSet())
		sender.sendFeedbackMessage(SUCCESS, "Successfully created Capturable Station ({0}), At {1}, {2}, SiegeHour is {3}", stationname, x, z, siegehour)
	}
}