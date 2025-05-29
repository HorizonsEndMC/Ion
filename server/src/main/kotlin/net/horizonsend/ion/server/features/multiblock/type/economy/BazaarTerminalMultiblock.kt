package net.horizonsend.ion.server.features.multiblock.type.economy

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryParent.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.gui.invui.bazaar.terminal.BazaarTerminalMainMenu
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

sealed class BazaarTerminalMultiblock : Multiblock(), EntityMultiblock<BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity>, InteractableMultiblock {
	override val name: String = "bazaarterminal"

	override val signText: Array<Component?> = createSignText(
		Component.text("Bazaar Terminal"),
		null,
		null,
		null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): BazaarTerminalMultiblockEntity {
		return BazaarTerminalMultiblockEntity(data, manager, this, world, x, y, z, structureDirection)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		getMultiblockEntity(sign, false)?.handleInteract(player)
	}

	data object BazaarTerminalStandardMultiblock : BazaarTerminalMultiblock() {
		override fun MultiblockShape.buildStructure() {
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
			z(2) {
				y(-1) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).anyPipedInventory()
				}
				y(0) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).ironBlock()
					x(-1).sponge()
					x(-2).anyPipedInventory()
				}
			}
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

	data object BazaarTerminalMergeableMultiblock : BazaarTerminalMultiblock() {
		override fun MultiblockShape.buildStructure() {
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
			z(2) {
				y(-1) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).netheriteCasing()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
				y(0) {
					x(2).anyPipedInventory()
					x(1).sponge()
					x(0).ironBlock()
					x(-1).sponge()
					x(-2).type(Material.LODESTONE)
				}
			}
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

	class BazaarTerminalMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: BazaarTerminalMultiblock,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, 100_000) {
		override val displayHandler: TextDisplayHandler = standardPowerDisplay(this)

		private val mergeEnd = createLinkage(
			offsetRight = -2,
			offsetUp = -1,
			offsetForward = 2,
			linkageDirection = RelativeFace.LEFT,
			predicate = { multiblock is BazaarTerminalMergeableMultiblock },
			AdvancedShipFactoryEntity::class
		)

		fun handleInteract(player: Player) {
			BazaarTerminalMainMenu(player, this).openGui()
			player.information("other end: ${mergeEnd?.get()}")
		}
	}
}
