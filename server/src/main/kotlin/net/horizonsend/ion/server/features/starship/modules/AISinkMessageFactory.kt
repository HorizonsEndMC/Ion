package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.formatSpacePrefix
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text

class AISinkMessageFactory(private val sunkShip: ActiveStarship) : MessageFactory {
	override fun execute() {
		val data = sunkShip.damagers

		// First person got the final blow
		val sortedByTime = data.entries.filter { (damager, data) ->
				if (damager is AIShipDamager && damager.starship.controller is NoOpController) {
					IonServer.slF4JLogger.warn("Removed AI damager $damager")
					return@filter false
				}

				if (data.lastDamaged < ShipKillXP.damagerExpiration) {
					IonServer.slF4JLogger.warn("Removed expired damager $damager")
					return@filter false
				}

				true
			}
			.sortedByDescending { it.value.lastDamaged }

		if (sortedByTime.isEmpty()) return IonServer.slF4JLogger.warn("Starship sunk with no damagers")

		val (killerDamager, _) = sortedByTime.first()
		val killerShip = killerDamager.starship

		val sinkMessage = if (killerShip != null) template(
			text("{0} was sunk by {1} piloting {2}"),
			useQuotesAroundObjects = false,
			sunkShip.getDisplayName(),
			killerDamager.getDisplayName(),
			killerShip.getDisplayName()
		) else template(
			text("{0} was sunk by {1}"),
			useQuotesAroundObjects = false,
			sunkShip.getDisplayName(),
			killerDamager.getDisplayName(),
		)

		val assists = getAssists(sortedByTime.map { it.key })
		Notify.chatAndGlobal(ofChildren(sinkMessage, assists.orEmpty()))
	}

	private fun getAssists(damagers: Iterable<Damager>) : Component? {
		val sortedByTime = damagers.iterator()
		if (!sortedByTime.hasNext()) return null

		val assistsMessage = formatSpacePrefix(bracketed(text("Assists", HE_LIGHT_GRAY)))
		val hoverBuilder = text()

		// Take 5 damagers
		while (sortedByTime.hasNext()) {
			hoverBuilder.append(sortedByTime.next().getDisplayName())
			if (sortedByTime.hasNext()) hoverBuilder.append(newline())
		}

		assistsMessage.hoverEvent(hoverBuilder.build())

		return assistsMessage
	}
}
