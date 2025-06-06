package net.horizonsend.ion.server.features.multiblock.type.economy

import com.manya.pdc.base.MapDataType
import com.manya.pdc.base.UuidDataType
import com.manya.pdc.base.array.StringArrayDataType
import com.manya.util.MapCollectors
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.economy.city.CityNPCs.BAZAAR_CITY_TERRITORIES
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.economy.RemotePipeMultiblock.InventoryReference
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryParent.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.transport.items.util.addToInventory
import net.horizonsend.ion.server.features.transport.items.util.getTransferSpaceFor
import net.horizonsend.ion.server.gui.invui.bazaar.terminal.BazaarTerminalMainMenu
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.persistence.SettingsContainer
import net.horizonsend.ion.server.miscellaneous.utils.persistence.SettingsContainer.SettingsProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.BOOLEAN
import org.bukkit.persistence.PersistentDataType.DOUBLE
import org.bukkit.util.Vector
import java.nio.charset.Charset
import java.util.UUID

sealed class BazaarTerminalMultiblock : Multiblock(), EntityMultiblock<BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity>, InteractableMultiblock {
	override val name: String = "bazaarterminal"

	override val signText: Array<Component?> = createSignText(
		ofChildren(text("Bazaar", DARK_AQUA), text(" Terminal", GRAY)),
		text("Ecomanage, LLC", AQUA),
		null,
		null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): BazaarTerminalMultiblockEntity {
		return BazaarTerminalMultiblockEntity(data, manager, this, world, x, y, z, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign, false)?.handleInteract(player)
	}

	abstract val depositInventoryOffsets: Array<Vec3i>
	abstract val depositPipeOrigin: Array<Vec3i>
	abstract val withdrawInventoryOffsets: Array<Vec3i>
	abstract val withdrawPipeOrigin: Array<Vec3i>

	data object BazaarTerminalMultiblockStandard : BazaarTerminalMultiblock() {
		override val depositInventoryOffsets: Array<Vec3i> = arrayOf(Vec3i(2, 0, 2))
		override val depositPipeOrigin: Array<Vec3i> = arrayOf(Vec3i(2, -1, 2))
		override val withdrawInventoryOffsets: Array<Vec3i> = arrayOf(Vec3i(-2, 0, 2))
		override val withdrawPipeOrigin: Array<Vec3i> = arrayOf(Vec3i(-2, -1, 2))

		override fun MultiblockShape.buildStructure() {
			z(1) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(2) {
				y(-1) {
					x(2).anyGlass()
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).extractor()
				}
				y(0) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).anyPipedInventory()
				}
			}
			z(3) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(0) {
				y(-1) {
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(0).powerInput()
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
					x(0).anyGlass()
					x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				}
			}
		}
	}

	data object BazaarTerminalMultiblockMergeableLeft : BazaarTerminalMultiblock() {
		override val depositInventoryOffsets: Array<Vec3i> = arrayOf(Vec3i(2, 0, 2))
		override val depositPipeOrigin: Array<Vec3i> = arrayOf(Vec3i(2, -1, 2))
		override val withdrawInventoryOffsets: Array<Vec3i> = arrayOf()
		override val withdrawPipeOrigin: Array<Vec3i> = arrayOf()

		override fun MultiblockShape.buildStructure() {
			z(1) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).netheriteCasing()
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).netheriteCasing()
				}
			}
			z(2) {
				y(-1) {
					x(2).anyGlass()
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
				y(0) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
			}
			z(3) {
				y(-1) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).netheriteCasing()
				}
				y(0) {
					x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).netheriteCasing()
				}
			}
			z(0) {
				y(-1) {
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(0).powerInput()
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
					x(0).anyGlass()
					x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				}
			}
		}
	}

	data object BazaarTerminalMultiblockMergeableRight : BazaarTerminalMultiblock() {
		override val depositInventoryOffsets: Array<Vec3i> = arrayOf()
		override val depositPipeOrigin: Array<Vec3i> = arrayOf()
		override val withdrawInventoryOffsets: Array<Vec3i> = arrayOf(Vec3i(-2, 0, 2))
		override val withdrawPipeOrigin: Array<Vec3i> = arrayOf(Vec3i(-2, -1, 2))

		override fun MultiblockShape.buildStructure() {
			z(1) {
				y(-1) {
					x(2).netheriteCasing()
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).netheriteCasing()
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(2) {
				y(-1) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).extractor()
				}
				y(0) {
					x(2).type(Material.LODESTONE)
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).anyPipedInventory()
				}
			}
			z(3) {
				y(-1) {
					x(2).netheriteCasing()
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(2).netheriteCasing()
					x(1).steelBlock()
					x(0).ironBlock()
					x(-1).steelBlock()
					x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				}
			}
			z(0) {
				y(-1) {
					x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
					x(0).powerInput()
					x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				}
				y(0) {
					x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
					x(0).anyGlass()
					x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				}
			}
		}
	}

	override fun setupSign(player: Player, sign: Sign) {
		super.setupSign(player, sign)

		Tasks.syncDelay(2L) {
			val entity = getMultiblockEntity(sign) ?: return@syncDelay
			if (entity.owner == null) {
				entity.owner = player.uniqueId
				player.information("You claimed ownership of this multiblock!")
			}
		}
	}

	class BazaarTerminalMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: BazaarTerminalMultiblock,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, 100_000), RemotePipeMultiblock {
		val territory: RegionTerritory? get() = Regions.findFirstOf(location)

		companion object {
			val uuidSerializer = UuidDataType()
			val stringArraySerializer = StringArrayDataType(Charset.defaultCharset())
			val materialMapSerializer: MapDataType<Map<String, Double>, Map<String, Double>, String, Double> = MapDataType(MapCollectors.toMap { mutableMapOf() }, PersistentDataType.STRING, DOUBLE)
		}

		val settings = SettingsContainer.multiblockSettings(data,
			SettingsProperty(BazaarTerminalMultiblockEntity::owner, uuidSerializer, null),
			SettingsProperty(BazaarTerminalMultiblockEntity::enableShipFactoryIntegration, BOOLEAN, true),
			SettingsProperty(BazaarTerminalMultiblockEntity::shipFactoryAllowRemote, BOOLEAN, false),
			SettingsProperty(BazaarTerminalMultiblockEntity::shipFactoryMaxUnitPrice, materialMapSerializer, mapOf()),
			SettingsProperty(BazaarTerminalMultiblockEntity::shipFactoryPriceCap, DOUBLE, 10_000_000.0),
			SettingsProperty(BazaarTerminalMultiblockEntity::shipFactoryItemRestriction, stringArraySerializer, arrayOf()),
			SettingsProperty(BazaarTerminalMultiblockEntity::shipFactoryWhitelistMode, BOOLEAN, true),
		)

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			settings.save(store.getAdditionalDataRaw(), adapterContext)
			super.storeAdditionalData(store, adapterContext)
		}

		var owner: UUID? by settings.getDelegate()

		var enableShipFactoryIntegration: Boolean by settings.getDelegate()

		var shipFactoryAllowRemote: Boolean by settings.getDelegate()
		var shipFactoryMaxUnitPrice: Map<String, Double> by settings.getDelegate()
		var shipFactoryPriceCap: Double by settings.getDelegate()

		var shipFactoryItemRestriction: Array<String> by settings.getDelegate()
		var shipFactoryWhitelistMode: Boolean by settings.getDelegate()

		override val displayHandler: TextDisplayHandler = standardPowerDisplay(this)

		fun isWithdrawAvailable(): Boolean {
			return multiblock == BazaarTerminalMultiblockStandard
		}

		fun isDepositAvailable(): Boolean {
			if (multiblock is BazaarTerminalMultiblockMergeableRight) return false

			var depositAvailable = true

			val region = Regions.findFirstOf<RegionTerritory>(location)
			if (region == null) depositAvailable = false

			if (region != null) {
				if (!TradeCities.isCity(region)) depositAvailable = false

				if (!BAZAAR_CITY_TERRITORIES.contains(region.id)) depositAvailable = false
			}

			return depositAvailable
		}

		val mergeEnd = createLinkage(
			offsetRight = if (multiblock is BazaarTerminalMultiblockMergeableRight) 2 else -2,
			offsetUp = -1,
			offsetForward = 2,
			linkageDirection = if (multiblock is BazaarTerminalMultiblockMergeableRight) RelativeFace.RIGHT else RelativeFace.LEFT,
			predicate = { multiblock !is BazaarTerminalMultiblockStandard },
			AdvancedShipFactoryEntity::class
		)

		fun handleInteract(player: Player) {
			if (owner == null) {
				owner = player.uniqueId
				player.information("You claimed ownership of this multiblock!")
			}

			for (reference in getOutputInventories()) {
				val location = reference.inventory.location ?: continue

				val block = Material.GREEN_CONCRETE.createBlockData()
				sendEntityPacket(player, displayBlock(world.minecraft, block, Vector(location.x, location.y, location.z), 1.5f, true), 10 * 20L)
			}

			for (reference in getInputInventories()) {
				val location = reference.inventory.location ?: continue

				val block = Material.RED_CONCRETE.createBlockData()
				sendEntityPacket(player, displayBlock(world.minecraft, block, Vector(location.x, location.y, location.z), 1.5f, true), 10 * 20L)
			}

			BazaarTerminalMainMenu(player, this).openGui()
		}

		fun getInputInventories(): Set<InventoryReference> {
			val base = multiblock.depositInventoryOffsets.mapNotNullTo(mutableSetOf(), ::getStandardReference)
			return getRemoteReferences(getNetworkedExtractors(multiblock.depositPipeOrigin), manager.getTransportManager().itemPipeManager.cache).plus(base)
		}

		fun getOutputInventories(): Set<InventoryReference> {
			val withdrawInventoryReferences: MutableSet<InventoryReference> = multiblock.withdrawInventoryOffsets.mapNotNullTo(mutableSetOf()) { getStandardReference(it) }
			withdrawInventoryReferences.addAll(getNetworkedInventories(multiblock.withdrawPipeOrigin).values.firstOrNull() ?: setOf())

			return withdrawInventoryReferences
		}

		fun intakeItems(itemStack: ItemStack, amount: Int, cost: Double, priceMult: Int): () -> InputResult {
			// This part is run async
			val destinations = getOutputInventories()
				.filter { getTransferSpaceFor(it.inventory, itemStack) > 0 }
				.sortedBy { (it as? InventoryReference.RemoteInventoryReference)?.path?.length ?: 0 }
				.toMutableList()

			return syncBlock@{
				val maxStackSize = itemStack.maxStackSize

				val fullStacks = amount / maxStackSize
				val remainder = amount % maxStackSize

				var remaining = amount
				var destinationsRemaining = destinations.size

				var inventories = 0

				while (remaining > 0 && destinationsRemaining >= 1) {
					destinationsRemaining--
					val destination = destinations.first()
					val notAdded = addToInventory(destination.inventory, itemStack.asQuantity(remaining))
					inventories++

					remaining -= (remaining - notAdded)

					if (notAdded == 0) {
						break
					}

					destinations.remove(destination)
				}

				var droppedStacks = 0
				var droppedItems = 0

				if (remaining > 0) {
					val remainingStacks = remaining / maxStackSize
					val remainingStacksremainder = remaining % maxStackSize

					val dropLocation = location.clone().add(structureDirection.oppositeFace.direction).toCenterLocation()

					repeat(remainingStacks) {
						world.dropItem(dropLocation, itemStack.asQuantity(maxStackSize))
						droppedStacks += maxStackSize
					}

					droppedItems += remainingStacksremainder
					world.dropItem(dropLocation, itemStack.asQuantity(remainingStacksremainder))
				}

				val quantityMessage = if (itemStack.maxStackSize == 1) "{0}" else "{0} stack${if (fullStacks == 1) "" else "s"} and {1} item${if (remainder == 1) "" else "s"}"

				val fullMessage = template(
					text("Bought $quantityMessage of {2} for {3}.", GREEN),
					fullStacks,
					remainder,
					itemStack.displayNameComponent,
					cost.toCreditComponent()
				)

				val lore = mutableListOf(fullMessage)

				if (droppedItems > 0 || droppedStacks > 0) {
					val droppedItemsMessage = template(
						text("${if (itemStack.maxStackSize == 1) "{0}" else "{0} stack${if (fullStacks == 1) "" else "s"} and {1} item${if (remainder == 1) "" else "s"}"} {2} was dropped due to insufficent storage space.", RED),
						droppedStacks,
						droppedItems,
						itemStack.displayNameComponent
					)
					lore.add(droppedItemsMessage)
				}

				if (priceMult > 1) {
					val priceMultiplicationMessage = template(text("(Price multiplied by {0} due to browsing remotely)", YELLOW), priceMult)
					lore.add(priceMultiplicationMessage)
				}

				InputResult.SuccessReason(lore)

				return@syncBlock InputResult.SuccessReason(lore)
			}
		}
	}
}
