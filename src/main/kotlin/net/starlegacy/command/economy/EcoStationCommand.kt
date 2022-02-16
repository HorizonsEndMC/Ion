package net.starlegacy.command.economy

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import com.google.gson.GsonBuilder
import net.starlegacy.cache.trade.EcoStations
import net.starlegacy.command.SLCommand
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.economy.EcoStation
import net.starlegacy.util.green
import net.starlegacy.util.lightPurple
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("ecostation")
@CommandPermission("slcore.ecostation")
object EcoStationCommand : SLCommand() {
	@Subcommand("create")
	@CommandCompletion("@nothing @worlds @nothing @nothing @nothing @nothing @nothing")
	fun onCreate(sender: CommandSender, name: String, world: World, x: Int, z: Int) = asyncCommand(sender) {
		val id: Oid<EcoStation> = EcoStation.create(name, world.name, x, z)
		sender msg green("Created eco station $name at $x, $z in ${world.name} with database ID $id")
	}

	@Subcommand("set center")
	@CommandCompletion("@ecostations @nothing @nothing")
	fun onSetCenter(sender: CommandSender, ecoStation: EcoStation, x: Int, z: Int) = asyncCommand(sender) {
		EcoStation.setCenter(ecoStation._id, x, z)

		sender msg green("Updated center of ${ecoStation.name} to $x, $z")
	}

	@Subcommand("set world")
	@CommandCompletion("@ecostations @worlds")
	fun onSetCenter(sender: CommandSender, ecoStation: EcoStation, world: World) = asyncCommand(sender) {
		EcoStation.setWorld(ecoStation._id, world.name)

		sender msg green("Updated world of ${ecoStation.name} to ${world.name}")
	}

	@Subcommand("teleport|goto|tp|visit")
	@CommandCompletion("@ecostations")
	fun onTeleport(sender: Player, ecoStation: EcoStation) {
		val world: World = Bukkit.getWorld(ecoStation.world)
			?: throw ConditionFailedException("World ${ecoStation.world} not loaded!")

		val x = ecoStation.x.toDouble()
		val y = 256.0
		val z = ecoStation.z.toDouble()

		val location = Location(world, x, y, z)

		sender.teleport(location)

		sender msg green("Teleported to eco station ${ecoStation.name}")
	}

	@Subcommand("list")
	fun onList(sender: CommandSender) {
		val json: String = GsonBuilder().setPrettyPrinting().create().toJson(EcoStations.getAll())

		sender msg lightPurple(json)
	}

	@Subcommand("delete")
	@CommandCompletion("@ecostations")
	fun onDelete(sender: CommandSender, ecoStation: EcoStation) {
		EcoStation.delete(ecoStation._id)

		sender msg green("Deleted eco station ${ecoStation.name}")
	}
}
