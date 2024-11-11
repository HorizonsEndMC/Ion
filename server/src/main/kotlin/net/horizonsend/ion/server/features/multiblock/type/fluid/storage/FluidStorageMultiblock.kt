package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SplitFluidDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.BasicFluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.UnlimitedInternalStorage
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class FluidStorageMultiblock(val capacity: Int) : Multiblock(), EntityMultiblock<FluidStorageMultiblock.FluidStorageMultiblockEntity> {
	override val name: String = "tank"
	override val alternativeDetectionNames: Array<String> = arrayOf("gastank", "fluidtank")

	override val signText: Array<Component?> = arrayOf(
		ofChildren(text("Fluid ", NamedTextColor.RED), text("Tank", NamedTextColor.GOLD)),
		null,
		null,
		null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): FluidStorageMultiblockEntity {
		return FluidStorageMultiblockEntity(manager, this, data, x, y, z, world, structureDirection)
	}

	class FluidStorageMultiblockEntity(
		manager: MultiblockManager,
		override val multiblock: FluidStorageMultiblock,
		data: PersistentMultiblockData,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : BasicFluidStoringEntity(manager, multiblock, data, x, y, z, world, structureDirection, UnlimitedInternalStorage(multiblock.capacity)), FluidStoringEntity, DisplayMultiblockEntity {
		override val fluidInputOffset: Vec3i = Vec3i(0, -1, 0)
		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			SplitFluidDisplay(mainStorage, +0.0, -0.0, +0.0, 0.45f),
		).register()

		override val fluidInputOffsets: Array<Vec3i> = arrayOf(Vec3i(0, -1, 0))

		override fun onLoad() {
			registerInputs(NetworkType.FLUID, getFluidInputLocations())
		}

		override fun onUnload() {
			releaseInputs(NetworkType.FLUID, getFluidInputLocations())
		}

		override fun handleRemoval() {
			releaseInputs(NetworkType.FLUID, getFluidInputLocations())
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			storeFluidData(store, adapterContext)
		}
	}
}
