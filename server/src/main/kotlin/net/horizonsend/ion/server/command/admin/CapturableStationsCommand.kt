package net.horizonsend.ion.server.command.admin

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.RegionalObjective
import net.horizonsend.ion.common.database.schema.nations.RegionalObjectiveType
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionCapturableStation
import net.horizonsend.ion.server.features.nations.region.types.RegionRegionalObjective
import net.horizonsend.ion.server.features.nations.sieges.RegionalObjectiveSieges
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
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

@CommandAlias("regionalobjective|robj")
@CommandPermission("ion.core.regionalobjective")
object RegionalObjectiveCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("objectiveTypes") {
			RegionalObjectiveType.entries.map { it.name }
		}
		manager.commandCompletions.registerAsyncCompletion("objectiveNames") { context ->
			val typeStr = runCatching { context.getContextValue(String::class.java, 1) }.getOrNull()
			val type = typeStr?.let { runCatching { RegionalObjectiveType.valueOf(it) }.getOrNull() }
			if (type != null) RegionalObjective.findAllOfType(type).map { it.name }
			else RegionalObjective.col.find().toList().map { it.name }
		}
	}

	@Subcommand("create")
	@CommandCompletion("@objectiveTypes @nothing @nothing @nothing")
	fun onCreate(sender: Player, type: RegionalObjectiveType, name: String, x: Int, z: Int) = asyncCommand(sender) {
		failIf(RegionalObjective.findByName(name, type) != null) {
			"A ${type.name} with name $name already exists!"
		}

		val id = RegionalObjective.create(name, sender.world.name, x, z, type)
		val objective = RegionalObjective.findById(id)
			?: return@asyncCommand sender.userError("Failed to create objective")

		Tasks.sync {
			val region = RegionRegionalObjective(objective)
			NationsMap.addRegionalObjective(region)
		}

		sender.success("Created ${type.name} '$name' at $x, $z in ${sender.world.name}")
	}

	@Subcommand("delete")
	@CommandCompletion("@objectiveTypes @objectiveNames")
	fun onDelete(sender: Player, type: RegionalObjectiveType, name: String) = asyncCommand(sender) {
		val objective = RegionalObjective.findByName(name, type)
			?: return@asyncCommand sender.userError("Cannot find ${type.name} '$name'")

		failIf(RegionalObjectiveSieges.isUnderSiege(objective._id)) {
			"Cannot delete an objective while it is under siege!"
		}

		RegionalObjective.delete(objective._id)

		Tasks.sync {
			Regions.getAllOf<RegionRegionalObjective>()
				.firstOrNull { it.id == objective._id }
				?.let { NationsMap.removeRegionalObjective(it) }
		}

		sender.success("Deleted ${type.name} '$name'")
	}

	@Subcommand("list")
	@CommandCompletion("@objectiveTypes")
	fun onList(sender: Player, type: RegionalObjectiveType) = asyncCommand(sender) {
		val objectives = RegionalObjective.findAllOfType(type)
		if (objectives.isEmpty()) return@asyncCommand sender.success("No ${type.name} objectives found")
		sender.success("${type.name} objectives: ${objectives.joinToString { "${it.name} (${it.world}: ${it.x}, ${it.z})" }}")
	}

	@Subcommand("setnation")
	@CommandCompletion("@objectiveTypes @objectiveNames @nations")
	fun onSetNation(sender: Player, type: RegionalObjectiveType, name: String, nationName: String?) = asyncCommand(sender) {
		val objective = RegionalObjective.findByName(name, type)
			?: return@asyncCommand sender.userError("Cannot find ${type.name} '$name'")

		if (nationName == null) {
			RegionalObjective.setNation(objective._id, null)
			Tasks.sync {
				Regions.getAllOf<RegionRegionalObjective>()
					.firstOrNull { it.id == objective._id }
					?.let { NationsMap.updateRegionalObjective(it) }
			}
			return@asyncCommand sender.success("Cleared nation for ${type.name} '$name'")
		}

		val nation = Nation.findOne(Nation::name eq nationName)
			?: return@asyncCommand sender.userError("Cannot find nation '$nationName'")

		RegionalObjective.setNation(objective._id, nation._id)

		Tasks.sync {
			Regions.getAllOf<RegionRegionalObjective>()
				.firstOrNull { it.id == objective._id }
				?.let { NationsMap.updateRegionalObjective(it) }
		}

		sender.success("Set nation of ${type.name} '$name' to $nationName")
	}

	@Subcommand("info")
	@CommandCompletion("@objectiveTypes @objectiveNames")
	fun onInfo(sender: Player, type: RegionalObjectiveType, name: String) = asyncCommand(sender) {
		val objective = RegionalObjective.findByName(name, type)
			?: return@asyncCommand sender.userError("Cannot find ${type.name} '$name'")

		val nationName = objective.nation?.let { Nation.findById(it)?.name } ?: "None"
		val siegeActive = RegionalObjectiveSieges.isUnderSiege(objective._id)

		sender.success(
			"${type.name}: ${objective.name} | World: ${objective.world} | " +
				"Pos: ${objective.x}, ${objective.z} | Nation: $nationName | " +
				"Last Sieged: ${objective.lastSieged ?: "Never"} | " +
				"Under Siege: $siegeActive"
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
