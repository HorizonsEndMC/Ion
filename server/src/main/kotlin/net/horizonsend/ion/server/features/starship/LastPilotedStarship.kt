package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.toLocation
import org.bukkit.Location
import java.util.UUID

object LastPilotedStarship : IonServerComponent() {
    val map = mutableMapOf<UUID, Location>()

    override fun onEnable() {
        listen<StarshipUnpilotedEvent> { event ->
            map[event.player.uniqueId] = event.starship.centerOfMass.toLocation(event.player.world)
        }

        listen<StarshipPilotedEvent> { event ->
            map.remove(event.player.uniqueId)
        }
    }
}