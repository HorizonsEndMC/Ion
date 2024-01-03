package net.horizonsend.ion.server.features.starship.modules

import net.horizonsend.ion.common.utils.text.MessageFactory
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit

class AISinkMessageFactory(private val sunkShip: ActiveStarship) : MessageFactory {
	override fun execute() {
		val arena = sunkShip.world.name.contains("arena", ignoreCase = true) // TODO manager later
		val data = sunkShip.damagers

		// First person got the final blow
		val sortedByTime = data.entries.sortedByDescending { it.value.lastDamaged }.iterator()

		if (!sortedByTime.hasNext()) throw NullPointerException("Starship sunk with no damagers")

		val (killerDamager, _) = sortedByTime.next()

		val sinkMessage = getSinkMessage(arena, killerDamager)
		val assists = getAssists(sortedByTime)

		sendGameMessage(arena, sinkMessage, assists)
	}

	private fun sendGameMessage(arena: Boolean, sinkMessage: Component, assists: Map<Damager, Component>) {
		val assistPrefix = if (assists.isNotEmpty()) ofChildren(Component.text(", assisted by:", NamedTextColor.RED), Component.newline()) else Component.empty()

		val message = ofChildren(sinkMessage, assistPrefix, assists.values.join(separator = Component.newline()))

		if (arena) Bukkit.getServer().sendMessage(message) else Notify.online(message)
	}

	private fun getSinkMessage(arena: Boolean, killerDamager: Damager): Component {
		val killedShipText = formatName(sunkShip)

		val killerName = formatName(killerDamager)
		val sunkMessage = ofChildren(Component.text(" was sunk by ", NamedTextColor.RED), killerName)

		val arenaText = if (arena) ofChildren(
			Component.text("[", TextColor.color(85, 85, 85)),
			Component.text("Space Arena", TextColor.color(255, 255, 102)),
			Component.text("] ", TextColor.color(85, 85, 85))
		) else Component.empty()

		return ofChildren(arenaText, killedShipText, sunkMessage)
	}

	private fun getAssists(sortedByTime: Iterator<Map.Entry<Damager, ShipKillXP.ShipDamageData>>) : Map<Damager, Component> {
		val components = mutableMapOf<Damager, Component>()

		// Take 5 damagers
		while (sortedByTime.hasNext()) {
			val (assistDamager, _) = sortedByTime.next()

			val assistName = formatName(assistDamager)

			val assist = Component.text()
				.append(assistName)

			if (sortedByTime.hasNext()) assist.append(Component.text(",", NamedTextColor.RED))

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
		val hover = ofChildren(Component.text("${starship.initialBlockCount} block ", NamedTextColor.WHITE), starship.type.displayNameComponent)

		val nameFormat = if ((starship as? ActiveControlledStarship)?.data?.name == null) ofChildren(
			Component.text("A ", NamedTextColor.RED),
			Component.text(starship.initialBlockCount),
			Component.text(" block ", NamedTextColor.RED),
			starship.type.displayNameComponent.color(NamedTextColor.WHITE)
		)
		else ofChildren(
			starship.getDisplayName(),
			Component.text(", a ", NamedTextColor.RED),
			Component.text(starship.initialBlockCount),
			Component.text(" block ", NamedTextColor.RED),
			starship.type.displayNameComponent.color(NamedTextColor.WHITE)
		)

		return ofChildren(nameFormat, Component.text(", piloted by ", NamedTextColor.RED), starship.controller.getPilotName()).hoverEvent(hover)
	}
}
