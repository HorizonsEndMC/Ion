package net.horizonsend.ion.server.features.multiblock.type.fluid.storage

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureTypeKeys
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.MATCH_SIGN_FONT_SIZE
import net.horizonsend.ion.server.features.client.display.modular.display.StaticTextDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.fluid.SplitFluidDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.getLinePos
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidInputMetadata
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.space.signatures.SignatureManager
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext

object GasHarvesterMultiblock : Multiblock(), EntityMultiblock<GasHarvesterMultiblock.GasHarvesterMultiblockEntity>, InteractableMultiblock {
	override val name: String = "gasharvester"
	override val signText: Array<Component?> = createSignText(
		ofChildren(Component.text("Gas", NamedTextColor.GOLD), Component.text(" Harvester", HEColorScheme.Companion.HE_MEDIUM_GRAY)),
		null,
		null,
		null
	)

	override val alternativeDetectionNames: Array<String> = arrayOf("harvester", "gasharvester")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).anyWall()
				x(0).fluidInput()
				x(1).anyWall()
			}
			y(0) {
				x(0).anyTerracotta()
			}
			y(1) {
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(1) {
			y(-1) {
				x(-1).anyCopperGrate()
				x(0).anyCopperGrate()
				x(1).anyCopperGrate()
			}
			y(0) {
				x(-1).anyWall()
				x(0).anyCopperGrate()
				x(1).anyWall()
			}
			y(1) {
				x(0).anyCopperBulb()
			}
			y(2) {
				x(0).lightningRod(PrepackagedPreset.simpleDirectional(RelativeFace.UP))
			}
		}
		z(2) {
			y(-1) {
				x(-1).anyWall()
				x(0).anyTerracotta()
				x(1).anyWall()
			}
			y(0) {
				x(0).anyTerracotta()
			}
			y(1) {
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): GasHarvesterMultiblockEntity {
		return GasHarvesterMultiblockEntity(data, manager, world, x, y, z, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign, false)?.getStores()?.first()?.let { container -> player.information(container.getContents().toString()) }
	}

	class GasHarvesterMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, GasHarvesterMultiblock, world, x, y, z, structureDirection), DisplayMultiblockEntity, FluidStoringMultiblock, AsyncTickingMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)

		override val ioData: IOData = IOData.Companion.builder(this)
			.addPort(IOType.FLUID, 0, -1, 0) { IOPort.RegisteredMetaDataInput<FluidInputMetadata>(this, FluidInputMetadata(connectedStore = mainStorage, inputAllowed = false, outputAllowed = true)) }
			.build()

		val mainStorage = FluidStorageContainer(data, "main_storage", Component.text("Main Storage"), NamespacedKeys.MAIN_STORAGE, 5_000.0, FluidRestriction.Unlimited)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ SplitFluidDisplayModule(handler = it, storage = mainStorage, offsetLeft = 0.0, offsetUp = getLinePos(4), offsetBack = 0.0, scale = MATCH_SIGN_FONT_SIZE) },
			{ StaticTextDisplayModule(handler = it, text = Component.text("Output"), offsetLeft = -1.0, offsetUp = 0.15, offsetBack = 0.0, scale = 0.7f) }
		)

		override fun getStores(): List<FluidStorageContainer> {
			return listOf(mainStorage)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			saveStorageData(store)
		}

		override fun tickAsync() {
			bootstrapNetwork()

			if (!SignatureManager.isWithinSignatureRadius(location, SignatureTypeKeys.GAS_CLOUD)) {
				return
			}

			val harvested = FluidStack(FluidTypeKeys.HYDROGEN, 50.0)
			mainStorage.addFluid(harvested, location)
		}
	}
}
