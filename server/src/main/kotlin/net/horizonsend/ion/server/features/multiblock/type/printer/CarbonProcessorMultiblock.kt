package net.horizonsend.ion.server.features.multiblock.type.printer

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.ammo.MissileLoaderMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.isConcretePowder
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlass
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

object CarbonProcessorMultiblock : Multiblock(), EntityMultiblock<CarbonProcessorMultiblock.CarbonProcessorEntity> {
	override val name = "processor"

	override val signText = createSignText(
		line1 = "&3Carbon",
		line2 = "&7Processor",
		line3 = null,
		line4 = "&7:[''']:"
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).sponge()
				x(+0).goldBlock()
				x(+1).sponge()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).anyGlass()
				x(+1).anyGlass()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).hopper()
				x(+1).ironBlock()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyPipedInventory()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CarbonProcessorEntity {
		return CarbonProcessorEntity(data, manager, x, y, z, world, structureDirection)
	}

	class CarbonProcessorEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace
	) : SimplePoweredEntity(data, MissileLoaderMultiblock, manager, x, y, z, world, structureFace, 30_000), LegacyMultiblockEntity, StatusTickedMultiblockEntity, SyncTickingMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(interval = 1)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
		).register()

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}

		override fun tick() {
			val furnaceInventory = getInventory(0, 0, 0) as? FurnaceInventory ?: return sleepWithStatus(text("No Furnace"), 250)
			val outputInventory = getInventory(0, 0, 2) ?: return sleepWithStatus(text("No Output Inventory", NamedTextColor.RED), 250)

			val fuel = furnaceInventory.fuel

			if (powerStorage.getPower() < 250) return sleepWithStatus(text("No Power", NamedTextColor.RED), 100)
			if (fuel?.type?.isConcretePowder != true) return sleepWithStatus(text("Out of Powder", NamedTextColor.RED), 100)

			val output = getOutput(fuel)

			if (!LegacyItemUtils.canFit(outputInventory, output)) return sleepWithStatus(text("No Space", NamedTextColor.RED), 100)
			LegacyItemUtils.addToInventory(outputInventory, output)

			fuel.amount--

			powerStorage.removePower(250)

			sleepWithStatus(text("Working", GREEN), 50)

			val furnace = furnaceInventory.holder ?: return

			furnace.burnTime = Short.MAX_VALUE
			furnace.cookTime = 50

			furnace.update()
		}

		fun getOutput(inputType: ItemStack): ItemStack {
			val glassType = getBlockRelative(0, 0, 1).type

			if (glassType.isStainedGlass) {
				val concreteTypeName = glassType.name.replace("STAINED_GLASS", "CONCRETE")
				val outputType = Material.getMaterial(concreteTypeName) ?: error("No material $concreteTypeName")

				return ItemStack(outputType, 1)
			}

			val existingType = inputType.type
			return ItemStack(Material.getMaterial(existingType.name.removeSuffix("_POWDER")) ?: error("No material $glassType"), 1)
		}
	}
}
