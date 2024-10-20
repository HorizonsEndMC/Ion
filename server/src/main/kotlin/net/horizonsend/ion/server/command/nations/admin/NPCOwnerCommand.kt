package net.horizonsend.ion.server.command.nations.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.spacestation.NPCSpaceStation
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

@CommandPermission("nations.npcowner")
@CommandAlias("npcowner")
internal object NPCOwnerCommand : SLCommand() {
	private fun validateTerritoryName(name: String) {
		failIf(!name.isAlphanumeric()) { "Name must be alphanumeric" }

		failIf(!NPCTerritoryOwner.none(NPCTerritoryOwner.nameQuery(name))) { "An npc owner named $name already exists" }
	}

	private fun validateStationName(name: String) {
		failIf(!name.isAlphanumeric()) { "Name must be alphanumeric" }

		failIf(!NPCSpaceStation.none(NPCTerritoryOwner.nameQuery(name))) { "An npc owner named $name already exists" }
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
	fun onDeleteStation(sender: Player, name: String) = asyncCommand(sender) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }
		NPCSpaceStation.delete(station._id)
		sender.success("Deleted $name")
	}

	@Subcommand("station set color")
	fun onSetStationColor(sender: Player, name: String, red: Int, green: Int, blue: Int) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		val color = validateColor(red, green, blue)

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::color, color))
		sender.success("Set the color of $name!")
	}

	@Subcommand("station set radius")
	fun onSetStationRadius(sender: Player, name: String, value: Int) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::radius, value))
		sender.success("Set the radius of $name!")
	}

	@Subcommand("station set description")
	fun onSetStationDescription(sender: Player, name: String, description: String,) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::dynmapDescription, description))
		sender.success("Set the description of $name!")
	}

	@Subcommand("station set protected")
	fun onSetStationProtection(sender: Player, name: String, protected: Boolean) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, setValue(NPCSpaceStation::isProtected, protected))
		sender.success("Set the protection of $name!")
	}

	@Subcommand("station set location")
	fun onSetStationLocation(sender: Player, name: String, world: World, x: Int, z: Int) {
		val station = NPCSpaceStation.findOne(NPCSpaceStation::name eq name) ?: fail { "Station $name not found!" }

		NPCSpaceStation.updateById(station._id, and(
			setValue(NPCSpaceStation::world, world.name),
			setValue(NPCSpaceStation::x, x),
			setValue(NPCSpaceStation::z, z),
		))
		sender.success("Set the location of $name!")
	}
}
