package net.horizonsend.ion.server

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import com.google.gson.Gson
import com.mysql.cj.jdbc.Blob
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.common.database.Cryopod
import net.horizonsend.ion.common.database.DBLocation
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.userError
import net.starlegacy.util.Vec3i
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileReader
import java.nio.ByteBuffer
import java.sql.DriverManager
import java.util.*

@CommandAlias("ion")
@CommandPermission("ion.utilities")
class IonCommand : BaseCommand() {
	@Suppress("Unused")
	@Subcommand("view set")
	fun setServerViewDistance(sender: CommandSender, renderDistance: Int) {
		if (renderDistance > 32) {
			sender.userError("View distances above 32 are not supported.")
			return
		}

		if (renderDistance < 2) {
			sender.userError("View distances below 2 are not supported.")
			return
		}

		for (world in Bukkit.getWorlds()) {
			world.viewDistance = renderDistance
		}

		sender.sendMessage("View distance set to $renderDistance.")
	}

	@Suppress("Unused")
	@Subcommand("simulation set")
	fun setServerSimulationDistance(sender: CommandSender, simulationDistance: Int) {
		if (simulationDistance > 32) {
			sender.userError("Simulation distances above 32 are not supported.")
			return
		}

		if (simulationDistance < 2) {
			sender.userError("Simulation distances below 2 are not supported.")
			return
		}

		for (world in Bukkit.getWorlds()) {
			world.viewDistance = simulationDistance
		}

		sender.sendMessage("Simulation distance set to $simulationDistance.")
	}

	@Suppress("Unused")
	@Subcommand("view get")
	fun getServerViewDistance(sender: CommandSender) {
		sender.sendMessage("View distance is currently set to ${Bukkit.getWorlds()[0].viewDistance}.")
	}

	@Suppress("Unused")
	@Subcommand("simulation get")
	fun getServerSimulationDistance(sender: CommandSender) {
		sender.sendMessage("Simulation distance is currently set to ${Bukkit.getWorlds()[0].simulationDistance}.")
	}

	private data class CryoPod(val player: UUID, val world: String, val pos: Vec3i)

	@Suppress("Unused")
	@Subcommand("migratecryos")
	fun migrateCryos(sender: CommandSender) {
		sender.sendRichMessage("loading cryos")
		val folder = File(IonServer.dataFolder, "cryopods")
		val list = mutableListOf<CryoPod>()

		for (file in folder.listFiles()!!) {
			FileReader(file).use { reader ->
				list.add(Gson().fromJson(reader, CryoPod::class.java))
			}
		}

		sender.sendRichMessage("loaded cryos")

		transaction {
			list.forEach {
				val data = PlayerData[it.player] ?: run {
					sender.sendRichMessage("<red>NOT FOUND ${it.player}")
					return@forEach
				}

				Cryopod.new {
					location = DBLocation(it.world, it.pos.triple())
					owner = data
					active = false
				}
			}
		}
	}
}
