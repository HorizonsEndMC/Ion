package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.formatSpacePrefix
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.modules.SinkMessageFactory.Companion.SPACE_ARENA
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE

class AISinkMessageFactory(private val sunkShip: ActiveStarship) : MessageFactory {
	override fun execute() {
		val arena = IonServer.configuration.serverName.equals("creative", ignoreCase = true) // TODO manager later
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
	}

	private fun sendGameMessage(arena: Boolean, sinkMessage: Component, assists: Component?) {
		val message = ofChildren(sinkMessage, assists.orEmpty())

		if (arena) IonServer.server.sendMessage(message) else Notify.chatAndGlobal(message)
	}

	private fun getSinkMessage(arena: Boolean, killerDamager: Damager): Component {
		val killedShipText = ofChildren(sunkShip.getDisplayName(), text(" "), bracketed(text(sunkShip.initialBlockCount)))

		val killerName = formatName(killerDamager)
		val sunkMessage = ofChildren(text(" was sunk by ", RED), killerName)

		val arenaText = if (arena) SPACE_ARENA else Component.empty()

		return ofChildren(arenaText, killedShipText, sunkMessage)
	}

	private fun getAssists(sortedByTime: Iterator<Pair<Damager, ShipKillXP.ShipDamageData>>) : Component? {
		if (!sortedByTime.hasNext()) return null

		val assistsMessage = formatSpacePrefix(bracketed(text("Assists")))

		val hover = text()

		// Take 5 damagers
		while (sortedByTime.hasNext()) {
			val (assistDamager, _) = sortedByTime.next()
			hover.append(formatName(assistDamager))

			if (sortedByTime.hasNext()) hover.append(newline())
		}

		assistsMessage.hoverEvent(hover.build())

		return assistsMessage
	}

	private fun formatName(damager: Damager): Component {
		val starship = damager.starship

		if (starship !is ActiveControlledStarship) return damager.getDisplayName()

		return formatPlayerShipName(starship)
	}

	private fun formatPlayerShipName(starship: ActiveStarship): Component {
		val hover = ofChildren(text("${starship.initialBlockCount} block ", WHITE), starship.type.displayNameComponent)

		val nameFormat = if ((starship as? ActiveControlledStarship)?.data?.name == null) ofChildren(
			text("A ", RED),
			text(starship.initialBlockCount),
			text(" block ", RED),
			starship.type.displayNameComponent.color(WHITE)
		)
		else ofChildren(
			starship.getDisplayName(),
			text(", a ", RED),
			text(starship.initialBlockCount),
			text(" block ", RED),
			starship.type.displayNameComponent.color(WHITE)
		)

		return ofChildren(nameFormat, text(", piloted by ", RED), starship.controller.getPilotName()).hoverEvent(hover)
	}
}
