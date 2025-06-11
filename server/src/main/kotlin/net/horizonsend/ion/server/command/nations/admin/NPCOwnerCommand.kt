package net.horizonsend.ion.server.command.nations.admin

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import com.sk89q.worldedit.regions.CuboidRegion
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.StationRentalArea
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.spacestation.NPCSpaceStation
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.economy.misc.StationRentalAreas
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.combine
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

@CommandPermission("nations.npcowner")
@CommandAlias("npcowner")
internal object NPCOwnerCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerAsyncCompletion("npcStations") { NPCSpaceStation.all().map(NPCSpaceStation::name) }
		manager.commandCompletions.registerAsyncCompletion("rentalAreas") { context ->
			val stationName = context.getContextValueByName(String::class.java, "stationName") ?: throw InvalidCommandArgument("Station not specified!")
			val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq stationName) ?: throw InvalidCommandArgument("Station not found!")
			StationRentalArea.find(StationRentalArea::station eq station._id).map(StationRentalArea::name).toList()
		}
	}

	private fun validateTerritoryName(name: String) {
		failIf(!name.isAlphanumeric()) { "Name must be alphanumeric" }

		failIf(!NPCTerritoryOwner.none(NPCTerritoryOwner.nameQuery(name))) { "An npc owner named $name already exists" }
	}

	private fun validateStationName(name: String) {
		failIf(!name.isAlphanumeric()) { "Name must be alphanumeric" }

		failIf(!NPCSpaceStation.none(NPCTerritoryOwner.nameQuery(name))) { "An npc owner named $name already exists" }
	}

	private fun validateRentalAreaName(station: Oid<NPCSpaceStation>, name: String) {
		failIf(!name.isAlphanumeric()) { "Name must be alphanumeric" }

		failIf(!StationRentalArea.none(and(StationRentalArea::station eq station, StationRentalArea::name eq name))) { "An npc owner named $name already exists" }
	}

	private fun validateColor(red: Int, green: Int, blue: Int): Int {
		val range = 0..255
		failIf(red !in range || green !in range || blue !in range) { "Invalid color values" }

		return Color.fromRGB(red, green, blue).asRGB()
	}

	@Subcommand("territory create tradecity")
    fun onCreateTerritoryTC(sender: Player, name: String, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		validateTerritoryName(name)
		val color = validateColor(red, green, blue)
		NPCTerritoryOwner.create(territory.id, name, color, true)
		sender.success("Created npc trade city $name")
	}

	@Subcommand("territory create")
    fun onCreateTerritory(sender: Player, name: String, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		validateTerritoryName(name)
		val color = validateColor(red, green, blue)
		NPCTerritoryOwner.create(territory.id, name, color, false)
		sender.success("Created npc territory $name")
	}

	@Subcommand("territory delete")
	fun onDeleteTerritory(sender: Player) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val npcOwner = territory.npcOwner ?: fail { "${territory.name} is not owned by an npc owner" }
		val name = NPCTerritoryOwner.getName(npcOwner)
		NPCTerritoryOwner.delete(npcOwner)
		sender.success("Deleted $name")
	}

	@Subcommand("territory set color")
    fun onSetTerritoryColor(sender: Player, name: String, red: Int, green: Int, blue: Int) {
		val city = NPCTerritoryOwner.findOne(NPCTerritoryOwner::name eq name) ?: fail { "City $name not found!" }

		val color = validateColor(red, green, blue)

		NPCTerritoryOwner.updateById(city._id, setValue(NPCTerritoryOwner::color, color))
		sender.success("Set the color of $name!")
	}

	// Space stations
	@Subcommand("station create")
	fun onCreateStation(sender: Player, name: String, radius: Int, protected: Boolean) = asyncCommand(sender) {
		validateStationName(name)
		NPCSpaceStation.create(
			name,
			sender.world.name,
			sender.location.blockX,
			sender.location.blockZ,
			radius,
			protected
		)

		sender.success("Created npc station $name")
	}

	@Subcommand("station delete")
	@CommandCompletion("@npcStations")
	fun onDeleteStation(sender: Player, name: String) = asyncCommand(sender) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }
		NPCSpaceStation.delete(station._id)
		sender.success("Deleted $name")
	}

	@Subcommand("station set color")
	@CommandCompletion("@npcStations")
	fun onSetStationColor(sender: Player, name: String, red: Int, green: Int, blue: Int) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		val color = validateColor(red, green, blue)

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::color, color))
		sender.success("Set the color of $name!")
	}

	@Subcommand("station set radius")
	@CommandCompletion("@npcStations")
	fun onSetStationRadius(sender: Player, name: String, value: Int) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::radius, value))
		sender.success("Set the radius of $name!")
	}

	@Subcommand("station set description")
	@CommandCompletion("@npcStations")
	fun onSetStationDescription(sender: Player, name: String, description: String) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::dynmapDescription, description))
		sender.success("Set the description of $name!")
	}

	@Subcommand("station set protected")
	@CommandCompletion("@npcStations")
	fun onSetStationProtection(sender: Player, name: String, protected: Boolean) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::isProtected, protected))
		sender.success("Set the protection of $name!")
	}

	@Subcommand("station set location")
	@CommandCompletion("@npcStations")
	fun onSetStationLocation(sender: Player, name: String, world: World, x: Int, z: Int) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, combine(
			setValue(NPCSpaceStation::world, world.name),
			setValue(NPCSpaceStation::x, x),
			setValue(NPCSpaceStation::z, z),
		))
		sender.success("Set the location of $name!")
	}

	@Subcommand("station rentalarea create")
	@CommandCompletion("@npcStations")
	fun onSetRentalAreaStation(sender: Player, stationName: String, regionName: String, rent: Double) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq stationName) ?: fail { "Station $stationName not found!" }
		validateRentalAreaName(station._id, regionName)

		val signLocation = sender.location
		signLocation.block.state as? Sign ?: fail { "You must be standing in the name plate sign!" }

		val selection = requireSelection(sender)
		if (selection !is CuboidRegion) fail { "You must make a cuboid selection!" }

		val minPoint = Vec3i(selection.minimumPoint.x(), selection.minimumPoint.y(), selection.minimumPoint.z())
		val maxPoint = Vec3i(selection.maximumPoint.x(), selection.maximumPoint.y(), selection.maximumPoint.z())

		val new = StationRentalArea.create(regionName, station._id, sender.world.name, Vec3i(signLocation), minPoint, maxPoint, rent)
		sender.information("Created rental area $regionName in station $stationName")
	}

	@Subcommand("station rentalarea outline")
	@CommandCompletion("@npcStations @rentalAreas")
	fun outline(sender: Player, stationName: String, regionName: String, durationSeconds: Long) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq stationName) ?: fail { "Station $stationName not found!" }
		val rentalArea = StationRentalArea.findOne(and(StationRentalArea::station eq station._id, StationRentalArea::station eq station._id)) ?: fail { "Rental area $regionName not found!" }

		val points = cube(Vec3i(rentalArea.minPoint).toLocation(sender.world), Vec3i(rentalArea.maxPoint).toLocation(sender.world).add(1.0, 1.0, 1.0))

		val startTime = System.currentTimeMillis()

		sender.highlightBlock(Vec3i(rentalArea.signLocation), durationSeconds * 20)
		runnable {
			if ((System.currentTimeMillis() - startTime) > TimeUnit.SECONDS.toMillis(durationSeconds) || !sender.isOnline) {
				cancel()
				return@runnable
			}

			for (point in points) {
				// I can't think of a better way to do this
				sender.spawnParticle(Particle.SOUL_FIRE_FLAME, point, 1, 0.0, 0.0, 0.0, 0.0)
			}
		}.runTaskTimerAsynchronously(IonServer, 10L, 10L)
	}

	@Subcommand("station rentalarea clearowner")
	@CommandCompletion("@npcStations @rentalAreas")
	fun clearOwner(sender: Player, stationName: String, regionName: String) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq stationName) ?: fail { "Station $stationName not found!" }
		val rentalArea = StationRentalArea.findOne(and(StationRentalArea::station eq station._id, StationRentalArea::station eq station._id)) ?: fail { "Rental area $regionName not found!" }

		StationRentalArea.removeOwner(rentalArea._id)
	}

	@Subcommand("station rentalarea info")
	@CommandCompletion("@npcStations @rentalAreas @nothing")
	fun rentalAreaInfo(sender: Player, stationName: String, regionName: String) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq stationName) ?: fail { "Station $stationName not found!" }
		val rentalArea = StationRentalArea.findOne(and(StationRentalArea::station eq station._id, StationRentalArea::station eq station._id)) ?: fail { "Rental area $regionName not found!" }

		sender.information("Station: $stationName")
		sender.information("Rental area: $regionName")
		sender.information("Sign Location: ${rentalArea.signLocation}")
		sender.information("Min Point: ${rentalArea.minPoint}")
		sender.information("Max Point: ${rentalArea.maxPoint}")
		sender.information("Rent: ${rentalArea.rent}")
		sender.information("Owner: ${rentalArea.owner?.let(SLPlayer::getName)}")
		sender.information("Rent Balance: ${rentalArea.rentBalance}")
		sender.information("Rent last charged: ${Date(rentalArea.rentLastCharged)}")
	}

	@Subcommand("station rentalarea collectRent")
	fun collectRent(sender: CommandSender) {
		sender.information("Collected rents")
		Tasks.async {
			val duration = measureTime { StationRentalAreas.collectRents() }
			sender.information("Collection took ${duration.inWholeMilliseconds}ms")
		}
	}

	@Subcommand("station rentalarea refreshsign")
	@CommandCompletion("@npcStations @rentalAreas @nothing")
	fun refreshSign(sender: CommandSender, stationName: String, regionName: String) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq stationName) ?: fail { "Station $stationName not found!" }
		val rentalArea = StationRentalArea.findOne(and(StationRentalArea::station eq station._id, StationRentalArea::station eq station._id)) ?: fail { "Rental area $regionName not found!" }
		StationRentalAreas.refreshSign(Regions[rentalArea._id])
	}
}
