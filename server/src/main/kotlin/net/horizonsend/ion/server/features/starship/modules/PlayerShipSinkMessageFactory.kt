package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.discord.Embed.Field
import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.ARENA_PREFIX
import net.horizonsend.ion.server.miscellaneous.utils.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Discord.asDiscord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED

class PlayerShipSinkMessageFactory(private val sunkShip: ActiveStarship) : MessageFactory {
	override fun execute() {
		val data = sunkShip.damagers

		// First person got the final blow
		val sortedByTime: Iterator<MutableMap.MutableEntry<Damager, ShipKillXP.ShipDamageData>> = data
			.entries
			.filter { (damager, data) ->
				if (damager is AIShipDamager && damager.starship.controller is NoOpController) return@filter false
				if (data.lastDamaged < ShipKillXP.damagerExpiration) return@filter false

				true
			}
			.sortedByDescending { it.value.lastDamaged }
			.iterator()

		val (killerDamager, _) = sortedByTime.next()
		if (sunkShip.playerPilot?.isDead == true) sendPilotKilledMessage(killerDamager, sortedByTime) else sendSinkMessage(killerDamager, sortedByTime)
	}

	private fun sendSinkMessage(killerDamager: Damager, sortedByTime: Iterator<MutableMap.MutableEntry<Damager, ShipKillXP.ShipDamageData>>) {
		val arena = sunkShip.world.ion.hasFlag(WorldFlag.ARENA)
		val assists = getAssists(sortedByTime)

		val message = template(
			text("{0} was sunk by {1} using {2}{3}", RED),
			formatName(sunkShip),
			formatName(killerDamager),
			sunkShip.lastWeaponName ?: text("Unknown Weapon"),
			formatAssists(assists)
		)

		if (arena) return IonServer.server.sendMessage(ofChildren(ARENA_PREFIX, message))
		// The rest is necessary

		Notify.chatAndGlobal(message)

		val headURL = (sunkShip.controller as? PlayerController)?.player?.name?.let { "https://minotar.net/avatar/$it" }
		val killedNationColor = sunkShip.controller.damager.color.asRGB()

		val fields = mutableListOf(Field(name = asDiscord(message), value = "", inline = false))
		if (assists.isNotEmpty()) fields.add(Field("Assisted By:", assists.entries.joinToString("\n") { asDiscord(formatName(it.key)) }))

		val embed = Embed(
			title = "Starship Kill",
			timestamp = System.currentTimeMillis(),
			color = killedNationColor,
			thumbnail = headURL,
			fields = fields
		)

		Discord.sendEmbed(IonServer.discordSettings.eventsChannel, embed)
		Discord.sendEmbed(IonServer.discordSettings.globalChannel, embed)
	}

	private fun sendPilotKilledMessage(killer: Damager, sortedByTime: Iterator<MutableMap.MutableEntry<Damager, ShipKillXP.ShipDamageData>>) {
		val arena = sunkShip.world.ion.hasFlag(WorldFlag.ARENA)
		val assists = getAssists(sortedByTime)

		val message = template(
			text("{0}, the pilot of {1} was killed by {2}{3}", RED),
			sunkShip.controller.damager.getDisplayName(),
			formatName(sunkShip, includePilotInfo = false),
			killer.getDisplayName(),
			formatAssists(assists)
		)

		if (arena) return IonServer.server.sendMessage(ofChildren(ARENA_PREFIX, message))
		// The rest is necessary

		Notify.chatAndGlobal(message)

		val fields = mutableListOf(Field(name = asDiscord(message), value = "", inline = false))
		if (assists.isNotEmpty()) fields.add(Field("Assisted By:", assists.entries.joinToString("\n") { asDiscord(formatName(it.key)) }))

		val embed = Embed(
			title = "Starship Kill",
			timestamp = System.currentTimeMillis(),
			color = sunkShip.controller.damager.color.asRGB(),
			fields = fields
		)

		Discord.sendEmbed(IonServer.discordSettings.eventsChannel, embed)
		Discord.sendEmbed(IonServer.discordSettings.globalChannel, embed)
	}

	private fun getAssists(sortedByTime: Iterator<MutableMap.MutableEntry<Damager, ShipKillXP.ShipDamageData>>): Map<Damager, Component> {
		val components = mutableMapOf<Damager, Component>()

		while (sortedByTime.hasNext()) {
			val (assistDamager, _) = sortedByTime.next()

			val assist = text()
				.append(formatName(assistDamager))
				.append(if (sortedByTime.hasNext()) text(",", RED) else empty())

			components[assistDamager] = assist.build()
		}

		return components
	}

	private fun formatAssists(assists: Map<Damager, Component>): Component {
		if (assists.isEmpty()) return empty()
		return ofChildren(text(", assisted by:", RED), newline(), assists.values.join(separator = newline()))
	}

	private fun formatName(damager: Damager): Component {
		val starship = damager.starship
		if (starship !is ActiveControlledStarship) return damager.getDisplayName()

		return formatName(starship)
	}

	private fun formatName(starship: Starship, includePilotInfo: Boolean = true): Component {
		val hover = ofChildren(text("${starship.initialBlockCount} block ", NamedTextColor.WHITE), starship.type.displayNameComponent)

		val shipNameFormat = if ((starship as? ActiveControlledStarship)?.data?.name == null) ofChildren(
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

		if (!includePilotInfo) return shipNameFormat

		val newName = when (val controller = starship.controller) {
			is PlayerController -> text(controller.player.name)
			else -> controller.getPilotName()
		}

		return ofChildren(shipNameFormat, text(", piloted by ", RED), newName).hoverEvent(hover)
	}
}
