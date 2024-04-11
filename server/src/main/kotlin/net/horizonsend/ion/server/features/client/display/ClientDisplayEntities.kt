package net.horizonsend.ion.server.features.client.display

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.createBlockDisplay
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
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
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.math.atan2

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

    /**
     * Sends a packet to a client-side entity to teleport it.
     * @param bukkitPlayer the player with the targeted entity
     * @param entity the NMS entity to edit
     * @param x the x-coordinate to teleport the entity to
     * @param y the y-coordinate to teleport the entity to
     * @param z the z-coordinate to teleport the entity to
     */
    fun moveDisplayEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Display, x: Double, y: Double, z: Double) {
        entity.teleportTo(x, y, z)

        val player = bukkitPlayer.minecraft
        val conn = player.connection

        conn.send(ClientboundTeleportEntityPacket(entity))
    }

    /**
     * Sends a packet to a client-side entity to change the transformation of it.
     * @param bukkitPlayer the player with the targeted entity
     * @param entity the NMS entity to edit
     * @param transformation the new transformation assigned to the entity
     */
    fun transformDisplayEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Display, transformation: com.mojang.math.Transformation) {
        val player = bukkitPlayer.minecraft

        entity.setTransformation(transformation)

        entity.entityData.refresh(player)
    }

    /**
     * Sends a packet to a client-side entity to change the glow status of it.
     * @param bukkitPlayer the player with the targeted entity
     * @param entity the NMS entity to edit
     * @param glowing enable or disable the entity's glow
     */
    fun highlightDisplayEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Display, glowing: Boolean) {
        val player = bukkitPlayer.minecraft

        entity.setGlowingTag(glowing)

        entity.entityData.refresh(player)
    }

    /**
     * Sends a packet to a client-side entity to delete it.
     * @param bukkitPlayer the player with the targeted entity
     * @param entity the NMS entity to delete
     */
    fun deleteDisplayEntityPacket(bukkitPlayer: Player, entity: net.minecraft.world.entity.Entity) {
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
     * Function for getting the axis-angle representation of a rotation where an object faces a desired direction.
     * @return the axis-angle representation to get the desired rotation
     * @param direction the direction that an object should face
     */
    fun rotateToFaceVector(direction: Vector3f): Quaternionf {
        // Attempt with axis-angle representation
        // Two rotations are necessary (global transforms assumed):
        // - the first rotation rotates the object around the axis of rotation and results in the finished rotation
        // - the second rotation rolls the object around the direction vector so that the object's local y-axis is
        // aligned as close as possible to the global y-axis

        // Find the axis of rotation and final rotation angle

        // Assuming initial rotation vector is facing SOUTH (+z direction)
        val globalZAxis = Vector3f(0f, 0f, 1f)
        // cross product of initial and final vectors will give the axis of rotation
        val cross = (globalZAxis.clone() as Vector3f).cross((direction.clone() as Vector3f).normalize()).normalize()
        // angle between the initial vector and final vector to determine the rotation needed (in radians)
        val angle = globalZAxis.angle(direction)
        // get the quaternion to rotate the object to face the initial vector
        val firstRotation = Quaternionf(AxisAngle4f(angle, cross))

        // Find the roll amount

        // Method derived from:
        // https://math.stackexchange.com/questions/2548811/find-an-angle-to-rotate-a-vector-around-a-ray-so-that-the-vector-gets-as-close-a

        // In the answer above, this is vector B (the vector to get close to)
        val globalYAxis = Vector3f(0f, 1f, 0f)
        // Vector U is the direction vector given
        // Vector A (the vector to rotate) == Vector E due to A and U already being perpendicular
        // Rotating the global Y axis using the firstRotation transform gets the local Y axis
        val localYAxis = (globalYAxis.clone() as Vector3f).rotate(firstRotation)
        // Vector F (U cross E), equivalent to the local X axis here
        val localXAxis = (direction.clone() as Vector3f).cross((localYAxis.clone() as Vector3f).normalize()).normalize()
        // Theta = arc tangent 2(B dot F, B dot E)
        val rollAngle = atan2(globalYAxis.dot(localXAxis), globalYAxis.dot(localYAxis))

        // get the quaternion to roll the object
        val secondRotation = Quaternionf(AxisAngle4f(rollAngle, (direction.clone() as Vector3f).normalize()))
        // Combine the two rotations

        // When multiplying quaternions together, going from right to left applies the quaternions in that order,
        // assuming rotation from the global PoV (the rotation axes do not need to factor in previous rotations).
        // Going from left to right is equivalent if rotations are applied in that order from a local PoV (the
        // rotation axes must follow previous rotations).
        //
        // This implementation assumes rotation on the global axes, which is why secondRotation appears first and
        // secondRotation uses the direction vector as its axis of rotation (instead of the local Z axis).
        return secondRotation.mul(firstRotation)
    }

    /**
     * Function for getting the axis-angle representation of a rotation where an object faces a desired direction.
     * Only rotates around the y-axis.
     * @return the axis-angle representation to get the desired rotation
     * @param direction the direction that an object should face
     */
    fun rotateToFaceVector2d(direction: Vector3f): Quaternionf {
        // Assuming initial rotation vector is facing SOUTH (+z direction)
        val globalZAxis = Vector3f(0f, 0f, 1f)
        // create a new vector with no y-component
        val flattenedDirection = (direction.clone() as Vector3f).apply { this.y = 0f }
        // angle between vectors to determine the rotation needed (in radians)
        val angle = if (flattenedDirection.x > 0) globalZAxis.angle(flattenedDirection) else globalZAxis.angle(flattenedDirection) * -1
        // return the axis-angle representation, with the axis of rotation around the y-axis
        return Quaternionf(AxisAngle4f(angle, Vector3f(0f, 1f, 0f)))
    }
}