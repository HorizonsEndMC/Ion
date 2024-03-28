package net.horizonsend.ion.server.features.client.display

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.createBlockDisplay
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.createItemDisplay
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Slime
import org.bukkit.Bukkit
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.math.min

/**
 * Functions for creating client-side display entities.
 */
object ClientDisplayEntities : IonServerComponent() {

    /**
     * Map to store display entity information associated with each player.
     */
    private val map = mutableMapOf<UUID, MutableMap<String, net.minecraft.world.entity.Display>>()

    operator fun get(uuid: UUID?): MutableMap<String, net.minecraft.world.entity.Display>? = map[uuid]

    /* TODO : I don't like how this is organized; maybe make a better overload in the future, or some way to
              differenetiate between client-side display entities vs. client-side non-display entities? */

    /**
     * Sends a client-side display entity to a client that lasts indefinitely. Has an identifier to reference the entity
     * in the future.
     * @param bukkitPlayer the player that the entity should be visible to
     * @param entity the NMS entity to send
     */
    fun sendEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Entity) {
        val player = bukkitPlayer.minecraft
        val conn = player.connection

        conn.send(ClientboundAddEntityPacket(entity))
        entity.entityData.refresh(player)
    }

    /**
     * Sends a client-side entity to a client that lasts for a set duration.
     * @param bukkitPlayer the player that the entity should be visible to
     * @param entity the NMS entity to send
     * @param duration the duration that the entity should be visible for
     */
    fun sendEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Entity, duration: Long) {
        val player = bukkitPlayer.minecraft
        val conn = player.connection

        conn.send(ClientboundAddEntityPacket(entity))
        entity.entityData.refresh(player)

        Tasks.syncDelayTask(duration) { conn.send(ClientboundRemoveEntitiesPacket(entity.id)) }
    }

    private fun moveDisplayEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Display, x: Double, y: Double, z: Double) {
        entity.teleportTo(x, y, z)

        val player = bukkitPlayer.minecraft
        val conn = player.connection

        conn.send(ClientboundTeleportEntityPacket(entity))
    }

    private fun transformDisplayEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Display, transformation: com.mojang.math.Transformation) {
        val player = bukkitPlayer.minecraft

        entity.setTransformation(transformation)

        entity.entityData.refresh(player)
    }

    private fun deleteDisplayEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Display) {
        val player = bukkitPlayer.minecraft
        val conn = player.connection

        conn.send(ClientboundRemoveEntitiesPacket(entity.id))
    }

    /**
     * Creates a glowing block outline.
     * @return an invisible, glowing NMS slime object that represents the glowing outline
     * @param player the player that the entity should be visible to
     * @param pos the position of the entity
     */
    fun highlightBlock(player: Player, pos: Vec3i): net.minecraft.world.entity.Entity {
        return Slime(EntityType.SLIME, player.minecraft.level()).apply {
            setPos(pos.x + 0.5, pos.y + 0.25, pos.z + 0.5)
            this.setSize(1, true)
            setGlowingTag(true)
            isInvisible = true

        }
    }

    fun Audience.highlightBlock(pos: Vec3i, duration: Long) {
        when (this) {
            is Player -> sendEntityPacket(this, highlightBlock(this, pos), duration)
            is ForwardingAudience -> for (player in audiences().filterIsInstance<Player>()) {
                sendEntityPacket(player, highlightBlock(player, pos), duration)
            }
        }
    }

    private fun Audience.highlightBlocks(positions: Collection<Vec3i>, duration: Long) {
        for (pos in positions) this.highlightBlock(pos, duration)
    }

    fun debugHighlightBlock(x: Number, y: Number, z: Number, duration: Long = 5L) = debugAudience.highlightBlock(Vec3i(x.toInt(), y.toInt(), z.toInt()), duration)
    fun debugHighlightBlock(pos: Vec3i, duration: Long = 5L) = debugAudience.highlightBlock(pos, duration)
    fun debugHighlightBlocks(blocks: Collection<Vec3i>, duration: Long = 5L) = debugAudience.highlightBlocks(blocks, duration)

    /**
     * Creates a client-side block object.
     * @return the NMS BlockDisplay object
     * @param player the player that the entity should be visible to
     * @param blockData the block information to use
     * @param pos the position of the entity
     * @param scale the size of the entity
     * @param glow if the BlockDisplay should glow
     */
    fun displayBlock(
        player: Player,
        blockData: BlockData,
        pos: Vector,
        scale: Float = 1.0f,
        glow: Boolean = false
    ): net.minecraft.world.entity.Display.BlockDisplay {

        val block = createBlockDisplay(player)
        val offset = (-scale / 2) + 0.5
        block.block = blockData
        block.isGlowing = glow
        block.transformation = Transformation(Vector3f(0f), Quaternionf(), Vector3f(scale), Quaternionf())

        return block.getNMSData(pos.x + offset, pos.y + offset, pos.z + offset)
    }

    /**
     * Creates a client-side ItemDisplay entity for rendering planet icons in space.
     * @return the NMS ItemDisplay object
     * @param player the player that the entity should be visible to
     * @param identifier the string used to retrieve the entity later
     * @param distance the distance that the planet is to the player
     * @param direction the direction that the entity will render from with respect to the player
     */
    fun createPlanetEntity(
        player: Player,
        identifier: String,
        distance: Double,
        direction: Vector
    ): net.minecraft.world.entity.Display.ItemDisplay? {

        val entity = createItemDisplay(player)
        val entityRenderDistance = getViewDistanceEdge(player)
        // do not render if the planet is closer than the entity render distance
        if (distance < entityRenderDistance) return null

        entity.itemStack = CustomItems.PLANET_ICON_ARET.itemStack(1)
        entity.billboard = Billboard.FIXED
        entity.viewRange = 5.0f
        entity.interpolationDuration = 20
        entity.teleportDuration = 20

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        val offset = direction.clone().normalize().multiply(entityRenderDistance)

        // apply transformation
        entity.transformation = Transformation(
            offset.toVector3f(),
            Quaternionf(),
            Vector3f(scale(distance) * viewDistanceFactor(entityRenderDistance)),
            Quaternionf()
        )

        val nmsEntity = entity.getNMSData()

        sendEntityPacket(player, nmsEntity)
        moveDisplayEntityPacket(player, nmsEntity, position.x, position.y, position.z)
        map[player.uniqueId]?.set(identifier, nmsEntity)

        println("CREATING ENTITY: ${nmsEntity.id}")
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
    fun updatePlanetEntity(player: Player, identifier: String, distance: Double, direction: Vector) {

        val entityRenderDistance = getViewDistanceEdge(player)

        val nmsEntity = map[player.uniqueId]?.get(identifier) ?: return

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded ||
            nmsEntity.level().world.name != player.world.name ||
            distance < entityRenderDistance
            ) {
            deleteDisplayEntityPacket(player, nmsEntity)
            map[player.uniqueId]?.remove(identifier)
            return
        }
        else {
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            val offset = direction.clone().normalize().multiply(entityRenderDistance)

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                Quaternionf(),
                Vector3f(scale(distance) * viewDistanceFactor(entityRenderDistance)),
                Quaternionf()
            )

            println("UPDATING ENTITY: ${nmsEntity.id}")
            println("map: ${map[player.uniqueId]}")

            moveDisplayEntityPacket(player, nmsEntity, position.x, position.y, position.z)
            transformDisplayEntityPacket(player, nmsEntity, transformation)
        }
    }

    /**
     * Handler that adds a new entry to the DisplayEntityData map when a player joins the server.
     * @param event PlayerJoinEvent
     */
    @Suppress("unused")
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        map[event.player.uniqueId] = mutableMapOf()
    }

    /**
     * Handler that removes an entry from the DisplayEntityData map when a player leaves the server.
     * @param event PlayerQuitEvent
     */
    @Suppress("unused")
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (map[event.player.uniqueId] != null) {
            map.remove(event.player.uniqueId)
        }
    }

    /**
     * Equation for getting the scale of a planet display entity. Maximum (0, 50) and horizontal asymptote at x = 5.
     */
    private fun scale(distance: Double) = ((250000000 / ((0.015625 * distance * distance) + 5555555)) + 5).toFloat()

    /**
     * Equation for getting the factor of the planet scaling to maintain apparent visual scale depending on
     * the player's view distance. Calculated assuming a default view distance of 10 (160 blocks); 0.5h / 160 = h` / x,
     * where h is the apparent visual height of the display entity, h` is the apparent visual height of the display
     * entity after transformation, and x is the view distance of the player in blocks.
     */
    private fun viewDistanceFactor(viewDistance: Int) = (0.003125 * viewDistance).toFloat()

    /**
     * Function for getting the distance from the edge of the player's view distance, minus several blocks.
     */
    private fun getViewDistanceEdge(player: Player) = (min(player.clientViewDistance, Bukkit.getWorlds()[0].viewDistance) * 16) - 16
}