package net.horizonsend.ion.server.features.client.display

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.min

object PlanetSpaceRendering : IonServerComponent() {
    private const val PLANET_UPDATE_RATE = 10L

    override fun onEnable() {
        Tasks.syncRepeat(0L, PLANET_UPDATE_RATE) {
            Bukkit.getOnlinePlayers().forEach { player ->
                renderPlanets(player)
            }
        }
    }

    /**
     * Creates a client-side ItemDisplay entity for rendering planet icons in space.
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param identifier the string used to retrieve the entity later
     * @param distance the distance that the planet is to the player
     * @param direction the direction that the entity will render from with respect to the player
     */
    private fun createPlanetEntity(
        player: Player,
        identifier: String,
        distance: Double,
        direction: Vector
    ): net.minecraft.world.entity.Display.ItemDisplay? {

        val entity = ClientDisplayEntityFactory.createItemDisplay(player)
        val entityRenderDistance = getViewDistanceEdge(player)
        // do not render if the planet is closer than the entity render distance
        if (distance < entityRenderDistance) return null

        entity.itemStack = getPlanetItemStack(identifier)
        entity.billboard = Display.Billboard.FIXED
        entity.viewRange = 5.0f
        entity.interpolationDuration = 10
        entity.teleportDuration = 10
        println("identifier: $identifier")
        println("entity.itemStack: ${entity.itemStack}")

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        val offset = direction.clone().normalize().multiply(entityRenderDistance)

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector(offset.toVector3f()),
            Vector3f(scale(distance) * viewDistanceFactor(entityRenderDistance)),
            AxisAngle4f()
        )

        // position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
        val nmsEntity = entity.getNMSData(position.x, position.y, position.z)

        ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.set(identifier, nmsEntity)

        return nmsEntity
    }

    /**
     * Updates a client-side ItemDisplay for rendering planet icons in space
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param identifier the string used to retrieve the entity later
     * @param distance the distance that the planet is to the player
     * @param direction the direction that the entity will render from with respect to the player
     */
    private fun updatePlanetEntity(player: Player, identifier: String, distance: Double, direction: Vector) {

        val entityRenderDistance = getViewDistanceEdge(player)

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name ||
            distance < entityRenderDistance
        ) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove(identifier)
            return
        }
        else {
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            val offset = direction.clone().normalize().multiply(entityRenderDistance)

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                Quaternionf(ClientDisplayEntities.rotateToFaceVector(offset.toVector3f())),
                Vector3f(scale(distance) * viewDistanceFactor(entityRenderDistance)),
                Quaternionf()
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
        }
    }

    /**
     * Equation for getting the scale of a planet display entity. Maximum (0, 100) and horizontal asymptote at x = 5.
     * @return a scale size for planet display entities
     * @param distance the distance at which the player is from the planet
     */
    private fun scale(distance: Double) = ((500000000 / ((0.03125 * distance * distance) + 5250000)) + 5).toFloat()

    /**
     * Equation for getting the factor of the planet scaling to maintain apparent visual scale depending on
     * the player's view distance. Calculated assuming a default view distance of 10 (160 blocks); 0.5h / 160 = h` / x,
     * where h is the apparent visual height of the display entity, h` is the apparent visual height of the display
     * entity after transformation, and x is the view distance of the player in blocks.
     * @return a scaling factor useful for maintaining the apparent size of objects as they are rendered closer
     * or further away
     * @param viewDistance the distance at which the object is being rendered
     */
    private fun viewDistanceFactor(viewDistance: Int) = (0.003125 * viewDistance).toFloat()

    /**
     * Function for getting the distance from the edge of the player's view distance, minus several blocks.
     * @return the view distance of a player in blocks, minus some offset
     * @param player the player to get the view distance from
     */
    private fun getViewDistanceEdge(player: Player) = (min(player.clientViewDistance, Bukkit.getWorlds()[0].viewDistance) * 16) - 16

    private fun getPlanetItemStack(name: String): ItemStack = when (name) {
        "Aerach" -> CustomItems.AERACH
        "Aret" -> CustomItems.ARET
        "Chandra" -> CustomItems.CHANDRA
        "Damkoth" -> CustomItems.DAMKOTH
        "Disterra" -> CustomItems.DISTERRA
        "Eden" -> CustomItems.EDEN
        "Gahara" -> CustomItems.GAHARA
        "Herdoli" -> CustomItems.HERDOLI
        "Ilius" -> CustomItems.ILIUS
        "Isik" -> CustomItems.ISIK
        "Kovfefe" -> CustomItems.KOVFEFE
        "Krio" -> CustomItems.KRIO
        "Lioda" -> CustomItems.LIODA
        "Luxiterna" -> CustomItems.LUXITERNA
        "Qatra" -> CustomItems.QATRA
        "Rubaciea" -> CustomItems.RUBACIEA
        "Turms" -> CustomItems.TURMS
        "Vask" -> CustomItems.VASK

        "Asteri" -> CustomItems.ASTERI
        "EdenHack" -> CustomItems.HORIZON
        "Ilios" -> CustomItems.ILIOS
        "Regulus" -> CustomItems.REGULUS
        "Sirius" -> CustomItems.SIRIUS

        else -> CustomItems.AERACH
    }.constructItemStack()

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
                createPlanetEntity(player, planet.name, distance, direction) ?: continue
            }
            // entity exists; update position
            else {
                updatePlanetEntity(player, planet.name, distance, direction)
            }
        }

        val starList = Space.getStars().filter { it.spaceWorld == player.world }
        for (star in starList) {
            val distance = player.location.toVector().distance(star.location.toVector())
            val direction = star.location.toVector().subtract(player.location.toVector()).normalize()

            // entity does not exist yet; create it
            if (playerDisplayEntities[star.name] == null) {
                // send packet and create the planet entity
                createPlanetEntity(player, star.name, distance, direction) ?: continue
            }
            // entity exists; update position
            else {
                updatePlanetEntity(player, star.name, distance, direction)
            }
        }
    }
}