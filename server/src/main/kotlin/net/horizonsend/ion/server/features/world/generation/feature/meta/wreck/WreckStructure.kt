package net.horizonsend.ion.server.features.world.generation.feature.meta.wreck

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.nbt.CompoundTag
import org.bukkit.block.data.BlockData

abstract class WreckStructure(override val key: IonRegistryKey<WreckStructure, out WreckStructure>) : Keyed<WreckStructure> {
	abstract fun getExtents(metaData: WreckMetaData): Pair<Vec3i, Vec3i>
	abstract fun getBlockData(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, metaData: WreckMetaData): BlockData
	abstract fun getNBTData(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, realX: Int, realY: Int, realZ: Int, metaData: WreckMetaData): CompoundTag?
	abstract fun isInBounds(startOffsetX: Int, startOffsetY: Int, startOffsetZ: Int, metaData: WreckMetaData): Boolean
}
