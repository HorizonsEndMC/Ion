package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SplitFluidDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.BasicFluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.UnlimitedInternalStorage
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
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
	) : BasicFluidStoringEntity(manager, multiblock, data, x, y, z, world, structureDirection, UnlimitedInternalStorage(multiblock.capacity)), FluidStoringEntity {
		override val fluidInputOffset: Vec3i = Vec3i(0, -1, 0)
		private val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			SplitFluidDisplay(mainStorage, +0.0, -0.0, +0.0, 0.45f),
		).register()

		override fun onLoad() {
			displayHandler.update()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData) {
			val rawStorage = store.getAdditionalDataRaw()
			storeFluidData(rawStorage, rawStorage.adapterContext)
		}
	}
}
