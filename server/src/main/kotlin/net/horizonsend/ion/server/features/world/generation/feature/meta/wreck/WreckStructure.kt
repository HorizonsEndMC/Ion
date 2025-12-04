package net.horizonsend.ion.server.features.world.generation.feature.meta.wreck

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import org.bukkit.block.data.BlockData
import java.io.File

class WreckStructure(override val key: IonRegistryKey<WreckStructure, out WreckStructure>, clipboardName: String) : Keyed<WreckStructure> {
	private val file: File = IonServer.dataFolder.resolve("generation").resolve("wrecks").apply { mkdirs() }.resolve("$clipboardName.schem")
	val clipboard: Clipboard = readSchematic(file) ?: throw IllegalStateException("Could not read clipboard $clipboardName!")

	val minPoint: Vec3i = clipboard.region.minimumPoint.subtract(clipboard.origin).toVec3i()
	val maxPoint: Vec3i = clipboard.region.maximumPoint.subtract(clipboard.origin).toVec3i()

	fun getBlockData(localX: Int, localY: Int, localZ: Int): BlockData {
		val adjusted = clipboard.origin.subtract(localX, localY, localZ)
		val baseBlock = clipboard.getFullBlock(adjusted)

		return BukkitAdapter.adapt(baseBlock)
	}

	fun isInBounds(localX: Int, localY: Int, localZ: Int): Boolean {
		val adjusted = clipboard.origin.subtract(localX, localY, localZ)
		return clipboard.region.contains(adjusted)
	}
}
