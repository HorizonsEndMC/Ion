package net.horizonsend.ion.server.features.world.generation.feature.meta.wreck

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.miscellaneous.utils.LegacyBlockUtils.getRotatedBlockData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.Rotation
import org.bukkit.block.data.BlockData
import java.io.File
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class SchematicWreckStructure(key: IonRegistryKey<WreckStructure, out WreckStructure>, clipboardName: String) : WreckStructure(key) {
	private val file: File = IonServer.dataFolder.resolve("generation").resolve("wrecks").apply { mkdirs() }.resolve("$clipboardName.schem")
	val clipboard: Clipboard = readSchematic(file) ?: throw IllegalStateException("Could not read clipboard $clipboardName!")

	private val minPoint: Vec3i = clipboard.region.minimumPoint.subtract(clipboard.origin).toVec3i()
	private val maxPoint: Vec3i = clipboard.region.maximumPoint.subtract(clipboard.origin).toVec3i()

	override fun getExtents(metaData: WreckMetaData): Pair<Vec3i, Vec3i> {
		val rotatedMin = getLocalCoordinate(minPoint.x, minPoint.y, minPoint.z, metaData)
		val rotatedMax = getLocalCoordinate(maxPoint.x, maxPoint.y, maxPoint.z, metaData)

		return Pair(
			Vec3i(minOf(rotatedMin.x(), rotatedMax.x()), minPoint.y, minOf(rotatedMin.z(), rotatedMax.z())),
			Vec3i(maxOf(rotatedMin.x(), rotatedMax.x()), maxPoint.y, maxOf(rotatedMin.z(), rotatedMax.z()))
		)
	}

	private fun getLocalCoordinate(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, metaData: WreckMetaData): BlockVector3 {
		val angle = when(metaData.rotation) {
			Rotation.NONE -> 0.0
			Rotation.CLOCKWISE_90 -> -90.0
			Rotation.CLOCKWISE_180 -> 180.0
			Rotation.COUNTERCLOCKWISE_90 -> 90.0
		}

		val cosTheta: Double = cos(Math.toRadians(angle))
		val sinTheta: Double = sin(Math.toRadians(angle))

		val rotatedOffset = BlockVector3.at(
			(startOffsetX.toDouble() * cosTheta - startOffsetZ.toDouble() * sinTheta).roundToInt(),
			startOffsetY,
			(startOffsetX.toDouble() * sinTheta + startOffsetZ.toDouble() * cosTheta).roundToInt()
		)

		return clipboard.origin.subtract(rotatedOffset)
	}

	override fun getBlockData(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, metaData: WreckMetaData): BlockData {
		val local = getLocalCoordinate(startOffsetX, startOffsetY, startOffsetZ, metaData)
		val baseBlock = clipboard.getFullBlock(local)

		return getRotatedBlockData(BukkitAdapter.adapt(baseBlock), metaData.rotation).createCraftBlockData()
	}

	override fun getNBTData(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, realX: Int, realY: Int, realZ: Int, metaData: WreckMetaData): CompoundTag? {
		val local = getLocalCoordinate(startOffsetX, startOffsetY, startOffsetZ, metaData)
		val nbt = clipboard.getFullBlock(local).nbt?.nms() as? CompoundTag ?: return null

		nbt.putInt("x", realX)
		nbt.putInt("y", realY)
		nbt.putInt("z", realZ)

		return nbt
	}

	override fun isInBounds(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, metaData: WreckMetaData): Boolean {
		val local = getLocalCoordinate(startOffsetX, startOffsetY, startOffsetZ, metaData)
		return clipboard.region.contains(local)
	}
}
