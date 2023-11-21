package net.horizonsend.ion.server.features.starship.messages

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import java.time.Instant

class SinkMessageFactory(private val sunkShip: ActiveControlledStarship) : MessageFactory {
	override fun createAndSend() {
		val arena = sunkShip.world.name.contains("arena", ignoreCase = true) // TODO manager later
		val data = sunkShip.damagers

		// First person got the final blow
		val sortedByTime = data.entries.sortedByDescending { it.value.lastDamaged }.iterator()

		if (!sortedByTime.hasNext()) throw NullPointerException("Starship sunk with no damagers")

		val (killerDamager, _) = sortedByTime.next()

		val sinkMessage = getSinkMessage(arena, killerDamager)
		val assists = getAssists(sortedByTime)

		sendGameMessage(arena, sinkMessage, assists)
		sendDiscordMessage(arena, sinkMessage, assists)
	}

	private fun sendGameMessage(arena: Boolean, sinkMessage: Component, assists: Map<Damager, Component>) {
		val assistPrefix = if (assists.isNotEmpty()) ofChildren(text(", assisted by:", RED), newline()) else empty()

		val message = ofChildren(sinkMessage, assistPrefix, assists.values.join(separator = newline()))

		if (arena) Bukkit.getServer().sendMessage(message) else Notify.online(message)
	}

	private fun sendDiscordMessage(arena: Boolean, sinkMessage: Component, assists: Map<Damager, Component>) {
		if (arena) return
		if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return

		Tasks.async {
			val channel: TextChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("events") ?: return@async

			// Formatting the messages
			val sunkPlayer = (sunkShip.controller as? PlayerController)?.player
			val headURL = sunkPlayer?.name?.let { "https://minotar.net/avatar/$sunkPlayer" }

			val killedNationColor = sunkShip.controller.damager.color.asRGB()

			val embed = EmbedBuilder()
				.setTitle("Ship Kill") // Title at top
				.setTimestamp(Instant.now()) // Timestamp at the bottom
				.setColor(killedNationColor) // Color bar on the side is the killed player's nation's color
				.setThumbnail(headURL) // Head of the killed player
				.addField(MessageEmbed.Field(sinkMessage.plainText(), "", false))

			if (assists.isNotEmpty()) {
				embed.addField(MessageEmbed.Field("Assisted By:", assists.map { formatName(it.key).plainText() }.joinToString("\n"), false))
			}

			channel.sendMessageEmbeds(embed.build()).queue()
		}
	}

	private fun getSinkMessage(arena: Boolean, killerDamager: Damager): Component {
		val killedShipText = formatName(sunkShip)

		val killerName = formatName(killerDamager)
		val sunkMessage = ofChildren(text(" was sunk by ", RED), killerName)

		val arenaText = if (arena) ofChildren(
			text("[", TextColor.color(85, 85, 85)),
			text("Space Arena", TextColor.color(255, 255, 102)),
			text("] ", TextColor.color(85, 85, 85))
		) else empty()

		return ofChildren(arenaText, killedShipText, sunkMessage)
	}

	private fun getAssists(sortedByTime: Iterator<Map.Entry<Damager, ShipKillXP.ShipDamageData>>) : Map<Damager, Component> {
		val components = mutableMapOf<Damager, Component>()

		// Take 5 damagers
		while (sortedByTime.hasNext()) {
			val (assistDamager, _) = sortedByTime.next()

			val assistName = formatName(assistDamager)

			val assist = text()
				.append(assistName)

			if (sortedByTime.hasNext()) assist.append(text(",", RED))

			components[assistDamager] = assist.build()
		}

		return components
	}

	private fun formatName(damager: Damager): Component {
		val starship = damager.starship

		if (starship !is ActiveControlledStarship) return damager.getDisplayName()

		return formatName(starship)
	}

	private fun formatName(starship: ActiveControlledStarship): Component {
		val hover = ofChildren(text("${starship.initialBlockCount} block ", NamedTextColor.WHITE), starship.type.displayNameComponent)

		val nameFormat = if (starship.data.name == null) ofChildren(
			text("A ", RED),
			text(starship.initialBlockCount),
			text(" block ", RED),
			starship.type.displayNameComponent.color(NamedTextColor.WHITE)
		)
		else ofChildren(
			starship.getDisplayName(),
			text(", a ", RED),
			text(starship.initialBlockCount),
			text(" block ", RED),
			starship.type.displayNameComponent.color(NamedTextColor.WHITE)
		)

		return ofChildren(nameFormat, text(", piloted by ", RED), starship.controller.pilotName).hoverEvent(hover)
	}
}
