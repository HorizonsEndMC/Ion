package net.horizonsend.ion.server.features.multiblock.type.printer

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

abstract class PrinterMultiblock : Multiblock(), NewPoweredMultiblock<PrinterMultiblock.PrinterEntity> {
	override val name: String = "printer"
	override val maxPower: Int = 50_000
	abstract fun getOutput(product: Material): ItemStack
	abstract val mirrored: Boolean

	protected abstract fun MultiblockShape.RequirementBuilder.printerCoreBlock()
	protected abstract fun MultiblockShape.RequirementBuilder.printerMachineryBlock()
	protected abstract fun MultiblockShape.RequirementBuilder.printerProductBlock()


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
				if (!mirrored) {
					x(-1).anyCopperVariant()
					x(+0).printerMachineryBlock()
					x(+1).ironBlock()
				} else {
					x(-1).ironBlock()
					x(+0).printerMachineryBlock()
					x(+1).anyCopperVariant()
				}
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+2) {
			y(-1) {
				if (!mirrored) {
					x(-1).anyCopperVariant()
					x(+0).printerCoreBlock()
					x(+1).anyGlass()
				} else {
					x(-1).anyGlass()
					x(+0).printerCoreBlock()
					x(+1).anyCopperVariant()
				}
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).printerProductBlock()
				x(+1).anyGlass()
			}
		}

		z(+3) {
			y(-1) {
				if (!mirrored) {
					x(-1).anyCopperVariant()
					x(+0).printerMachineryBlock()
					x(+1).ironBlock()
				} else {
					x(-1).ironBlock()
					x(+0).printerMachineryBlock()
					x(+1).anyCopperVariant()
				}
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+4) {
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

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): PrinterEntity {
		return PrinterEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class PrinterEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val poweredMultiblock: PrinterMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : SimplePoweredMultiblockEntity(data, manager, poweredMultiblock, x, y, z, world, structureDirection), StatusTickedMultiblockEntity, SyncTickingMultiblockEntity, LegacyMultiblockEntity {
		override val tickingManager: TickingManager = TickingManager(interval = 20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
		).register()

		override fun tick() {
			val furnaceInventory = getInventory(0, 0, 0) as? FurnaceInventory ?: return sleepWithStatus(text("No Furnace"), 250)
			val outputInventory = getInventory(0, 0, 4) ?: return sleepWithStatus(text("No Output Inventory", RED), 250)

			val fuel = furnaceInventory.fuel

			if (powerStorage.getPower() < 250) return sleepWithStatus(text("No Power", RED), 100)
			if (fuel?.type != Material.COBBLESTONE) return sleepWithStatus(text("Out of Cobblestone", RED), 100)

			val product = getBlockRelative(0, 0, 2).type
			val output = poweredMultiblock.getOutput(product)

			if (!LegacyItemUtils.canFit(outputInventory, output)) return sleepWithStatus(text("No Space", RED), 100)
			LegacyItemUtils.addToInventory(outputInventory, output)

			fuel.amount--

			powerStorage.removePower(250)

			sleepWithStatus(text("Working", GREEN), 100)

			val furnace = furnaceInventory.holder ?: return
			furnace.burnTime = Short.MAX_VALUE
			furnace.cookTime = 100

			furnace.update()
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
