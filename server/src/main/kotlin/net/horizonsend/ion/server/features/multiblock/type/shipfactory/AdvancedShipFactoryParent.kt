package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryParent.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.TransportTask
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.BACKWARD
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace.FORWARD
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
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
		line1 = text("Starship Factory", NamedTextColor.BLUE),
		line2 = text("Advanced", NamedTextColor.YELLOW),
		line3 = null,
		line4 = null
	)

	override val blockPlacementsPerTick: Int = 100

	override val displayName: Component get() = text("Advanced Ship Factory")
	override val description: Component get() = text("Print starships and other structures with materials and credits. Provides more configuration settings.")

	data object AdvancedShipFactoryMultiblock : AdvancedShipFactoryParent() {
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

	data object AdvancedShipFactoryMergeable : AdvancedShipFactoryParent() {
		override fun MultiblockShape.buildStructure() {
			z(2) {
				y(-1) {
					x(2).netheriteCasing()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).netheriteCasing()
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
					x(2).netheriteCasing()
					x(1).ironBlock()
					x(0).powerInput()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).netheriteCasing()
					x(1).ironBlock()
					x(0).anyGlass()
					x(-1).ironBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
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
		multiblock: AdvancedShipFactoryParent,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : ShipFactoryEntity(data, multiblock, manager, world, x, y, z, structureDirection), PoweredMultiblockEntity {
		override val maxPower: Int = 300_000
		override val powerStorage: PowerStorage = loadStoredPower(data)

		val mergeEnd = createLinkage(
			offsetRight = 2,
			offsetUp = -1,
			offsetForward = 1,
			linkageDirection = RelativeFace.RIGHT,
			predicate = { multiblock is AdvancedShipFactoryMergeable },
			BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity::class
		)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this) },
			{ StatusDisplayModule(it, statusManager) }
		).register()

		override val inputsData: InputsData = InputsData.Builder(this)
			.addPowerInput(0, -1, 0)
			.registerSignInputs()
			.build()

		override val guiTitle: String = "Advanced Ship Factory"

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			super.storeAdditionalData(store, adapterContext)
			savePowerData(store)
		}

		private val pipeInputOffsets = arrayOf(
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
			val extractors =  getNetworkedExtractors()

			val transportManager = manager.getTransportManager()
			val itemCache = transportManager.itemPipeManager.cache

			val base = inventoryOffsets.mapNotNullTo(mutableSetOf()) {
				val inv = itemCache.getInventory(toBlockKey(getPosRelative(right = it.x, up = it.y, forward = it.z))) ?: return@mapNotNullTo null
				InventoryReference.StandardInventoryReference(inv)
			}

			return extractors.flatMapTo(mutableSetOf()) { (_, destinations) ->

				destinations.flatMap { pathResult: PathfindResult ->
					itemCache.getSources(pathResult.destinationPosition).map {
						InventoryReference.RemoteInventoryReference(it, itemCache.holder, pathResult.trackedPath, this)
					}
				}
			}.plus(base)
		}

		private fun getNetworkedExtractors(): Map<BlockKey, Array<PathfindResult>> {
			if (!settings.grabFromNetworkedPipes) return mapOf()

			val transportManager = manager.getTransportManager()
			val itemCacheHolder = transportManager.itemPipeManager

			val localPipeInputKeys = pipeInputOffsets.map { i -> toBlockKey(getPosRelative(i.x, i.y, i.z)) }

			val allDestinations = localPipeInputKeys.associateWith { inputLoc ->
				val cacheResult = itemCacheHolder.globalNodeCacher.invoke(itemCacheHolder.cache, world, inputLoc) ?: return@associateWith arrayOf()
				val node = cacheResult.second ?: return@associateWith arrayOf()

				val task = TransportTask(inputLoc, world, {}, 1000, IonServer.slF4JLogger)

				itemCacheHolder.cache.getNetworkDestinations(
					task = task,
					destinationTypeClass = ItemNode.ItemExtractorNode::class,
					originPos = inputLoc,
					originNode = node,
					retainFullPath = true,
					nextNodeProvider = { getPreviousNodes(itemCacheHolder.globalNodeCacher, null) }
				)
			}

			return allDestinations
		}
	}
}
