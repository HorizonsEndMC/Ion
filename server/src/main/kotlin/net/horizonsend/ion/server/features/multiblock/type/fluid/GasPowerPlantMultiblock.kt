package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses.EMPTY_CANISTER
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Effect
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import kotlin.math.roundToInt

object GasPowerPlantMultiblock : Multiblock(), NewPoweredMultiblock<GasPowerPlantMultiblock.GasPowerPlantMultiblockEntity> {

	override val maxPower: Int = 500000

	override val name: String = "gaspowerplant"

	override val signText: Array<Component?> = arrayOf(
		text()
			.append(text("Gas", NamedTextColor.RED))
			.append(text(" Power Plant", NamedTextColor.GOLD))
			.build(),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
				x(+2).anyGlassPane()
			}
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).extractor()
				x(+0).wireInputComputer()
				x(+1).extractor()
				x(+2).anyCopperVariant()
			}
		}
		z(+1) {
			y(+0) {
				x(-2).anyCopperVariant()
				x(-1).lightningRod()
				x(+0).sponge()
				x(+1).lightningRod()
				x(+2).anyCopperVariant()
			}
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).anyCopperVariant()
			}
		}
		z(+2) {
			y(+0) {
				x(-2).anyGlass()
				x(-1).lightningRod()
				x(+0).sponge()
				x(+1).lightningRod()
				x(+2).anyGlass()
			}
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).anyCopperVariant()
			}
		}
		z(+3) {
			y(+0) {
				x(-2).anyGlass()
				x(-1).lightningRod()
				x(+0).sponge()
				x(+1).lightningRod()
				x(+2).anyGlass()
			}
			y(-1) {
				x(-2).anyGlass()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).anyGlass()
			}
		}
		z(+4) {
			y(+0) {
				x(-2).anyGlass()
				x(-1).lightningRod()
				x(+0).sponge()
				x(+1).lightningRod()
				x(+2).anyGlass()
			}
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).anyCopperVariant()
			}
		}
		z(+5) {
			y(+0) {
				x(-2).anyCopperVariant()
				x(-1).lightningRod()
				x(+0).sponge()
				x(+1).lightningRod()
				x(+2).anyCopperVariant()
			}
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).anyWall()
				x(+0).redstoneBlock()
				x(+1).anyWall()
				x(+2).anyCopperVariant()
			}
		}
		z(+6) {
			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlassPane()
				x(+0).anyPipedInventory()
				x(+1).anyGlassPane()
				x(+2).anyGlassPane()
			}
			y(-1) {
				x(-2).anyCopperVariant()
				x(-1).ironBlock()
				x(+0).extractor()
				x(+1).ironBlock()
				x(+2).anyCopperVariant()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): GasPowerPlantMultiblockEntity {
		return GasPowerPlantMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class GasPowerPlantMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, GasPowerPlantMultiblock, x, y, z, world, structureDirection), SyncTickingMultiblockEntity, PoweredMultiblockEntity, StatusTickedMultiblockEntity, LegacyMultiblockEntity, DisplayMultiblockEntity {
		override val multiblock: GasPowerPlantMultiblock = GasPowerPlantMultiblock
		override val powerStorage: PowerStorage = loadStoredPower(data)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(interval = 20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
		).register()

		override fun tick() {
			val inventory = getInventory(0, 0, 0) as? FurnaceInventory ?: return

			val fuelItem = inventory.smelting ?: return
			val oxidizerItem = inventory.fuel ?: return

			val fuel = (fuelItem.customItem as? GasCanister) ?: return
			val oxidizer = (oxidizerItem.customItem as? GasCanister) ?: return

			val fuelType = fuel.gas
			val oxidizerType = oxidizer.gas

			if (fuelType !is GasFuel || oxidizerType !is GasOxidizer) return

			val consumed = checkCanisters(inventory, fuelItem, fuel, oxidizerItem, oxidizer) ?: return

			if (powerStorage.getPower() < maxPower) {
				tickingManager.sleep(fuelType.cooldown)

				inventory.holder?.burnTime = fuelType.cooldown.toShort()
				inventory.holder?.update()

				val power = (fuelType.powerPerUnit * oxidizerType.powerMultiplier) * consumed
				powerStorage.addPower(power.roundToInt())
			} else {
				world.playEffect(location.toCenterLocation(), Effect.SMOKE, 4)
			}
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			savePowerData(store)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}

		private fun checkCanisters(furnace: FurnaceInventory, fuelItem: ItemStack, fuelType: GasCanister, oxidizerItem: ItemStack, oxidizerType: GasCanister): Int? {
			val fuelFill = fuelType.getFill(fuelItem)
			val oxidizerFill = oxidizerType.getFill(oxidizerItem)

			// Burn fuel and oxidizer at 1:1
			// Cap consumption at 30 units
			val consumed = minOf(ConfigurationFiles.globalGassesConfiguration().powerPlantConsumption, fuelFill, oxidizerFill)

			// God forbid it goes negative
			if (fuelFill <= 0) {
				clearEmpty(furnace, fuelItem)
				return null
			}

			if (oxidizerFill <= 0) {
				clearEmpty(furnace, oxidizerItem)
				return null
			}

			if (fuelFill - consumed <= 0) {
				// Replaces with empty, no need to set fill to zero
				if (clearEmpty(furnace, fuelItem)) return null
			} else {
				fuelType.setFill(fuelItem, fuelFill - consumed)
			}

			if (oxidizerFill - consumed <= 0) {
				// Replaces with empty, no need to set fill to zero
				if (clearEmpty(furnace, oxidizerItem)) return null
			} else {
				oxidizerType.setFill(oxidizerItem, oxidizerFill - consumed)
			}

			return consumed
		}

		/** Returns whether the process should be aborted due to a problem **/
		private fun clearEmpty(furnaceInventory: Inventory, itemStack: ItemStack): Boolean {
			val discardChest = getInventory(0, 0, 6) ?: return true
			if (!LegacyItemUtils.canFit(discardChest, EMPTY_CANISTER)) return true

			furnaceInventory.remove(itemStack)
			return false
		}
	}
}
