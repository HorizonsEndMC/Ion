package net.horizonsend.ion.server.features.client.display

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlanetSpaceRendering : IonServerComponent() {
    private const val PLANET_UPDATE_RATE = 20L

    override fun onEnable() {
        Tasks.syncRepeat(0L, PLANET_UPDATE_RATE) {
            Bukkit.getOnlinePlayers().forEach { player ->
                renderPlanets(player)
            }
        }
    }

    /**
     * Renders client-side ItemEntity planets for each player.
     * @param player the player to send objects to
     */
    private fun renderPlanets(player: Player) {
        // Only render planets if the player is in a space world
        if (!SpaceWorlds.contains(player.world)) return

        val planetList = Space.getPlanets().filter { it.spaceWorld == player.world }
        val playerDisplayEntities = ClientDisplayEntities[player.uniqueId] ?: return

        for (planet in planetList) {
            val distance = player.location.toVector().distance(planet.location.toVector())
            val direction = planet.location.toVector().subtract(player.location.toVector()).normalize()

            // entity does not exist yet; create it
            if (playerDisplayEntities[planet.name] == null) {
                // send packet and create the planet entity
                val entity = ClientDisplayEntities.createPlanetEntity(player, planet.name, distance, direction) ?: continue
                ClientDisplayEntities.sendEntityPacket(player, entity)
            }
            // entity exists; update position
            else {
                ClientDisplayEntities.updatePlanetEntity(player, planet.name, distance, direction)
            }
        }
    }
}