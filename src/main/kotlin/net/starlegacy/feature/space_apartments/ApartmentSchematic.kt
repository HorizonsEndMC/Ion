package net.starlegacy.feature.space_apartments

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import net.starlegacy.command.starship.HyperspaceBeaconCommand
import net.starlegacy.util.paste
import net.starlegacy.util.readSchematic
import org.bukkit.Location
import org.bukkit.entity.Player

object ApartmentSchematic {
    private const val SCHEMATIC_FILE_NAME = "space_apartment.schematic"

    private fun getFile() = WorldEdit.getInstance().getWorkingDirectoryFile("schematics/$SCHEMATIC_FILE_NAME")

    private fun getSchematic() = readSchematic(getFile()) ?: error("Missing ${getFile().absolutePath}")

    fun paste(index: Int) {
        val schematic = getSchematic()
        pasteSchematic(schematic, index)
    }

    private fun pasteSchematic(schematic: Clipboard, index: Int) {
        val world = SpaceApartments.getWorld()
        val position = getPastePosition(schematic, index)
        schematic.paste(world, position.x, position.y, position.z)
    }

    private fun getPastePosition(schematic: Clipboard, index: Int): BlockVector3 {
        val xPos = index * schematic.region.width.toDouble()
        val offset = schematic.origin.subtract(schematic.minimumPoint)
        return BlockVector3.at(xPos, 0.0, 0.0).add(offset)
    }

    fun getLocation(index: Int): Location {
        val world = SpaceApartments.getWorld()

        val schematic = getSchematic()
        val vector = getPastePosition(schematic, index)

        val x = vector.x.toDouble()
        val y = vector.y.toDouble()
        val z = vector.z.toDouble()

        return Location(world, x, y, z)
    }

    fun createWorldGuardRegion(index: Int, player: Player) {
        val region = getRegion(index)
        val cuboidRegion = getProtectedRegion(region, index, player)
        addProtectedRegion(cuboidRegion)
    }

    private fun getProtectedRegion(region: Region, index: Int, player: Player): ProtectedCuboidRegion {
        val protectedRegion = createProtectedRegion(region, index)
        setRegionPlayer(protectedRegion, player)
        setRegionFlags(protectedRegion)
        return protectedRegion
    }

    private fun createProtectedRegion(region: Region, index: Int): ProtectedCuboidRegion {
        val min = region.minimumPoint
        val max = region.maximumPoint
        return ProtectedCuboidRegion("apartment_$index", min, max)
    }

    private fun setRegionPlayer(protectedRegion: ProtectedRegion, player: Player) {
        protectedRegion.members.addPlayer(player.uniqueId)
    }

    private fun setRegionFlags(protectedRegion: ProtectedRegion) {
        protectedRegion.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY)
        protectedRegion.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY)
        protectedRegion.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY)
        protectedRegion.setFlag(Flags.PVP, StateFlag.State.DENY)
    }

    private fun getRegion(index: Int): Region {
        val schematic = getSchematic()
        val region = schematic.region.clone()
        val offset = getPastePosition(schematic, index)
        region.shift(schematic.origin.multiply(-1))
        region.shift(offset)
        return region
    }

    private fun addProtectedRegion(cuboidRegion: ProtectedCuboidRegion) {
        val world = SpaceApartments.getWorld()
        val regionManager = WorldGuard.getInstance().platform.regionContainer[BukkitAdapter.adapt(world)]
            ?: error { "Couldn't get region data for world!" }
        val existing = regionManager.getRegion(cuboidRegion.id)
        if (existing != null) {
            regionManager.removeRegion(existing.id)
        }
        regionManager.addRegion(cuboidRegion)
    }
}
