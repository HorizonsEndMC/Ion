package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.multiblock.type.economy.RemotePipeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.economy.RemotePipeMultiblock.InventoryReference
import net.horizonsend.ion.server.features.multiblock.type.processing.automason.AutoMasonMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryParent.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.factory.ShipFactoryPrintTask
import net.horizonsend.ion.server.features.starship.factory.integration.AutoMasonIntegration
import net.horizonsend.ion.server.features.starship.factory.integration.BazaarTerminalIntegration
import net.horizonsend.ion.server.features.starship.factory.integration.ShipFactoryIntegration
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.BACKWARD
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.FORWARD
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext

sealed class AdvancedShipFactoryParent : AbstractShipFactoryMultiblock<AdvancedShipFactoryEntity>() {
	override val signText = createSignText(
		line1 = text("Starship Factory", BLUE),
		line2 = text("Advanced", YELLOW),
		line3 = null,
		line4 = null
	)

	override val blockPlacementsPerTick: Int = 100

	override val displayName: Component get() = ofChildren(
		text("Advanced ", YELLOW), text("Starship Factory", BLUE),

		if (leftMergeAvailable && rightMergeAvailable) text(" Mergable Left and Right")
		else if (leftMergeAvailable) text(" Mergable Left")
		else if (rightMergeAvailable) text(" Mergable Right")
		else empty(),
	)
	override val description: Component get() = text("Print starships and other structures with materials and credits. Provides more configuration settings.")

	abstract val leftMergeAvailable: Boolean
	abstract val rightMergeAvailable: Boolean

	data object AdvancedShipFactoryMultiblock : AdvancedShipFactoryParent() {
		override val rightMergeAvailable: Boolean = false
		override val leftMergeAvailable: Boolean = false

		override fun MultiblockShape.buildStructure() {
			z(0) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.TOP, shape = STRAIGHT))
					x(1).ironBlock()
					x(0).powerInput()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.TOP, shape = STRAIGHT))
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
				}
			}
			z(1) {
				y(-1) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).sponge()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
				y(0) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).anyGlass()
					x(-1).sponge()
					x(-2).anyPipedInventory()
				}
			}
			z(2) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.TOP, shape = STRAIGHT))
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.TOP, shape = STRAIGHT))
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.BOTTOM, shape = STRAIGHT))
				}
			}
		}
	}

	data object AdvancedShipFactoryMergeableRight : AdvancedShipFactoryParent() {
		override val rightMergeAvailable: Boolean = true
		override val leftMergeAvailable: Boolean = false

		override fun MultiblockShape.buildStructure() {
			z(2) {
				y(-1) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(1) {
				y(-1) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).sponge()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
				y(0) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).anyGlass()
					x(-1).sponge()
					x(-2).anyPipedInventory()
				}
			}
			z(0) {
				y(-1) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).powerInput()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
		}
	}

	data object AdvancedShipFactoryMergeableLeft : AdvancedShipFactoryParent() {
		override val rightMergeAvailable: Boolean = false
		override val leftMergeAvailable: Boolean = true

		override fun MultiblockShape.buildStructure() {
			z(0) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(1).ironBlock()
					x(0).powerInput()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
			}
			z(1) {
				y(-1) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).sponge()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
				y(0) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).anyGlass()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
			}
			z(2) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
			}
		}
	}

	data object AdvancedShipFactoryMergeableDouble : AdvancedShipFactoryParent() {
		override val rightMergeAvailable: Boolean = true
		override val leftMergeAvailable: Boolean = true

		override fun MultiblockShape.buildStructure() {
			z(0) {
				y(-1) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).powerInput()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
				y(0) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
			}
			z(1) {
				y(-1) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).sponge()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
				y(0) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).anyGlass()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
			}
			z(2) {
				y(-1) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
				y(0) {
					x(2).netheriteBlock()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).netheriteBlock()
				}
			}
		}
	}

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace,
	): AdvancedShipFactoryEntity {
		return AdvancedShipFactoryEntity(data, this, manager, x, y, z, world, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign)?.openMenu(player)
	}

	class AdvancedShipFactoryEntity(
		data: PersistentMultiblockData,
		override val multiblock: AdvancedShipFactoryParent,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : ShipFactoryEntity(data, multiblock, manager, world, x, y, z, structureDirection), PoweredMultiblockEntity, RemotePipeMultiblock {
		override val maxPower: Int = 300_000
		override val powerStorage: PowerStorage = loadStoredPower(data)

		private val mergeRight = createLinkage(
			offsetRight = 2,
			offsetUp = -1,
			offsetForward = 1,
			linkageDirection = RelativeFace.RIGHT,
			predicate = { multiblock.rightMergeAvailable },
			BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity::class,
			AutoMasonMultiblockEntity::class
		)

		private val mergeLeft = createLinkage(
			offsetRight = -2,
			offsetUp = -1,
			offsetForward = 1,
			linkageDirection = RelativeFace.LEFT,
			predicate = { multiblock.leftMergeAvailable },
			BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity::class,
			AutoMasonMultiblockEntity::class
		)

		override fun startTask(blueprint: Blueprint, gui: ShipFactoryGui?, user: Player) {
			val integrations = getMergedWith().mapNotNull(::getMergeIntegration)

			startTask(ShipFactoryPrintTask(blueprint, settings,this, integrations, gui, getInventories(), user))
		}

		fun getMergeIntegration(multiblockEntity: MultiblockEntity): ShipFactoryIntegration<*>? = when (multiblockEntity) {
			is BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity -> BazaarTerminalIntegration(this, multiblockEntity)
			is AutoMasonMultiblockEntity -> AutoMasonIntegration(this, multiblockEntity)
			else -> null
		}

		fun getMergedWith(): Collection<MultiblockEntity> {
			val integrations = mutableListOf<MultiblockEntity>()

			if (multiblock.leftMergeAvailable) mergeLeft?.get()?.let(integrations::add)
			if (multiblock.rightMergeAvailable) mergeRight?.get()?.let(integrations::add)

			return integrations
		}

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this) },
			{ StatusDisplayModule(it, statusManager) }
		).register()

		override val ioData: IOData = IOData.Builder(this)
			.addPowerInput(0, -1, 0)
			.registerSignInputs()
			.build()

		override val guiTitle: String = "Advanced Ship Factory"

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			super.storeAdditionalData(store, adapterContext)
			savePowerData(store)
		}

		private val pipeSearchPoints: Array<Vec3i> = arrayOf(
			Vec3i(0, 0, 0),
			Vec3i(0, 0, 1),
			Vec3i(0, 0, 2),
			Vec3i(0, -1, 2)
		)

		private val inventoryOffsets = arrayOf(
			Vec3i(-2, 0, 1),
			Vec3i(+2, 0, 1)
		)

		override fun getInventories(): Set<InventoryReference> {
			val base = inventoryOffsets.mapNotNullTo(mutableSetOf(), ::getStandardReference)

			return if (settings.grabFromNetworkedPipes)
				getRemoteReferences(getNetworkedExtractors(pipeSearchPoints), manager.getTransportManager().itemPipeManager.cache).plus(base)
				else base
		}
	}
}
