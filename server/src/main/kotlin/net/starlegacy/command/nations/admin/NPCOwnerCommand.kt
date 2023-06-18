package net.starlegacy.command.nations.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.starlegacy.command.SLCommand
import net.horizonsend.ion.server.database.schema.nations.NPCTerritoryOwner
import net.starlegacy.util.isAlphanumeric
import net.starlegacy.util.msg
import org.bukkit.Color
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

@CommandPermission("nations.npcowner")
@CommandAlias("npcowner")
internal object NPCOwnerCommand : SLCommand() {
	private fun validateName(name: String) {
		failIf(!name.isAlphanumeric()) { "Name must be alphanumeric" }

		failIf(!NPCTerritoryOwner.none(NPCTerritoryOwner.nameQuery(name))) { "An npc owner named $name already exists" }
	}

	private fun validateColor(red: Int, green: Int, blue: Int): Int {
		val range = 0..255
		failIf(red !in range || green !in range || blue !in range) { "Invalid color values" }

		return Color.fromRGB(red, green, blue).asRGB()
	}

	@Subcommand("create tradecity")
	@Suppress("unused")
	fun onCreateTC(sender: Player, name: String, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		validateName(name)
		val color = validateColor(red, green, blue)
		NPCTerritoryOwner.create(territory.id, name, color, true)
		sender msg "&aCreated npc territory"
	}

	@Subcommand("create territory")
	@Suppress("unused")
	fun onCreateTerritory(sender: Player, name: String, red: Int, green: Int, blue: Int) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		validateName(name)
		val color = validateColor(red, green, blue)
		NPCTerritoryOwner.create(territory.id, name, color, false)
		sender msg "&aCreated npc territory"
	}

	@Subcommand("delete")
	@Suppress("unused")
	fun onDelete(sender: Player) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val npcOwner = territory.npcOwner ?: fail { "${territory.name} is not owned by an npc owner" }
		val name = NPCTerritoryOwner.getName(npcOwner)
		NPCTerritoryOwner.delete(npcOwner)
		sender msg "&aDeleted $name"
	}

	@Subcommand("set color")
	@Suppress("unused")
	fun onSetColor(sender: Player, name: String, red: Int, green: Int, blue: Int) {
		val city = NPCTerritoryOwner.findOne(NPCTerritoryOwner::name eq name) ?: fail { "City $name not found!" }

		val color = validateColor(red, green, blue)

		NPCTerritoryOwner.updateById(city._id, setValue(NPCTerritoryOwner::color, color))
		sender.success("Set the color of $name!")
	}
}
