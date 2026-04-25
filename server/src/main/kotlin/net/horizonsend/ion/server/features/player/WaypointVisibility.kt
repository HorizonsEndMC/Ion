package net.horizonsend.ion.server.features.player

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent

object WaypointVisibility : IonServerComponent() {
	override fun onEnable() {
		Bukkit.getOnlinePlayers().forEach(::applyWaypointVisibility)

		listen<PlayerJoinEvent> { event ->
			applyWaypointVisibility(event.player)
		}
	}

	private fun applyWaypointVisibility(player: Player) {
		val config = ConfigurationFiles.serverConfiguration()

		player.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE)?.baseValue = config.waypointTransmitRange
		player.getAttribute(Attribute.WAYPOINT_RECEIVE_RANGE)?.baseValue = config.waypointReceiveRange
	}
}
