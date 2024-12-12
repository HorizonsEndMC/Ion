package net.horizonsend.ion.server.features.client.display

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.database.cache.BookmarkCache
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.misc.CapturableStationCache
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.math.PI
import kotlin.math.min

object HudIcons : IonServerComponent() {
    // How often the planet display entities should update in ticks
    private const val UPDATE_RATE = 10L

    // The threshold for "hovering" over a planet, in radians
    private const val SELECTOR_ANGLE_THRESHOLD = 5.0 / 180.0 * PI

    // The reduced rate at which stars should decrease in scale as the player moves away
    private const val STAR_SCALE_FACTOR = 0.25

    private const val ICON_SCALE = 20000.0

    private const val SELECTOR_ID = "hud-selector"
    private const val SELECTOR_TEXT_ID = "hud-selector-text"
    private const val PLANET_PREFIX = "hud-planet-"
    private const val STAR_PREFIX = "hud-star-"
    private const val BEACON_PREFIX = "hud-beacon-"
    private const val STATION_PREFIX = "hud-station-"
    private const val SIEGE_STATION_PREFIX = "hud-siege-station-"
    private const val BOOKMARK_PREFIX = "hud-bookmark-"

    // These vars are for saving the info of the closest
    private val lowestAngleMap = mutableMapOf<UUID, Float>()
    val selectorDataMap = mutableMapOf<UUID, PlanetSelectorData>()

    /**
     * Runs when the server starts. Schedules a task to render the planet entities for each player.
     */
    override fun onEnable() {
        Tasks.syncRepeat(0L, UPDATE_RATE) {
            Bukkit.getOnlinePlayers().forEach { player ->
                renderEntities(player)
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
    private fun createHudEntity(
        player: Player,
        identifier: String,
        distance: Double,
        direction: Vector,
        scaleFactor: Double? = 1.0
    ): net.minecraft.world.entity.Display.ItemDisplay? {

        /* Start with the Bukkit entity first as the NMS entity has private values that are easier to set by working off
         * the Bukkit wrapper first */
        val entity = ClientDisplayEntityFactory.createItemDisplay(player)
        val entityRenderDistance = ClientDisplayEntities.getViewDistanceEdge(player)
        // do not render if the planet is closer than the entity render distance
        if (distance < entityRenderDistance * 2) return null

        entity.setItemStack(getItemStack(identifier))
        entity.billboard = Display.Billboard.FIXED
        entity.viewRange = 5.0f
        //entity.interpolationDuration = PLANET_UPDATE_RATE.toInt()
        entity.brightness = Display.Brightness(15, 15)
        entity.teleportDuration = 0

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        val scale = scale(distance, scaleFactor)
        val offset = direction.clone().normalize().multiply(entityRenderDistance + offsetMod(scale))

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
            Vector3f(scale * ClientDisplayEntities.viewDistanceFactor(entityRenderDistance)),
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
     * @param visualDistance the distance that the planet is to the player
     * @param direction the direction that the entity will render from with respect to the player
     * @param selectable if this entity should be selectable by the player
     */
    private fun updateHudEntity(
        player: Player,
        identifier: String,
        visualDistance: Double,
        actualDistance: Double,
        direction: Vector,
        scaleFactor: Double? = 1.0,
        selectable: Boolean = true
    ) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return
        val entityRenderDistance = ClientDisplayEntities.getViewDistanceEdge(player)

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name ||
            visualDistance < entityRenderDistance * 2
        ) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove(identifier)
            return
        } else {
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            val scale = scale(visualDistance, scaleFactor)
            val offset = direction.clone().normalize().multiply(entityRenderDistance + offsetMod(scale))

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
                Vector3f(scale * ClientDisplayEntities.viewDistanceFactor(entityRenderDistance)),
                Quaternionf()
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player.minecraft, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)

            if (selectable) {
                // angle in radians
                val angle = player.location.direction.angle(offset)
                // set the lowest angle vars if there is a closer entity to the player's cursor
                if (angle < SELECTOR_ANGLE_THRESHOLD && lowestAngleMap[player.uniqueId] != null && lowestAngleMap[player.uniqueId]!! > angle) {
                    lowestAngleMap[player.uniqueId] = angle
                    selectorDataMap[player.uniqueId] =
                        PlanetSelectorData(identifier, entityRenderDistance, visualDistance.toInt(), actualDistance.toInt(), direction, scale)
                }
            }
        }
    }

    /**
     * Deletes a client-side ItemDisplay planet
     * @param player the player to delete the planet for
     * @param identifier the identifier of the entity to delete
     */
    private fun deleteHudEntity(player: Player, identifier: String) {

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
    private fun createSelectorEntity(
        player: Player,
        data: PlanetSelectorData
    ): net.minecraft.world.entity.Display.ItemDisplay {

        val entity = ClientDisplayEntityFactory.createItemDisplay(player)

        entity.setItemStack(CustomItemRegistry.PLANET_SELECTOR.constructItemStack())
        entity.billboard = Display.Billboard.FIXED
        entity.viewRange = 5.0f
        //entity.interpolationDuration = PLANET_UPDATE_RATE.toInt()
        entity.brightness = Display.Brightness(15, 15)
        entity.teleportDuration = 0

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        // subtract 1 to ensure it is rendered before the planet
        val offset = data.direction.clone().normalize().multiply(data.entityDistance - offsetMod(data.scale) - 1)

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
            Vector3f(data.scale * ClientDisplayEntities.viewDistanceFactor(data.entityDistance)),
            Quaternionf()
        )

        // position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
        val nmsEntity = entity.getNMSData(position.x, position.y, position.z)

        ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
        ClientDisplayEntities.highlightDisplayEntityPacket(player, nmsEntity, true)
        ClientDisplayEntities[player.uniqueId]?.set(SELECTOR_ID, nmsEntity)

        return nmsEntity
    }

    /**
     * Updates a client-side ItemDisplay for rendering a planet selector in space
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param data the planet selection data to update with
     */
    private fun updateSelectorEntity(player: Player, data: PlanetSelectorData) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(SELECTOR_ID) ?: return

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name
        ) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove(SELECTOR_ID)
            return
        } else {
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            // subtract 1 to ensure it is rendered before the planet
            val offset = data.direction.clone().normalize().multiply(data.entityDistance - offsetMod(data.scale) - 1)

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
                Vector3f(data.scale * ClientDisplayEntities.viewDistanceFactor(data.entityDistance)),
                Quaternionf()
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player.minecraft, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
        }
    }

    /**
     * Deletes a client-side ItemDisplay planet selector
     * @param player the player to delete the planet selector for
     */
    private fun deleteSelectorEntity(player: Player) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(SELECTOR_ID) ?: return

        ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.remove(SELECTOR_ID)

        selectorDataMap.remove(player.uniqueId)
    }

    /**
     * Creates a client-side TextDisplay entity for displaying a planet selector in space.
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param data the planet selection data to update with
     */
    private fun createSelectorTextEntity(
        player: Player,
        data: PlanetSelectorData
    ): net.minecraft.world.entity.Display.TextDisplay {

        val entity = ClientDisplayEntityFactory.createTextDisplay(player)

        entity.text(ofChildren(
            Component.text(sanitizePrefixes(data.name)),
            Component.space(),
            Component.text(data.actualDistance.toString() + "m", NamedTextColor.AQUA),
            Component.space(),
            Component.text("/jump", NamedTextColor.GREEN))
        )
        entity.billboard = Display.Billboard.FIXED
        entity.viewRange = 5.0f
        //entity.interpolationDuration = PLANET_UPDATE_RATE.toInt()
        entity.brightness = Display.Brightness(15, 15)
        entity.teleportDuration = 0
        entity.backgroundColor = Color.fromARGB(0x00000000)

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        val offset = data.direction.clone().normalize().multiply(data.entityDistance - offsetMod(data.scale) - 2)
            .apply { this.y -= getTextOffset(data.scale, player) }

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f().mul(-1f)),
            Vector3f(data.scale * ClientDisplayEntities.viewDistanceFactor(data.entityDistance)),
            Quaternionf()
        )

        // position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
        val nmsEntity = entity.getNMSData(position.x, position.y, position.z)

        ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.set(SELECTOR_TEXT_ID, nmsEntity)

        return nmsEntity
    }

    /**
     * Updates a client-side ItemDisplay for rendering a planet selector in space
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param data the planet selection data to update with
     */
    private fun updateSelectorTextEntity(player: Player, data: PlanetSelectorData) {

        val nmsEntity =
            ClientDisplayEntities[player.uniqueId]?.get(SELECTOR_TEXT_ID) as net.minecraft.world.entity.Display.TextDisplay?
                ?: return

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name
        ) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove(SELECTOR_TEXT_ID)
            return
        } else {
            nmsEntity.text = PaperAdventure.asVanilla(
                ofChildren(
                    Component.text(sanitizePrefixes(data.name)),
                    Component.space(),
                    Component.text(data.actualDistance.toString() + "m", NamedTextColor.AQUA),
                    Component.space(),
                    Component.text("/jump", NamedTextColor.GREEN)
                )
            )
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            val offset = data.direction.clone().normalize().multiply(data.entityDistance - offsetMod(data.scale) - 2)
                .apply { this.y -= getTextOffset(data.scale, player) }

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f().mul(-1f)),
                Vector3f(data.scale * ClientDisplayEntities.viewDistanceFactor(data.entityDistance)),
                Quaternionf()
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player.minecraft, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
        }
    }

    /**
     * Deletes a client-side TextDisplay planet selector text
     * @param player the player to delete the planet selector for
     */
    private fun deleteSelectorTextEntity(player: Player) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(SELECTOR_TEXT_ID) ?: return

        ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.remove(SELECTOR_TEXT_ID)
    }

    /**
     * Equation for getting the scale of a planet display entity. Maximum (0, 100) and horizontal asymptote at x = 5.
     * @return a scale size for planet display entities
     * @param distance the distance at which the player is from the planet
     * @param scaleReductionFactor adjust the rate at which the curve is reduced
     */
    private fun scale(distance: Double, scaleReductionFactor: Double?) = if (scaleReductionFactor != null)
            ((500000000 / ((0.0625 * distance * distance * scaleReductionFactor) + 5250000)) + 5).toFloat()
    else ((500000000 / ((0.0625 * distance * distance) + 5250000)) + 5).toFloat()

    /**
     * Equation for modifying the distance offset of a planet display entity. When added to the offset, decreases the
     * distance by a maximum of two blocks depending on the scale of the planet.
     * @param scale the current scale of the planet
     */
    private fun offsetMod(scale: Float) = scale * -0.02

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
    private fun getItemStack(name: String): ItemStack {
        if (name.contains(PLANET_PREFIX)) {
            return when (name) {
                PLANET_PREFIX + "Aerach" -> CustomItemRegistry.AERACH
                PLANET_PREFIX + "Aret" -> CustomItemRegistry.ARET
                PLANET_PREFIX + "Chandra" -> CustomItemRegistry.CHANDRA
                PLANET_PREFIX + "Chimgara" -> CustomItemRegistry.CHIMGARA
                PLANET_PREFIX + "Damkoth" -> CustomItemRegistry.DAMKOTH
                PLANET_PREFIX + "Disterra" -> CustomItemRegistry.DISTERRA
                PLANET_PREFIX + "Eden" -> CustomItemRegistry.EDEN
                PLANET_PREFIX + "Gahara" -> CustomItemRegistry.GAHARA
                PLANET_PREFIX + "Herdoli" -> CustomItemRegistry.HERDOLI
                PLANET_PREFIX + "Ilius" -> CustomItemRegistry.ILIUS
                PLANET_PREFIX + "Isik" -> CustomItemRegistry.ISIK
                PLANET_PREFIX + "Kovfefe" -> CustomItemRegistry.KOVFEFE
                PLANET_PREFIX + "Krio" -> CustomItemRegistry.KRIO
                PLANET_PREFIX + "Lioda" -> CustomItemRegistry.LIODA
                PLANET_PREFIX + "Luxiterna" -> CustomItemRegistry.LUXITERNA
                PLANET_PREFIX + "Qatra" -> CustomItemRegistry.QATRA
                PLANET_PREFIX + "Rubaciea" -> CustomItemRegistry.RUBACIEA
                PLANET_PREFIX + "Turms" -> CustomItemRegistry.TURMS
                PLANET_PREFIX + "Vask" -> CustomItemRegistry.VASK

                PLANET_PREFIX + "Asteri" -> CustomItemRegistry.ASTERI
                PLANET_PREFIX + "EdenHack" -> CustomItemRegistry.HORIZON
                PLANET_PREFIX + "Ilios" -> CustomItemRegistry.ILIOS
                PLANET_PREFIX + "Regulus" -> CustomItemRegistry.REGULUS
                PLANET_PREFIX + "Sirius" -> CustomItemRegistry.SIRIUS

                else -> CustomItemRegistry.AERACH
            }.constructItemStack()
        }

        else if (name.contains(STAR_PREFIX)) {
            return when (name) {
                STAR_PREFIX + "Asteri" -> CustomItemRegistry.ASTERI
                STAR_PREFIX + "Horizon" -> CustomItemRegistry.HORIZON
                STAR_PREFIX + "Ilios" -> CustomItemRegistry.ILIOS
                STAR_PREFIX + "Regulus" -> CustomItemRegistry.REGULUS
                STAR_PREFIX + "Sirius" -> CustomItemRegistry.SIRIUS

                else -> CustomItemRegistry.ASTERI
            }.constructItemStack()
        }

        else if (name.contains(BEACON_PREFIX)) {
            return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.BEACON.customModelData) }
        }

        else if (name.contains(STATION_PREFIX)) {
            return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
        }

        else if (name.contains(SIEGE_STATION_PREFIX)) {
            return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
        }

        else if (name.contains(BOOKMARK_PREFIX)) {
            return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.BOOKMARK.customModelData) }
        }

        else return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GENERIC_STARSHIP.customModelData) }
    }

    /**
     * Removes prefixes of entity names to make them more user-friendly.
     * @param name the name to sanitize
     */
    fun sanitizePrefixes(name: String): String {
        return if (name.contains(PLANET_PREFIX)) name.removePrefix(PLANET_PREFIX)
        else if (name.contains(STAR_PREFIX)) name.removePrefix(STAR_PREFIX)
        else if (name.contains(BEACON_PREFIX)) name.removePrefix(BEACON_PREFIX)
        else if (name.contains(STATION_PREFIX)) name.removePrefix(STATION_PREFIX)
        else if (name.contains(SIEGE_STATION_PREFIX)) name.removePrefix(SIEGE_STATION_PREFIX)
        else if (name.contains(BOOKMARK_PREFIX)) name.removePrefix(BOOKMARK_PREFIX)
        else name
    }

    /**
     * Renders client-side ItemEntity planets for each player.
     * @param player the player to send objects to
     */
    private fun renderEntities(player: Player) {
        // Only render planets if the player is in a space world
        if (!player.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) return

        val planetList = Space.getPlanets().filter { it.spaceWorld == player.world }
        val playerDisplayEntities = ClientDisplayEntities[player.uniqueId] ?: return

        // Reset planet selector information
        lowestAngleMap[player.uniqueId] = Float.MAX_VALUE

        val hudSelectorEnabled = PlayerCache[player].hudPlanetsSelector
        val hudPlanetsEnabled = PlayerCache[player].hudPlanetsImage
        val hudStarsEnabled = PlayerCache[player].hudIconStars
        val hudBeaconsEnabled = PlayerCache[player].hudIconBeacons
        val hudStationsEnabled = PlayerCache[player].hudIconStations
        val hudBookmarksEnabled = PlayerCache[player].hudIconBookmarks

        // Rendering planets
        for (planet in planetList) {
            val hudName = PLANET_PREFIX + planet.name

            if (hudPlanetsEnabled) {
                val distance = player.location.toVector().distance(planet.location.toVector())
                val direction = planet.location.toVector().subtract(player.location.toVector()).normalize()

                if (playerDisplayEntities[hudName] == null) {
                    // entity does not exist yet; create it
                    // send packet and create the planet entity
                    createHudEntity(player, hudName, distance, direction) ?: continue
                } else {
                    // entity exists; update position
                    updateHudEntity(player, hudName, distance, distance, direction)
                }
            } else if (playerDisplayEntities[hudName] != null) {
                deleteHudEntity(player, hudName)
            }
        }

        // Rendering stars
        val starList = Space.getStars().filter { it.spaceWorld == player.world }
        for (star in starList) {
            val hudName = STAR_PREFIX + star.name

            if (hudStarsEnabled) {
                val distance = player.location.toVector().distance(star.location.toVector())
                val direction = star.location.toVector().subtract(player.location.toVector()).normalize()

                if (playerDisplayEntities[hudName] == null) {
                    // entity does not exist yet; create it
                    // send packet and create the planet entity
                    createHudEntity(player, hudName, distance, direction, scaleFactor = STAR_SCALE_FACTOR) ?: continue
                } else {
                    // entity exists; update position
                    updateHudEntity(player, hudName, distance, distance, direction, scaleFactor = STAR_SCALE_FACTOR, selectable = false)
                }
            } else if (playerDisplayEntities[hudName] != null) {
                deleteHudEntity(player, hudName)
            }
        }

        // Rendering beacon
        val beaconList = ConfigurationFiles.serverConfiguration().beacons.filter { it.spaceLocation.bukkitWorld() == player.world }
        for (beacon in beaconList) {
            val hudName = BEACON_PREFIX + beacon.name

            if (hudBeaconsEnabled) {
                val distance = player.location.toVector().distance(beacon.spaceLocation.toVector())
                val direction = beacon.spaceLocation.toVector().subtract(player.location.toVector()).normalize()

                if (playerDisplayEntities[hudName] == null) {
                    createHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        direction,
                        scaleFactor = null
                    ) ?: continue
                } else {
                    updateHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        distance,
                        direction,
                        scaleFactor = null
                    )
                }
            } else if (playerDisplayEntities[hudName] != null) {
                deleteHudEntity(player, hudName)
            }
        }

        // Rendering station
        val stationList = SpaceStationCache.all().filter { it.world == player.world.name }
        for (station in stationList) {
            val hudName = STATION_PREFIX + station.name
            val stationLocation = Vector(station.x, 192, station.z)

            if (hudStationsEnabled) {
                val distance = player.location.toVector().distance(stationLocation)
                val direction = stationLocation.clone().subtract(player.location.toVector())

                if (playerDisplayEntities[hudName] == null) {
                    createHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        direction,
                        scaleFactor = null
                    ) ?: continue
                } else {
                    updateHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        distance,
                        direction,
                        scaleFactor = null
                    )
                }
            } else if (playerDisplayEntities[hudName] != null) {
                deleteHudEntity(player, hudName)
            }
        }

        // Rendering siege station
        val capturableStationList = CapturableStationCache.stations.filter {
            it.loc.world != null && it.loc.world.name == player.world.name
        }
        for (siegeStation in capturableStationList) {
            val hudName = SIEGE_STATION_PREFIX + siegeStation.name

            if (hudStationsEnabled) {
                val distance = player.location.toVector().distance(siegeStation.loc.toVector())
                val direction = siegeStation.loc.toVector().subtract(player.location.toVector())

                if (playerDisplayEntities[hudName] == null) {
                    createHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        direction,
                        scaleFactor = null
                    ) ?: continue
                } else {
                    updateHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        distance,
                        direction,
                        scaleFactor = null
                    )
                }
            } else if (playerDisplayEntities[hudName] != null) {
                deleteHudEntity(player, hudName)
            }
        }

        // Rendering bookmark
        val bookmarkList = BookmarkCache.getAll().filter { it.owner == player.slPlayerId && it.worldName == player.world.name }
        for (bookmark in bookmarkList) {
            val hudName = BOOKMARK_PREFIX + bookmark.name
            val bookmarkLocation = Vector(bookmark.x, bookmark.y, bookmark.z)

            if (hudBookmarksEnabled) {
                val distance = player.location.toVector().distance(bookmarkLocation)
                val direction = bookmarkLocation.clone().subtract(player.location.toVector())

                if (playerDisplayEntities[hudName] == null) {
                    createHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        direction,
                        scaleFactor = null
                    ) ?: continue
                } else {
                    updateHudEntity(
                        player,
                        hudName,
                        ICON_SCALE,
                        distance,
                        direction,
                        scaleFactor = null
                    )
                }
            } else if (playerDisplayEntities[hudName] != null) {
                deleteHudEntity(player, hudName)
            }
        }

        // Rendering planet selector
        if (hudSelectorEnabled) {
            if (PilotedStarships[player] != null && lowestAngleMap[player.uniqueId] != null &&
                lowestAngleMap[player.uniqueId]!! < Float.MAX_VALUE
            ) {
                if (playerDisplayEntities[SELECTOR_ID] == null) {
                    // planet should be selected but the planet selector doesn't exist yet
                    createSelectorEntity(player, selectorDataMap[player.uniqueId]!!)
                    createSelectorTextEntity(player, selectorDataMap[player.uniqueId]!!)
                } else {
                    // planet selector already exists
                    updateSelectorEntity(player, selectorDataMap[player.uniqueId]!!)
                    updateSelectorTextEntity(player, selectorDataMap[player.uniqueId]!!)
                }
            } else {
                // planet is not selected; delete selector if it exists
                deleteSelectorEntity(player)
                deleteSelectorTextEntity(player)
            }
        } else if (playerDisplayEntities[SELECTOR_ID] != null) {
            deleteSelectorEntity(player)
            deleteSelectorTextEntity(player)
        }
    }

    /**
     * Event handler that updates HUD planets when a player teleports.
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    private fun onPlayerTeleport(event: PlayerTeleportEvent) {
        Tasks.sync {
            renderEntities(event.player)
        }
    }

    data class PlanetSelectorData(
        val name: String,
        val entityDistance: Int,    // the distance that the physical entity is from the player
        val visualDistance: Int,    // the apparent visual distance that the object is from the player
        val actualDistance: Int,    // the actual distance from the player (used for data)
        val direction: Vector,
        val scale: Float
    )
}
