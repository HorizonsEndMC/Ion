package net.horizonsend.ion.server.features.multiblock.type.gridpower.turbine

import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidPortMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.RotationConsumer
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.RotationProvider
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gridpower.turbine.TurbineMultiblock.TurbineMultiblockEntity
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.block.BlockFace

abstract class TurbineMultiblock : Multiblock(), EntityMultiblock<TurbineMultiblockEntity> {
	override val name: String = "turbine"

	override val signText: Array<Component?> = createSignText(
		Component.text("Turbine"),
		null,
		null,
		null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): TurbineMultiblockEntity {
		return TurbineMultiblockEntity(manager, data, this, world, x, y, z, structureDirection)
	}

	class TurbineMultiblockEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		multiblock: TurbineMultiblock,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, multiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, AsyncTickingMultiblockEntity, StatusMultiblockEntity, RotationProvider, FluidStoringMultiblock {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ StatusDisplayModule(it, statusManager) },
		)

		override val ioData: IOData = IOData.Companion.builder(this)
			// Output
			.addPort(IOType.FLUID, 3, 0, 1) { IOPort.RegisteredMetaDataInput<FluidPortMetadata>(this, FluidPortMetadata(connectedStore = steamInput, inputAllowed = true, outputAllowed = false)) }
			.addPort(IOType.FLUID, -3, 0, 1) { IOPort.RegisteredMetaDataInput<FluidPortMetadata>(this, FluidPortMetadata(connectedStore = steamOutput, inputAllowed = false, outputAllowed = true)) }
			.build()

		val steamInput = FluidStorageContainer(data, "steam_input", Component.text("Steam Input"), STEAM_INPUT, 1_000_000.0, FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.WATER)))
		val steamOutput = FluidStorageContainer(data, "steam_output", Component.text("Steam Output"), STEAM_OUTPUT, 1_000_000.0, FluidRestriction.FluidTypeWhitelist(setOf(FluidTypeKeys.WATER)))

		override fun getStores(): List<FluidStorageContainer> = arrayListOf(steamInput, steamOutput)

		companion object {
			val STEAM_INPUT = NamespacedKeys.key("steam_input")
			val STEAM_OUTPUT = NamespacedKeys.key("steam_output")
		}

		val rotationLinkage = createLinkage(0, 0, 1, RelativeFace.FORWARD) { it is RotationConsumer }

		override fun tickAsync() {
			bootstrapFluidNetwork()

			tickSteam()
		}

		fun tickSteam() {
			if (steamInput.getContents().isEmpty()) return
		}
	}
}
