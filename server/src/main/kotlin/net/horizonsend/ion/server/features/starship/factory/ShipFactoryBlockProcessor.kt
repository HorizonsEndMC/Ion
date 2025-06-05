package net.horizonsend.ion.server.features.starship.factory

import com.sk89q.worldedit.extent.clipboard.Clipboard
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMaps
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactorySettings
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.loadClipboard
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.toBukkitBlockData
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.javautil.SignUtils
import net.starlegacy.javautil.SignUtils.SignData
import org.bukkit.block.data.BlockData
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

abstract class ShipFactoryBlockProcessor(
	protected val blueprint: Blueprint,
	protected val settings: ShipFactorySettings,
	val entity: ShipFactoryEntity
) {
	protected val clipboard: Clipboard by lazy { blueprint.loadClipboard() }

	// Use a RB tree map for key ordering.
	val blockMap: MutableMap<BlockKey, BlockData> = Long2ObjectSortedMaps.synchronize(Long2ObjectRBTreeMap())
	protected val signMap: MutableMap<BlockKey, SignData> = Long2ObjectSortedMaps.synchronize(Long2ObjectRBTreeMap())

	var blockQueue = ConcurrentLinkedQueue<Long>()

	protected open val clipboardNormalizationOffset: Vec3i = getClipboardOffset()
	protected open val target = calculateTarget()

	protected fun loadBlockQueue() {
		val min = clipboard.minimumPoint
		val max = clipboard.maximumPoint

		for (y in min.y()..max.y()) for (x in min.x()..max.x()) for (z in min.z()..max.z()) {
			val vec3i = Vec3i(x, y, z)

			val baseBlock = clipboard.getFullBlock(x, y, z)
			val data = baseBlock.toImmutableState().toBukkitBlockData()

			if (data.material.isAir) continue

			val worldKey = toBlockKey(toWorldCoordinates(vec3i))

			blockMap[worldKey] = data

			if (data.material.isSign) {
				signMap[worldKey] = SignUtils.readSignData(baseBlock.nbt)
			}

			blockQueue.add(worldKey)
		}
	}

	protected fun toWorldCoordinates(pos: Vec3i): Vec3i {
		if (settings.rotation == 0) return pos + clipboardNormalizationOffset + target

		val regionCenter = Vec3i(
			clipboard.region.center.x().toInt(),
			clipboard.region.center.y().toInt(),
			clipboard.region.center.z().toInt()
		)
		val localized = pos - regionCenter

		val cosTheta: Double = cos(Math.toRadians(settings.rotation.toDouble()))
		val sinTheta: Double = sin(Math.toRadians(settings.rotation.toDouble()))

		val rotatedVector =  Vec3i(
			(localized.x.toDouble() * cosTheta - localized.z.toDouble() * sinTheta).roundToInt(),
			localized.y,
			(localized.x.toDouble() * sinTheta + localized.z.toDouble() * cosTheta).roundToInt()
		)

		return rotatedVector + regionCenter + clipboardNormalizationOffset + target
	}

	protected fun calculateTarget(): Vec3i {
		return entity.getPosRelative(0, 0, 4) + Vec3i(settings.offsetX, settings.offsetY, settings.offsetZ)
	}

	protected fun getClipboardOffset(): Vec3i {
		val structureDirection = entity.structureDirection
		val rightDirection = structureDirection.rightFace

		val negativeX = if (structureDirection.modX == 0) rightDirection.modX < 0 else structureDirection.modX < 0
		val negativeZ = if (structureDirection.modZ == 0) rightDirection.modZ < 0 else structureDirection.modZ < 0

		val x = if (negativeX) clipboard.region.minimumPoint.x() else clipboard.region.maximumPoint.x()
		val y = clipboard.region.minimumPoint.y()
		val z = if (negativeZ) clipboard.region.minimumPoint.z() else clipboard.region.maximumPoint.z()

		val clipboardOffsetX = (x - clipboard.region.center.x() * 2).roundToInt()
		val clipboardOffsetY = (-y.toDouble()).roundToInt()
		val clipboardOffsetZ = (z - clipboard.region.center.z() * 2).roundToInt()

		return Vec3i(clipboardOffsetX, clipboardOffsetY, clipboardOffsetZ)
	}

	private fun getNMSRotation(): Rotation {
		return when (settings.rotation) {
			-180 -> Rotation.CLOCKWISE_180
			-90 -> Rotation.COUNTERCLOCKWISE_90
			0 -> Rotation.NONE
			+90 -> Rotation.CLOCKWISE_90
			+180 -> Rotation.CLOCKWISE_180
			else -> error("Unsupported rotation angle! ${settings.rotation}")
		}
	}

	protected fun getRotatedBlockData(data: BlockData): BlockState {
		val nms = data.nms
		val customBlock = CustomBlocks.getByBlockState(nms)
		if (customBlock != null) {
			return CustomBlocks.getRotated(customBlock, nms, getNMSRotation())
		}

		val rotation = getNMSRotation()
		return nms.rotate(rotation)
	}
}
