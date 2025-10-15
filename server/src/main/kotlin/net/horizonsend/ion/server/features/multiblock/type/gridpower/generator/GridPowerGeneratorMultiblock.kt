package net.horizonsend.ion.server.features.multiblock.type.gridpower.generator

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.client.display.modular.display.gridenergy.GridEnergyDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyPortMetaData
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.RotationConsumer
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.RotationProvider
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.gridpower.generator.GridPowerGeneratorMultiblock.GridPowerGeneratorMultiblockEntity
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.block.BlockFace

abstract class GridPowerGeneratorMultiblock : Multiblock(), EntityMultiblock<GridPowerGeneratorMultiblockEntity> {
	override val name: String = "generator"

	abstract val linkageOffset: Vec3i

	override val signText: Array<Component?> = createSignText(
		Component.text("Grid Generator"),
		null,
		null,
		null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): GridPowerGeneratorMultiblockEntity {
		return GridPowerGeneratorMultiblockEntity(manager, this, world, x, y, z, structureDirection)
	}

	class GridPowerGeneratorMultiblockEntity(
		manager: MultiblockManager,
		multiblock: GridPowerGeneratorMultiblock,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, multiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, AsyncTickingMultiblockEntity, GridEnergyMultiblock, StatusMultiblockEntity, RotationConsumer {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)
		override val gridEnergyManager: GridEnergyMultiblock.MultiblockGridEnergyManager = GridEnergyMultiblock.MultiblockGridEnergyManager(this)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val ioData: IOData = IOData.Companion.builder(this)
			// Output
			.addPort(IOType.GRID_ENERGY, 0, -1, 0) { IOPort.RegisteredMetaDataInput<GridEnergyPortMetaData>(this, GridEnergyPortMetaData(inputAllowed = false, outputAllowed = true)) }
			.build()

		override val displayHandler: TextDisplayHandler= DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ GridEnergyDisplay(handler = it, multiblock = this, offsetLeft = 0.0, offsetUp = getLinePos(3), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ StatusDisplayModule(it, statusManager) },
		)

		val rotationLinkage = createLinkage(multiblock.linkageOffset.x, multiblock.linkageOffset.y, multiblock.linkageOffset.z, RelativeFace.FORWARD) {
			it is RotationProvider
		}

		override fun getGridEnergyOutput(): Double {
			val linkage = rotationLinkage.get() ?: return 0.0
			return (linkage as RotationProvider).getRotationInertia()
		}

		override fun getSlowingJoules(): Double {
			return getConnectedNetworks().sumOf { network -> network.lastConsumption }
		}

		override fun tickAsync() {
			bootstrapGridEnergyNetwork()

			tickGeneration()
		}

		fun tickGeneration() {
		}
	}
}
