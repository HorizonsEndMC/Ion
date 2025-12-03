package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MINI_REACTOR_CORE
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object CoreForgeMultiblock : Multiblock(), EntityMultiblock<CoreForgeMultiblock.StandardCoreForgeEntity>, DisplayNameMultilblock, InteractableMultiblock {
	override val name: String = "coreforge"
	override val displayName: Component get() = text("Core Forge")
	override val description: Component get() = text("Combines materials into a warship core")
	override val signText = createSignText(
		line1 = "&4Core Forge",
		line2 = null,
		line3 = null,
		line4 = null
	)
	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(1).ironBlock()
				x(0).powerInput()
				x(-1).ironBlock()
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(2).anyPipedInventory()
				x(1).extractor()
				x(0).titaniumBlock()
				x(-1).anyGlass()
				x(-2).anyPipedInventory()
			}
			y(2) {
				x(2).anyWall()
				x(1).anyGlass()
				x(0).titaniumBlock()
				x(-1).anyGlass()
				x(-2).anyWall()
			}
			y(3) {
				x(2).ironBlock()
				x(1).anyGlass()
				x(0).titaniumBlock()
				x(-1).anyGlass()
				x(-2).ironBlock()
			}
			y(4) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(1) {
			y(0) {
				x(2).ironBlock()
				x(1).sponge()
				x(0).sponge()
				x(-1).sponge()
				x(-2).ironBlock()
			}
			y(1) {
				x(2).extractor()
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.UP, example = Material.END_ROD.createBlockData()))
				x(0).thrusterBlock()
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.UP, example = Material.END_ROD.createBlockData()))
				x(-2).anyGlass()
			}
			y(2) {
				x(2).anyGlass()
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(0).thrusterBlock()
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(-2).anyGlass()
			}
			y(3) {
				x(2).anyGlass()
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(0).thrusterBlock()
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(-2).anyGlass()
			}
			y(4) {
				x(2).ironBlock()
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
				x(-2).ironBlock()
			}
		}
		z(2) {
			y(0) {
				x(2).titaniumBlock()
				x(1).sponge()
				x(0).sponge()
				x(-1).sponge()
				x(-2).titaniumBlock()
			}
			y(1) {
				x(2).titaniumBlock()
				x(1).thrusterBlock()
				x(0).redstoneBlock()
				x(-1).thrusterBlock()
				x(-2).titaniumBlock()
			}
			y(2) {
				x(2).titaniumBlock()
				x(1).thrusterBlock()
				x(0).redstoneBlock()
				x(-1).thrusterBlock()
				x(-2).titaniumBlock()
			}
			y(3) {
				x(2).titaniumBlock()
				x(1).thrusterBlock()
				x(0).redstoneBlock()
				x(-1).thrusterBlock()
				x(-2).titaniumBlock()
			}
			y(4) {
				x(2).titaniumBlock()
				x(1).titaniumBlock()
				x(0).titaniumBlock()
				x(-1).titaniumBlock()
				x(-2).titaniumBlock()
			}
		}
		z(3) {
			y(0) {
				x(2).ironBlock()
				x(1).sponge()
				x(0).sponge()
				x(-1).sponge()
				x(-2).ironBlock()
			}
			y(1) {
				x(2).anyGlass()
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.UP, example = Material.END_ROD.createBlockData()))
				x(0).thrusterBlock()
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.UP, example = Material.END_ROD.createBlockData()))
				x(-2).anyGlass()
			}
			y(2) {
				x(2).anyGlass()
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(0).thrusterBlock()
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(-2).anyGlass()
			}
			y(3) {
				x(2).anyGlass()
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(0).thrusterBlock()
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
				x(-2).anyGlass()
			}
			y(4) {
				x(2).ironBlock()
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
				x(-2).ironBlock()
			}
		}
		z(4) {
			y(0) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(2).ironBlock()
				x(1).anyGlass()
				x(0).titaniumBlock()
				x(-1).anyGlass()
				x(-2).ironBlock()
			}
			y(2) {
				x(2).anyWall()
				x(1).anyGlass()
				x(0).titaniumBlock()
				x(-1).anyGlass()
				x(-2).anyWall()
			}
			y(3) {
				x(2).ironBlock()
				x(1).anyGlass()
				x(0).titaniumBlock()
				x(-1).anyGlass()
				x(-2).ironBlock()
			}
			y(4) {
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
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
	): StandardCoreForgeEntity {
		return StandardCoreForgeEntity(data, manager, x, y, z, world, structureDirection)
	}

	class StandardCoreForgeEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : CoreForgeEntity(data, CoreForgeMultiblock, manager, world, x, y, z, structureDirection) {
		override val ioData: IOData = none()
		override val guiTitle: String = "Core Forge"
		override var targetCore: ItemStack = MINI_REACTOR_CORE.getValue().constructItemStack()

		override val displayHandler: TextDisplayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ StatusDisplayModule(it, statusManager) }
		).register()

	}
override fun onSignInteract( sign: Sign, player: Player, event: PlayerInteractEvent) {
	val entity = getMultiblockEntity(sign) ?: return
	entity.openGui(player)
		}

}

