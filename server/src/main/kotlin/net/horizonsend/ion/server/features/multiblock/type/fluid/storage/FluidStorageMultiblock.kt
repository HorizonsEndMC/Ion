package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.BasicFluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.UnlimitedInternalStorage
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace

abstract class FluidStorageMultiblock(val capacity: Int) : Multiblock(), EntityMultiblock<FluidStorageMultiblock.FluidStorageMultiblockEntity> {
	override val name: String = "tank"
	override val alternativeDetectionNames: Array<String> = arrayOf("gastank", "fluidtank")

	override val signText: Array<Component?> = arrayOf(
		ofChildren(text("Fluid ", NamedTextColor.RED), text("Tank", NamedTextColor.GOLD)),
		null,
		null,
		null
	)

	override fun createEntity(manager: ChunkMultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): FluidStorageMultiblockEntity {
		return FluidStorageMultiblockEntity(manager, this, data, x, y, z, world, structureDirection)
	}

	class FluidStorageMultiblockEntity(
		manager: ChunkMultiblockManager,
		override val multiblock: FluidStorageMultiblock,
		data: PersistentMultiblockData,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : BasicFluidStoringEntity(manager, multiblock, data, x, y, z, world, structureDirection, UnlimitedInternalStorage(multiblock.capacity)), FluidStoringEntity
}
