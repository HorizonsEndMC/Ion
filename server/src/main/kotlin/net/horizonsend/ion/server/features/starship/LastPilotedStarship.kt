package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Location
import java.util.UUID

object LastPilotedStarship : IonServerComponent() {
    val map = mutableMapOf<UUID, Location>()

    override fun onEnable() {
        listen<StarshipUnpilotedEvent> { event ->
			val player = (event.starship.controller as? PlayerController)?.player ?: return@listen
            map[player.uniqueId] = event.starship.centerOfMass.toLocation(player.world)
        }

        listen<StarshipPilotedEvent> { event ->
            map.remove(event.player.uniqueId)
        }
    }
}
