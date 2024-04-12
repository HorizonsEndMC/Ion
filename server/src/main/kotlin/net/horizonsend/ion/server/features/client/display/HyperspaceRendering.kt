package net.horizonsend.ion.server.features.client.display

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftBlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

object HyperspaceRendering : IonServerComponent() {
    // How often the planet display entities should update in ticks
    private const val HYPERSPACE_UPDATE_RATE = 20L

    // How large the hyperspace "walls" should be, relative to a normally sized block
    private const val HYPERSPACE_WALL_SCALE = 100f

    // List of blockfaces to render the hyperspace wall entities for
    private val blockFaces = BlockFace.entries.filter { it.isCartesian }

    /**
     * Runs when the server starts. Schedules a task to render the hyperspace wall entities for each player.
     */
    override fun onEnable() {
        Tasks.syncRepeat(0L, HYPERSPACE_UPDATE_RATE) {
            Bukkit.getOnlinePlayers().forEach { player ->
                renderHyperspace(player)
            }
        }
    }

    /**
     * Creates a client-side BlockDisplay entity for rendering a hyperspace wall
     * @returns the NMS BlockDisplay object
     * @param player the player that the entity should be visible to
     * @param blockFace the BlockFace that the wall should face
     */
    private fun createHyperspaceWallEntity(
        player: Player,
        blockFace: BlockFace
    ): net.minecraft.world.entity.Display.BlockDisplay {
        /* Start with the Bukkit entity first as the NMS entity has private values that are easier to set by working off
         * the Bukkit wrapper first */
        val entity = ClientDisplayEntityFactory.createBlockDisplay(player)
        val entityRenderDistance = ClientDisplayEntities.getViewDistanceEdge(player)

        entity.block = Material.END_GATEWAY.createBlockData()
        entity.viewRange = 5.0f
        entity.brightness = Display.Brightness(15, 15)

        // calculate position and offset
        val position = player.eyeLocation.toVector()
        val direction = blockFace.direction
        val scale = HYPERSPACE_WALL_SCALE * ClientDisplayEntities.viewDistanceFactor(entityRenderDistance)
        // Because the origin of a BlockDisplay is the corner of the block, the scale will not originate from the center
        // transformationOffset corrects this
        val transformationOffset = (-scale / 2) + 0.5
        val offset = direction.clone().normalize().apply {
            x = x * entityRenderDistance + transformationOffset
            y = y * entityRenderDistance + transformationOffset
        }
        val identifier = hyperspaceIdentifierConstructor(blockFace)

        entity.transformation = Transformation(
            offset.toVector3f(),
            ClientDisplayEntities.rotateToFaceVector(direction.toVector3f()),
            Vector3f(scale),
            Quaternionf()
        )

        // position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
        val nmsEntity = entity.getNMSData(position.x, position.y, position.z)

        ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.set(identifier, nmsEntity)

        return nmsEntity
    }

    /**
     * Updates a client-side BlockDisplay for rendering a hyperspace wall
     * @param player the player that the entity should be visible to
     * @param blockFace the BlockFace that the entity should face
     * @param identifier the string used to retrieve the entity later
     */
    private fun updateHyperspaceWallEntity(
        player: Player,
        blockFace: BlockFace,
        identifier: String
    ) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return
        val entityRenderDistance = ClientDisplayEntities.getViewDistanceEdge(player)

        // remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
        // also do not render if the planet is closer than the entity render distance
        if (!nmsEntity.isChunkLoaded || nmsEntity.level().world.name != player.world.name) {
            ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
            ClientDisplayEntities[player.uniqueId]?.remove(identifier)
            return
        } else {
            // calculate position and offset
            val position = player.eyeLocation.toVector()
            val direction = blockFace.direction
            val scale = HYPERSPACE_WALL_SCALE * ClientDisplayEntities.viewDistanceFactor(entityRenderDistance)
            val transformationOffset = (-scale / 2) + 0.5
            val offset = direction.clone().normalize().apply {
                x = x * entityRenderDistance + transformationOffset
                y = y * entityRenderDistance + transformationOffset
            }
            val oldTransformation = (nmsEntity.bukkitEntity as CraftBlockDisplay).transformation

            // apply transformation
            val transformation = com.mojang.math.Transformation(
                offset.toVector3f(),
                oldTransformation.leftRotation,
                oldTransformation.scale,
                oldTransformation.rightRotation
            )

            ClientDisplayEntities.moveDisplayEntityPacket(player, nmsEntity, position.x, position.y, position.z)
            ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
        }
    }

    /**
     * Deletes a client-side BlockDisplay hyperspace wall
     * @param player the player to delete the hyperspace wall for
     * @param identifier the identifier of the entity to delete
     */
    private fun deleteHyperspaceWallEntity(player: Player, identifier: String) {

        val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return

        ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
        ClientDisplayEntities[player.uniqueId]?.remove(identifier)
    }

    /**
     * Gets the identifier for the hyperspace wall facing a direction
     * @param blockFace the BlockFace that the hyperspace wall is facing
     */
    private fun hyperspaceIdentifierConstructor(blockFace: BlockFace) = "hyperspace${blockFace.name}"

    /**
     * Renders client-side BlockDisplay hyperspace walls for each player.
     * @param player the player to send objects to
     */
    private fun renderHyperspace(player: Player) {
        val playerDisplayEntities = ClientDisplayEntities[player.uniqueId] ?: return

        // Only render hyperspace walls if the player is in hyperspace
        if (!Hyperspace.isHyperspaceWorld(player.world)) {
            for (blockFace in blockFaces) {
                if (playerDisplayEntities[hyperspaceIdentifierConstructor(blockFace)] != null) {
                    deleteHyperspaceWallEntity(player, hyperspaceIdentifierConstructor(blockFace))
                }
            }
            return
        }

        for (blockFace in blockFaces) {
            if (playerDisplayEntities[hyperspaceIdentifierConstructor(blockFace)] == null) {
                // entity does not exist yet; create it
                // send packet and create the entity
                createHyperspaceWallEntity(player, blockFace)
            } else {
                // entity exists; update position
                updateHyperspaceWallEntity(player, blockFace, hyperspaceIdentifierConstructor(blockFace))
            }
        }
    }
}