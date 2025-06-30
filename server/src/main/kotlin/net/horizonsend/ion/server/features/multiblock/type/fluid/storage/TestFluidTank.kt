package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SimpleFluidDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.storage.TestFluidTank.TestFluidTankEntity
import net.horizonsend.ion.server.features.transport.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace

object TestFluidTank : Multiblock(), EntityMultiblock<TestFluidTankEntity> {
	override val name: String = "fluidtank"
	override val signText: Array<Component?> = createSignText(
		Component.text("Fluid Smank"),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(1).anyGlass()
				x(0).fluidInput()
				x(-1).anyGlass()
			}
			y(0) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
		z(1) {
			y(-1) {
				x(1).type(Material.WAXED_COPPER_GRATE)
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(-1).type(Material.WAXED_COPPER_GRATE)
			}
			y(0) {
				x(1).type(Material.WAXED_COPPER_GRATE)
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(-1).type(Material.WAXED_COPPER_GRATE)
			}
			y(1) {
				x(1).type(Material.WAXED_COPPER_GRATE)
				x(0).type(Material.WAXED_COPPER_GRATE)
				x(-1).type(Material.WAXED_COPPER_GRATE)
			}
		}
		z(2) {
			y(-1) {
				x(1).anyGlass()
				x(0).anyGlass()
				x(-1).anyGlass()
			}
			y(0) {
				x(0).anyGlass()
			}
			y(1) {
				x(0).anyGlass()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): TestFluidTankEntity {
		return TestFluidTankEntity(data, manager, world, x, y, z, structureDirection)
	}

	class TestFluidTankEntity(data: PersistentMultiblockData, manager: MultiblockManager, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace) : MultiblockEntity(
		manager, TestFluidTank, world, x, y, z, structureDirection
	), DisplayMultiblockEntity, FluidStoringMultiblock {
		override val inputsData: InputsData = InputsData.builder(this)
			.addPowerInput(0, -1, 0)
			.build()

		val storage = FluidStorageContainer(data, "main", Component.text("Main"), NamespacedKeys.MAIN_STORAGE, 100_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ SimpleFluidDisplayModule(it, storage, 0.0, 0.0, 0.0, 1.0f) }
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(storage)
		}

		override fun saveStorageData(destination: PersistentMultiblockData) {
			saveStorageData(destination)
		}
	}
}
