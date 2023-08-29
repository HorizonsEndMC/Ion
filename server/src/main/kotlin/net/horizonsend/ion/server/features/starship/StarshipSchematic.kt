package net.horizonsend.ion.server.features.starship

import com.sk89q.worldedit.world.World as WEWorld
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object StarshipSchematic {
	fun createSchematic(starship: ActiveStarship): Clipboard {
		val minPoint = starship.min
		val maxPoint = starship.max
		val min = BlockVector3.at(minPoint.x, minPoint.y, minPoint.z)
		val max = BlockVector3.at(maxPoint.x, maxPoint.y, maxPoint.z)
		val region = CuboidRegion(min, max)
		val clipboard = BlockArrayClipboard(region)

		val origin = if (starship is ActiveControlledStarship) Vec3i(starship.data.blockKey) else starship.centerOfMass
		clipboard.origin = BlockVector3.at(origin.x, origin.y, origin.z)
		val weWorld: WEWorld = BukkitAdapter.adapt(starship.world)
		starship.iterateBlocks { x, y, z ->
			val pos = BlockVector3.at(x, y, z)
			clipboard.setBlock(pos, weWorld.getFullBlock(pos))
		}

		return clipboard
	}

	fun serializeSchematic(clipboard: Clipboard): ByteArray? {
		val stream = ByteArrayOutputStream()
		BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(stream).use { writer: ClipboardWriter ->
			writer.write(clipboard)
		}
		return stream.toByteArray()
	}

	fun deserializeSchematic(byteArray: ByteArray): Clipboard {
		val stream = ByteArrayInputStream(byteArray)
		return BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(stream).read()
	}
}
