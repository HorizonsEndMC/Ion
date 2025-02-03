package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.misc.AdvancedShipFactoryMultiblock.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
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
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext

object AdvancedShipFactoryMultiblock : Multiblock(), EntityMultiblock<AdvancedShipFactoryEntity>, DisplayNameMultilblock, InteractableMultiblock {
	override val name: String = "shipfactory"

	override val signText = createSignText(
		line1 = text("Starship Factory", NamedTextColor.BLUE),
		line2 = text("Advanced", NamedTextColor.YELLOW),
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Advanced Ship Factory")
	override val description: Component get() = text("Print starships and other structures with materials and credits. Provides more configuration settings.")

	override fun MultiblockShape.buildStructure() {
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
		z(1) {
			y(-1) {
				x(2).anyGlass()
				x(1).anyGlass()
				x(0).sponge()
				x(-1).anyGlass()
				x(-2).anyGlass()
			}
			y(0) {
				x(2).anyPipedInventory()
				x(1).sponge()
				x(0).type(Material.LODESTONE)
				x(-1).sponge()
				x(-2).anyPipedInventory()
			}
		}
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
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = getMultiblockEntity(sign) ?: return
		entity.openMenu(player)
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
		return AdvancedShipFactoryEntity(data, manager, x, y, z, world, structureDirection)
	}

	class AdvancedShipFactoryEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, AdvancedShipFactoryMultiblock, world, x, y, z, structureDirection), StatusTickedMultiblockEntity, SyncTickingMultiblockEntity, PoweredMultiblockEntity, UserManagedMultiblockEntity, DisplayMultiblockEntity {
		override val multiblock: AdvancedShipFactoryMultiblock = AdvancedShipFactoryMultiblock

		override val maxPower: Int = 300_000
		override val powerStorage: PowerStorage = loadStoredPower(data)

		override val userManager: UserManagedMultiblockEntity.UserManager = UserManagedMultiblockEntity.UserManager(data, true)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(5)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this) },
			{ StatusDisplayModule(it, statusManager) }
		).register()

		override val inputsData: InputsData = InputsData.Builder(this)
			.addPowerInput(0, -1, 0)
			.build()

		fun openMenu(player: Player) {
			if (userManager.currentlyUsed()) {
				userManager.clear()
				return
			}

			userManager.setUser(player)
		}

		override fun tick() {
			if (!userManager.currentlyUsed()) return
			println("Toggled on")
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			userManager.saveUserData(store)
			savePowerData(store)
		}

		private val pipeInputOffsets = arrayOf(
			Vec3i(0, 0, 0),
			Vec3i(0, 0, 2),
			Vec3i(-2, -1, 1),
			Vec3i(+2, -1, 1),
		)

		fun getNetworkedExtractors(): Map<BlockKey, Collection<BlockKey>> {
			val transportManager = manager.getTransportManager()
			val itemCacheHolder = transportManager.itemPipeManager

			val localPipeInputKeys = pipeInputOffsets.map { i -> toBlockKey(getPosRelative(i.x, i.y, i.z)) }

			val allDestinations = localPipeInputKeys.associateWith { inputLoc ->
				val node = itemCacheHolder.nodeProvider.invoke(CacheType.ITEMS, world, inputLoc) ?: return@associateWith listOf()

				itemCacheHolder.cache.getNetworkDestinations(
					clazz = ItemNode.ItemExtractorNode::class,
					originPos = inputLoc,
					originNode = node
				) {
					getPreviousNodes(itemCacheHolder.nodeProvider, null)
				}
			}

			return allDestinations
		}

		fun canRemoveFromDestination(destination: BlockKey, sourceLoc: BlockKey, stack: ItemStack): Boolean {
			val localCacheHolder = manager.getTransportManager().itemPipeManager.getCacheHolderAt(destination) ?: return false
			val destinationInventory = localCacheHolder.cache.getInventory(destination) ?: return false

			if (!destinationInventory.containsAtLeast(stack, stack.amount)) return false
			val singletonItem = stack.asOne()

			val path = localCacheHolder.cache.findPath(
				origin = NodePositionData(
					ItemNode.ItemExtractorNode,
					world,
					destination,
					BlockFace.SELF
				),
				destination = sourceLoc,
				itemStack = singletonItem,
			) { node, blockFace ->
				if (node !is ItemNode.FilterNode) return@findPath true

				node.matches(singletonItem)
			}

			return path != null
		}
	}
}
