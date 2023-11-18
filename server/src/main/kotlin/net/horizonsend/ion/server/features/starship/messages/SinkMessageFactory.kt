package net.horizonsend.ion.server.features.starship.messages

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.component
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
import net.kyori.adventure.text.format.NamedTextColor.GOLD
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
		val message = component(sinkMessage, newline(), *assists.values.toTypedArray())

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

		val killedShipHover = component(text("${sunkShip.initialBlockCount} block ", NamedTextColor.WHITE), sunkShip.type.displayNameComponent)
		val killedShipText = formatName(sunkShip).hoverEvent(killedShipHover)

		val killerName = formatName(killerDamager)
		val sunkMessage = component(text(" was sunk by ", GOLD), killerName)

		val arenaText = if (arena) component(
			text("[", TextColor.color(85, 85, 85)),
			text("Space Arena", TextColor.color(255, 255, 102)),
			text("] ", TextColor.color(85, 85, 85))
		) else empty()

		return component(arenaText, killedShipText, sunkMessage)
	}

	private fun getAssists(sortedByTime: Iterator<Map.Entry<Damager, ShipKillXP.ShipDamageData>>) : Map<Damager, Component> {
		val components = mutableMapOf<Damager, Component>()

		// Take 5 damagers
		while (sortedByTime.hasNext()) {
			val (assistDamager, _) = sortedByTime.next()

			val assistName = formatName(assistDamager)

			val assist = text()
				.append(assistName)

			if (sortedByTime.hasNext()) assist.append(text(",", GOLD))

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
		val nameFormat = if (starship.data.name == null) component(
			text("A ", GOLD),
			text(starship.initialBlockCount),
			text(" block ", GOLD),
			starship.type.displayNameComponent.color(NamedTextColor.WHITE)
		)
		else component(
			starship.getDisplayNameComponent(),
			text(", a ", GOLD),
			text(starship.initialBlockCount),
			text(" block ", GOLD),
			starship.type.displayNameComponent.color(NamedTextColor.WHITE)
		)

		return component(nameFormat, text(", piloted by ", GOLD), starship.controller.pilotName)
	}
}
