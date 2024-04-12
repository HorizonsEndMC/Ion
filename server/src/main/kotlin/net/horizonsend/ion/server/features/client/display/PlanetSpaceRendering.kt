package net.horizonsend.ion.server.features.client.display

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.math.PI
import kotlin.math.min

object PlanetSpaceRendering : IonServerComponent() {
    // How often the planet display entities should update in ticks
    private const val PLANET_UPDATE_RATE = 10L

    // The threshold for "hovering" over a planet, in radians
    private const val PLANET_SELECTOR_ANGLE_THRESHOLD = 5.0 / 180.0 * PI

    // The reduced rate at which stars should decrease in scale as the player moves away
    private const val STAR_SCALE_FACTOR = 0.25

    // These vars are for saving the info of the closest
    private val lowestAngleMap = mutableMapOf<UUID, Float>()
    val planetSelectorDataMap = mutableMapOf<UUID, PlanetSelectorData>()

    /**
     * Runs when the server starts. Schedules a task to render the planet entities for each player.
     */
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
        direction: Vector,
        scaleFactor: Double = 1.0
    ): net.minecraft.world.entity.Display.ItemDisplay? {

        /* Start with the Bukkit entity first as the NMS entity has private values that are easier to set by working off
         * the Bukkit wrapper first */
        val entity = ClientDisplayEntityFactory.createItemDisplay(player)
        val entityRenderDistance = getViewDistanceEdge(player)
        // do not render if the planet is closer than the entity render distance
        if (distance < entityRenderDistance * 2) return null

        entity.itemStack = getPlanetItemStack(identifier)
        entity.billboard = Display.Billboard.FIXED
        entity.viewRange = 5.0f
        //entity.interpolationDuration = PLANET_UPDATE_RATE.toInt()
        entity.brightness = Display.Brightness(15, 15)
        //entity.teleportDuration = PLANET_UPDATE_RATE.toInt()

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        val scale = scale(distance, scaleFactor)
        val offset = direction.clone().normalize().multiply(entityRenderDistance + offsetMod(scale))

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
            Vector3f(scale * viewDistanceFactor(entityRenderDistance)),
            Quaternionf()
        )

        // position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
        val nmsEntity = entity.getNMSData(position.x, position.y, position.z)

        ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.set(identifier, nmsEntity)

        return nmsEntity
    }

    /**
     * Updates a client-side ItemDisplay for rendering planet icons in space
     * @param player the player that the entity should be visible to
     * @param identifier the string used to retrieve the entity later
     * @param distance the distance that the planet is to the player
     * @param direction the direction that the entity will render from with respect to the player
     * @param selectable if this entity should be selectable by the player
     */
    private fun updatePlanetEntity(
        player: Player,
        identifier: String,
        distance: Double,
        direction: Vector,
        scaleFactor: Double = 1.0,
        selectable: Boolean = true
    ) {

        val entityRenderDistance = getViewDistanceEdge(player)

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name ||
            distance < entityRenderDistance * 2
        ) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove(identifier)
            return
        } else {
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            val scale = scale(distance, scaleFactor)
            val offset = direction.clone().normalize().multiply(entityRenderDistance + offsetMod(scale))

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
                Vector3f(scale * viewDistanceFactor(entityRenderDistance)),
                Quaternionf()
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)

            if (selectable) {
                // angle in radians
                val angle = player.location.direction.angle(offset)
                // set the lowest angle vars if there is a closer entity to the player's cursor
                if (angle < PLANET_SELECTOR_ANGLE_THRESHOLD && lowestAngleMap[player.uniqueId] != null && lowestAngleMap[player.uniqueId]!! > angle) {
                    lowestAngleMap[player.uniqueId] = angle
                    planetSelectorDataMap[player.uniqueId] =
                        PlanetSelectorData(identifier, entityRenderDistance, direction, scale)
                }
            }
        }
    }

    /**
     * Deletes a client-side ItemDisplay planet
     * @param player the player to delete the planet for
     */
    private fun deletePlanetEntity(player: Player, identifier: String) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return

        ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.remove(identifier)
    }

    /**
     * Creates a client-side ItemDisplay entity for displaying a planet selector in space.
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param data the planet selection data to update with
     */
    private fun createPlanetSelectorEntity(
        player: Player,
        data: PlanetSelectorData
    ): net.minecraft.world.entity.Display.ItemDisplay {

        val entity = ClientDisplayEntityFactory.createItemDisplay(player)

        entity.itemStack = CustomItems.PLANET_SELECTOR.constructItemStack()
        entity.billboard = Display.Billboard.FIXED
        entity.viewRange = 5.0f
        //entity.interpolationDuration = PLANET_UPDATE_RATE.toInt()
        entity.brightness = Display.Brightness(15, 15)
        //entity.teleportDuration = PLANET_UPDATE_RATE.toInt()

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        // subtract 1 to ensure it is rendered before the planet
        val offset = data.direction.clone().normalize().multiply(data.distance - offsetMod(data.scale) - 1)

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
            Vector3f(data.scale * viewDistanceFactor(data.distance)),
            Quaternionf()
        )

        // position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
        val nmsEntity = entity.getNMSData(position.x, position.y, position.z)

        ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
        ClientDisplayEntities.highlightDisplayEntityPacket(player, nmsEntity, true)
        ClientDisplayEntities[player.uniqueId]?.set("planetSelector", nmsEntity)

        return nmsEntity
    }

    /**
     * Updates a client-side ItemDisplay for rendering a planet selector in space
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param data the planet selection data to update with
     */
    private fun updatePlanetSelectorEntity(player: Player, data: PlanetSelectorData) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get("planetSelector") ?: return

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name
        ) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove("planetSelector")
            return
        } else {
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            // subtract 1 to ensure it is rendered before the planet
            val offset = data.direction.clone().normalize().multiply(data.distance - offsetMod(data.scale) - 1)

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
                Vector3f(data.scale * viewDistanceFactor(data.distance)),
                Quaternionf()
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
        }
    }

    /**
     * Deletes a client-side ItemDisplay planet selector
     * @param player the player to delete the planet selector for
     */
    private fun deletePlanetSelectorEntity(player: Player) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get("planetSelector") ?: return

        ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.remove("planetSelector")

        planetSelectorDataMap.remove(player.uniqueId)
    }

    /**
     * Creates a client-side TextDisplay entity for displaying a planet selector in space.
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param data the planet selection data to update with
     */
    private fun createPlanetSelectorTextEntity(
        player: Player,
        data: PlanetSelectorData
    ): net.minecraft.world.entity.Display.TextDisplay {

        val entity = ClientDisplayEntityFactory.createTextDisplay(player)

        entity.text(ofChildren(Component.text(data.name), Component.text(" /jump", NamedTextColor.GREEN)))
        entity.billboard = Display.Billboard.FIXED
        entity.viewRange = 5.0f
        //entity.interpolationDuration = PLANET_UPDATE_RATE.toInt()
        entity.brightness = Display.Brightness(15, 15)
        //entity.teleportDuration = PLANET_UPDATE_RATE.toInt()
        entity.backgroundColor = Color.fromARGB(0x00000000)

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        val offset = data.direction.clone().normalize().multiply(data.distance - offsetMod(data.scale) - 2)
            .apply { this.y -= getTextOffset(data.scale, player) }

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f().mul(-1f)),
            Vector3f(data.scale * viewDistanceFactor(data.distance)),
            Quaternionf()
        )

        // position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
        val nmsEntity = entity.getNMSData(position.x, position.y, position.z)

        ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.set("planetSelectorText", nmsEntity)

        return nmsEntity
    }

    /**
     * Updates a client-side ItemDisplay for rendering a planet selector in space
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param data the planet selection data to update with
     */
    private fun updatePlanetSelectorTextEntity(player: Player, data: PlanetSelectorData) {

        val nmsEntity =
            ClientDisplayEntities[player.uniqueId]?.get("planetSelectorText") as net.minecraft.world.entity.Display.TextDisplay?
                ?: return

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name
        ) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove("planetSelectorText")
            return
        } else {
            nmsEntity.text = PaperAdventure.asVanilla(
                ofChildren(
                    Component.text(data.name),
                    Component.text(" /jump", NamedTextColor.GREEN)
                )
            )
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            val offset = data.direction.clone().normalize().multiply(data.distance - offsetMod(data.scale) - 2)
                .apply { this.y -= getTextOffset(data.scale, player) }

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f().mul(-1f)),
                Vector3f(data.scale * viewDistanceFactor(data.distance)),
                Quaternionf()
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
        }
    }

    /**
     * Deletes a client-side TextDisplay planet selector text
     * @param player the player to delete the planet selector for
     */
    private fun deletePlanetSelectorTextEntity(player: Player) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get("planetSelectorText") ?: return

        ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.remove("planetSelectorText")
    }

    /**
     * Equation for getting the scale of a planet display entity. Maximum (0, 100) and horizontal asymptote at x = 5.
     * @return a scale size for planet display entities
     * @param distance the distance at which the player is from the planet
     * @param scaleReductionFactor adjust the rate at which the curve is reduced
     */
    private fun scale(distance: Double, scaleReductionFactor: Double) = ((500000000 /
            ((0.0625 * distance * distance * scaleReductionFactor) + 5250000)) + 5).toFloat()

    /**
     * Equation for modifying the distance offset of a planet display entity. When added to the offset, decreases the
     * distance by a maximum of two blocks depending on the scale of the planet.
     * @param scale the current scale of the planet
     */
    private fun offsetMod(scale: Float) = scale * -0.02

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
    private fun getViewDistanceEdge(player: Player) =
        (min(player.clientViewDistance, Bukkit.getWorlds()[0].viewDistance) * 16) - 16

    /**
     * Function for getting the distance offset that the planet selector text should be lowered by.
     * @return the distance between the center of the planet selector and the planet selector text
     * @param scale the scale of the planet selector text
     * @param player the affected player
     */
    private fun getTextOffset(scale: Float, player: Player) =
        0.48 * scale * (min(player.clientViewDistance.toDouble(), Bukkit.getWorlds()[0].viewDistance.toDouble()) / 10.0)

    /**
     * Gets the associated custom item from the planet's name.
     * @return the custom planet icon ItemStack
     * @param name the name of the planet
     */
    private fun getPlanetItemStack(name: String): ItemStack = when (name) {
        "Aerach" -> CustomItems.AERACH
        "Aret" -> CustomItems.ARET
        "Chandra" -> CustomItems.CHANDRA
        "Chimgara" -> CustomItems.CHIMGARA
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

        // Reset planet selector information
        lowestAngleMap[player.uniqueId] = Float.MAX_VALUE

        val hudPlanetsImageEnabled = PlayerCache[player].hudPlanetsImage
        val hudPlanetsSelectorEnabled = PlayerCache[player].hudPlanetsSelector

        // Rendering planets
        for (planet in planetList) {
            if (hudPlanetsImageEnabled) {
                val distance = player.location.toVector().distance(planet.location.toVector())
                val direction = planet.location.toVector().subtract(player.location.toVector()).normalize()

                if (playerDisplayEntities[planet.name] == null) {
                    // entity does not exist yet; create it
                    // send packet and create the planet entity
                    createPlanetEntity(player, planet.name, distance, direction) ?: continue
                } else {
                    // entity exists; update position
                    updatePlanetEntity(player, planet.name, distance, direction)
                }
            } else if (playerDisplayEntities[planet.name] != null) {
                deletePlanetEntity(player, planet.name)
            }
        }

        // Rendering stars
        val starList = Space.getStars().filter { it.spaceWorld == player.world }
        for (star in starList) {
            if (hudPlanetsImageEnabled) {
                val distance = player.location.toVector().distance(star.location.toVector())
                val direction = star.location.toVector().subtract(player.location.toVector()).normalize()

                if (playerDisplayEntities[star.name] == null) {
                    // entity does not exist yet; create it
                    // send packet and create the planet entity
                    createPlanetEntity(player, star.name, distance, direction, scaleFactor = STAR_SCALE_FACTOR) ?: continue
                } else {
                    // entity exists; update position
                    updatePlanetEntity(player, star.name, distance, direction, scaleFactor = STAR_SCALE_FACTOR, selectable = false)
                }
            } else if (playerDisplayEntities[star.name] != null) {
                deletePlanetEntity(player, star.name)
            }
        }

        // Rendering planet selector
        if (hudPlanetsSelectorEnabled) {
            if (PilotedStarships[player] != null && lowestAngleMap[player.uniqueId] != null &&
                lowestAngleMap[player.uniqueId]!! < Float.MAX_VALUE
            ) {
                if (playerDisplayEntities["planetSelector"] == null) {
                    // planet should be selected but the planet selector doesn't exist yet
                    createPlanetSelectorEntity(player, planetSelectorDataMap[player.uniqueId]!!)
                    createPlanetSelectorTextEntity(player, planetSelectorDataMap[player.uniqueId]!!)
                } else {
                    // planet selector already exists
                    updatePlanetSelectorEntity(player, planetSelectorDataMap[player.uniqueId]!!)
                    updatePlanetSelectorTextEntity(player, planetSelectorDataMap[player.uniqueId]!!)
                }
            } else {
                // planet is not selected; delete selector if it exists
                deletePlanetSelectorEntity(player)
                deletePlanetSelectorTextEntity(player)
            }
        } else if (playerDisplayEntities["planetSelector"] != null) {
            deletePlanetSelectorEntity(player)
            deletePlanetSelectorTextEntity(player)
        }
    }

    data class PlanetSelectorData(
        val name: String,
        val distance: Int,
        val direction: Vector,
        val scale: Float
    )
}