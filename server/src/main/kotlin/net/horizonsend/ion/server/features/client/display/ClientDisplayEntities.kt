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
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Slime
import org.bukkit.Bukkit
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Display.*
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
    private val map = mutableMapOf<UUID, MutableMap<String, Display>>()

    operator fun get(uuid: UUID?): MutableMap<String, Display>? = map[uuid]

    /* TODO : I don't like how this is organized; maybe make a better overload in the future, or some way to
              differenetiate between client-side display entities vs. client-side non-display entities? */

    /**
     * Sends a client-side display entity to a client that lasts indefinitely. Has an identifier to reference the entity
     * in the future.
     * @param bukkitPlayer the player that the entity should be visible to
     * @param identifier the identifier of the entity
     * @param entity the NMS entity to send
     */
    fun sendDisplayEntityPacket(bukkitPlayer: Player, entity: Display, identifier: String) {
        val player = bukkitPlayer.minecraft
        val conn = player.connection

        conn.send(ClientboundAddEntityPacket(entity))
        entity.entityData.refresh(player)

        map[bukkitPlayer.uniqueId]?.set(identifier, entity)
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

    // TODO : Work on this at home
    fun transformDisplayEntityPacket(bukkitPlayer: Player, entity: Display) {
        val player = bukkitPlayer.minecraft
        val conn = player.connection

        conn.send(ClientboundAddEntityPacket(entity))
        entity.entityData.refresh(player)
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
            is ForwardingAudience -> for (player in audiences().filterIsInstance<Player>()) { sendEntityPacket(player, highlightBlock(player, pos), duration) }
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
    ): Display.BlockDisplay {

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
     * @param distance the distance that the planet is to the player
     * @param direction the direction that the entity will render from with respect to the player
     */
    fun createPlanetEntity(
        player: Player,
        distance: Double,
        direction: Vector
    ): Display.ItemDisplay? {

        /**
         * Equation for getting the scale of a planet display entity. Maximum (0, 50) and horizontal asymptote at x = 5.
         */
        fun scale(distance: Double) = ((250000000 / ((0.015625 * distance * distance) + 5555555)) + 5).toFloat()

        /**
         * Equation for getting the factor of the planet scaling to maintain apparent visual scale depending on
         * the player's view distance. Calculated assuming that the display entity will, at most, take up 1/3 of
         * the player's screen at a view distance of 10 (160 blocks); y = 1.5h * x / 160h, where h is the apparent
         * visual height of the display entity and x is the view distance of the player in blocks.
         */
        fun viewDistanceFactor(viewDistance: Int) = (0.15 * viewDistance).toFloat()

        val item = createItemDisplay(player)
        // render the entity at the player's max client side view distance, minus 8 blocks
        val entityRenderDistance = (min(player.clientViewDistance, Bukkit.getWorlds()[0].viewDistance) * 16) - 8
        // do not render if the planet is closer than the entity render distance
        if (distance < entityRenderDistance) return null

        item.itemStack = CustomItems.PLANET_ICON_ARET.itemStack(1)
        item.billboard = Billboard.CENTER
        item.viewRange = 5.0f
        item.transformation = Transformation(
            Vector3f(),
            Quaternionf(),
            Vector3f(scale(distance) * viewDistanceFactor(entityRenderDistance)),
            Quaternionf()
        )

        // use the direction vector to offset the entity's position from the player
        val position = player.eyeLocation.toVector().add(direction.clone().normalize().multiply(entityRenderDistance))

        return item.getNMSData(position)
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
}