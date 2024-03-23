package net.horizonsend.ion.server.features.client.display

import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftBlockDisplay
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTextDisplay
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Vector

/**
 * Factory for creating client-side display entities.
 */
object ClientDisplayEntityFactory {

    /**
     * Creates a TextDisplay entity. Uses the underlying CraftTextDisplay class so that the entity can be created
     * without spawning in the world. Recommended to use only for client-side rendering; use the TextDisplay
     * interface instead of spawning the entity in the world.
     * @return the TextDisplay entity object
     * @param player the player that the entity will be visible to
     */
    fun createTextDisplay(player: Player): CraftTextDisplay =
        CraftTextDisplay(player.minecraft.server.server, Display.TextDisplay(EntityType.TEXT_DISPLAY, player.minecraft.level()))

    /**
     * Converts a Bukkit TextDisplay to a NMS TextDisplay.
     * @return an NMS TextDisplay
     * @param x the x-coordinate of the TextDisplay
     * @param y the y-coordinate of the TextDisplay
     * @param z the z-coordinate of the TextDisplay
     */
    fun TextDisplay.getNMSData(x: Double, y: Double, z: Double): Display.TextDisplay = (this as CraftTextDisplay).handle.apply { setPos(x, y, z) }

    /**
     * Converts a Bukkit TextDisplay to a NMS TextDisplay.
     * @return an NMS TextDisplay
     * @param vector the location of the TextDisplay represented as a vector
     */
    fun TextDisplay.getNMSData(vector: Vector): Display.TextDisplay = (this as CraftTextDisplay).handle.apply { setPos(vector.x, vector.y, vector.z) }

    /**
     * Creates a BlockDisplay entity. Uses the underlying CraftBlockDisplay class so that the entity can be created
     * without spawning in the world. Recommended to use only for client-side rendering; use the BlockDisplay
     * interface instead of spawning the entity in the world.
     * @return the TextDisplay entity object
     * @param player the player that the entity will be visible to
     */
    fun createDisplayBlock(player: Player): CraftBlockDisplay =
        CraftBlockDisplay(player.minecraft.server.server, Display.BlockDisplay(EntityType.BLOCK_DISPLAY, player.minecraft.level()))

    /**
     * Converts a Bukkit BlockDisplay to a NMS BlockDisplay.
     * @return an NMS BlockDisplay
     * @param x the x-coordinate of the BlockDisplay
     * @param y the y-coordinate of the BlockDisplay
     * @param z the z-coordinate of the BlockDisplay
     */
    fun BlockDisplay.getNMSData(x: Double, y: Double, z: Double): Display.BlockDisplay = (this as CraftBlockDisplay).handle.apply { setPos(x, y, z) }

    /**
     * Converts a Bukkit BlockDisplay to a NMS BlockDisplay.
     * @return an NMS BlockDisplay
     * @param vector the location of the BlockDisplay represented as a vector
     */
    fun BlockDisplay.getNMSData(vector: Vector): Display.BlockDisplay = (this as CraftBlockDisplay).handle.apply { setPos(vector.x, vector.y, vector.z) }
}