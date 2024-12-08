package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.or

@CommandAlias("starships")
object StarshipsCommand : SLCommand() {
	@Description("List all of your starships")
	@Suppress("Unused")
	@Default
	fun starships(sender: Player, @Optional currentPage: Int?) {
		val ships = PlayerStarshipData.find(and(
			PlayerStarshipData::captain eq sender.slPlayerId,
			or(
				PlayerStarshipData::serverName eq null,
				PlayerStarshipData::serverName eq ConfigurationFiles.serverConfiguration().serverName
			))
		).toList()

		sender.sendMessage(formatOutput(sender.slPlayerId, ships, currentPage ?: 1))
	}

	@Subcommand("inworld")
	@Description("List all of your starships in a world")
	fun starshipsWorld(sender: Player, world: World, @Optional currentPage: Int?) {
		val ships = PlayerStarshipData.find(and(
			PlayerStarshipData::captain eq sender.slPlayerId,
			PlayerStarshipData::levelName eq world.name,
			or(
				PlayerStarshipData::serverName eq null,
				PlayerStarshipData::serverName eq ConfigurationFiles.serverConfiguration().serverName
			))
		).toList()

		sender.sendMessage(formatOutput(sender.slPlayerId, ships, currentPage ?: 1, " ${world.name}"))
	}

	@CommandPermission("ion.starships.other")
	@Subcommand("other")
	fun onStarshipsOther(sender: CommandSender, otherName: String, @Optional currentPage: Int?) {
		val user = resolveOfflinePlayer(otherName).slPlayerId

		val ships = PlayerStarshipData.find(and(
			PlayerStarshipData::captain eq user,
			or(
				PlayerStarshipData::serverName eq null,
				PlayerStarshipData::serverName eq ConfigurationFiles.serverConfiguration().serverName
			))
		).toList()

		sender.sendMessage(formatOutput(null, ships, currentPage ?: 1, " other $otherName"))
	}

	private fun formatOutput(sender: SLPlayerId?, starships: List<PlayerStarshipData>, currentPage: Int, subcommand: String = ""): Component {
		val header = lineBreakWithCenterText(text("Inactive Starships", HEColorScheme.HE_LIGHT_ORANGE))
		val body = formatPaginatedMenu(
			starships.count(),
			"/starships$subcommand",
			currentPage
		) {
			val ship = starships[it]
			@Suppress("DEPRECATION") val x = blockKeyX(ship.blockKey)
			@Suppress("DEPRECATION") val y = blockKeyY(ship.blockKey)
			@Suppress("DEPRECATION") val z = blockKeyZ(ship.blockKey)

			template(
				text("{0} at {1} {2} {3} in {4}{5}{6}", HE_MEDIUM_GRAY),
				PilotedStarships.getDisplayName(ship).hoverEvent(ship.starshipType.actualType.displayNameComponent),
				x, y, z,
				ship.levelName,
				if (ship.captain != sender) template(text(" owned by {0}", AQUA), SLPlayer[ship.captain]?.lastKnownName) else empty(),
				if (ship.serverName == null) text(" (Unspecified Server)", RED) else empty()
			)
		}

		return ofChildren(header, newline(), body)
	}
}
