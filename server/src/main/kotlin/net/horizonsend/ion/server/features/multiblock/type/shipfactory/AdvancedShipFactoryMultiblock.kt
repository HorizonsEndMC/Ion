package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.common.utils.text.ADVANCED_SHIP_FACTORY_CHARACTER
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryMultiblock.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.getGlobalNode
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
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs.Shape.STRAIGHT
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

object AdvancedShipFactoryMultiblock : AbstractShipFactoryMultiblock<AdvancedShipFactoryEntity>() {
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

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		AdvancedShipFactoryGui(player, sign.block).open()
	}

	class AdvancedShipFactoryEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : ShipFactoryEntity(data, AdvancedShipFactoryMultiblock, manager, world, x, y, z, structureDirection), PoweredMultiblockEntity {
		override val maxPower: Int = 300_000
		override val powerStorage: PowerStorage = loadStoredPower(data)

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this) },
			{ StatusDisplayModule(it, statusManager) }
		).register()

		override val inputsData: InputsData = InputsData.Builder(this)
			.addPowerInput(0, -1, 0)
			.build()

		override fun tick() {
			if (!userManager.currentlyUsed()) return
			println("Toggled on")
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			userManager.saveUserData(store)
			savePowerData(store)
			settings.save(store)
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
					getPreviousNodes({ cacheType, world, key -> getGlobalNode(cacheType, world, key) }, null)
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

	class AdvancedShipFactoryGui(val viewer: Player, val block: Block) : GuiWrapper {
		override fun open() {
			val gui = Gui.normal()
				.setStructure(
					". . . . . i . . .",
					"^ ^ ^ . ^ ^ . . .",
					". . . . . . . . .",
					"v v v . v v . . .",
					". . . . . . . . .",
					". . . . . . . . ."
				)
				.addIngredient('^', GuiItems.CustomControlItem(text("up"), GuiItem.UP))
				.addIngredient('v', GuiItems.CustomControlItem(text("down"), GuiItem.DOWN))
				.addIngredient('i', GuiItems.CustomControlItem(text("down"), GuiItem.MAGNIFYING_GLASS))

			Window.single()
				.setGui(gui)
				.setTitle(AdventureComponentWrapper(setGuiOverlay()))
				.build(viewer)
				.open()
		}

		fun setGuiOverlay(): Component = GuiText("Advanced Goon Factory")
			.addBackground(GuiText.GuiBackground(
				backgroundChar = ADVANCED_SHIP_FACTORY_CHARACTER,
				backgroundWidth = 250 - 9,
				verticalShift = 10
			))
			.build()
	}
}
