package net.horizonsend.ion.server.features.world.generation.feature.meta.wreck

import net.horizonsend.ion.server.core.registration.keys.WreckStructureKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.block.data.BlockData

object EmptyWreckStructure : WreckStructure(WreckStructureKeys.EMPTY) {
	override fun getExtents(metaData: WreckMetaData): Pair<Vec3i, Vec3i> {
		return Pair(Vec3i(0, 0, 0), Vec3i(0, 0, 0))
	}

	private val AIR = Material.AIR.createBlockData()

	override fun getBlockData(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, metaData: WreckMetaData, ): BlockData = AIR
	override fun getNBTData(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, realX: Int, realY: Int, realZ: Int, metaData: WreckMetaData, ): CompoundTag? = null
	override fun isInBounds(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, metaData: WreckMetaData, ): Boolean = false
}
