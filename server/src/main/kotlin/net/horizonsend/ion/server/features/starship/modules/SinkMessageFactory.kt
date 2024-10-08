package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Discord.asDiscord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor

class SinkMessageFactory(private val sunkShip: ActiveStarship) : MessageFactory {
	override fun execute() {
		val arena = sunkShip.world.hasFlag(WorldFlag.ARENA)
		val data = sunkShip.damagers

		// First person got the final blow
		val sortedByTime = data.toList()
			.filter { (damager, data) ->
				if (damager is AIShipDamager && damager.starship.controller is NoOpController) return@filter false
				if (data.lastDamaged < ShipKillXP.damagerExpiration) return@filter false

				true
			}
			.sortedByDescending { it.second.lastDamaged }
			.iterator()

		if (!sortedByTime.hasNext()) {
			IonServer.slF4JLogger.warn("Starship sunk with no damagers")
			return
		}

		val (killerDamager, _) = sortedByTime.next()

		val sinkMessage = getSinkMessage(arena, killerDamager)
		val assists = getAssists(sortedByTime)

		sendGameMessage(arena, sinkMessage, assists)
		sendDiscordMessage(arena, sinkMessage, assists)
	}

	private fun sendGameMessage(arena: Boolean, sinkMessage: Component, assists: Map<Damager, Component>) {
		val assistPrefix = if (assists.isNotEmpty()) ofChildren(text(", assisted by:", RED), newline()) else empty()

		val message = ofChildren(sinkMessage, assistPrefix, assists.values.join(separator = newline()))

		if (arena) IonServer.server.sendMessage(message) else Notify.chatAndGlobal(message)
	}

	private fun sendDiscordMessage(arena: Boolean, sinkMessage: Component, assists: Map<Damager, Component>) {
		if (arena) return

		Tasks.async {
			// Formatting the messages
			val sunkPlayer = (sunkShip.controller as? PlayerController)?.player
			val headURL = sunkPlayer?.name?.let { "https://minotar.net/avatar/$sunkPlayer" }

			val killedNationColor = sunkShip.controller.damager.color.asRGB()

			val fields = mutableListOf(Embed.Field(name = asDiscord(sinkMessage), value = "", inline = false))

			if (assists.isNotEmpty()) {
				fields.add(Embed.Field("Assisted By:", assists.map { asDiscord(formatName(it.key)) }.joinToString("\n"), false))
			}

			val embed = Embed(
				title = "Ship Kill",
				timestamp = System.currentTimeMillis(),
				color = killedNationColor,
				thumbnail = headURL,
				fields = fields
			)

			Discord.sendEmbed(IonServer.discordSettings.eventsChannel, embed)
			Discord.sendEmbed(IonServer.discordSettings.globalChannel, embed)
		}
	}

	private fun getSinkMessage(arena: Boolean, killerDamager: Damager): Component {
		val killedShipText = formatName(sunkShip)

		val killerName = formatName(killerDamager)
		val sunkMessage = ofChildren(text(" was sunk by ", RED), killerName)

		val arenaText = if (arena) SPACE_ARENA else empty()

		return ofChildren(arenaText, killedShipText, sunkMessage)
	}

	private fun getAssists(sortedByTime: Iterator<Pair<Damager, ShipKillXP.ShipDamageData>>) : Map<Damager, Component> {
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

	private fun formatName(starship: ActiveStarship): Component {
		val hover = ofChildren(text("${starship.initialBlockCount} block ", NamedTextColor.WHITE), starship.type.displayNameComponent)

		val nameFormat = if ((starship as? ActiveControlledStarship)?.data?.name == null) ofChildren(
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

		val newName = when (val controller = starship.controller) {
			is PlayerController -> text(controller.player.name)
			else -> controller.getPilotName()
		}

		return ofChildren(nameFormat, text(", piloted by ", RED), newName).hoverEvent(hover)
	}

	companion object {
		val SPACE_ARENA = ofChildren(
			text("[", TextColor.color(85, 85, 85)),
			text("Space Arena", TextColor.color(255, 255, 102)),
			text("] ", TextColor.color(85, 85, 85))
		)
	}
}
