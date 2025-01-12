package net.horizonsend.ion.server.features.multiblock.type.ammo

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.attribute.AmmunitionRefillType
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity.StatusManager
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack
import java.lang.Integer.min

abstract class AmmoPressMultiblock : Multiblock(), EntityMultiblock<AmmoPressMultiblock.AmmoPressMultiblockEntity> {

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).powerInput()
				x(+1).anyStairs()
			}

			y(0) {
				x(+0).machineFurnace()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anySlab()
				x(+0).grindstone()
				x(+1).anySlab()
			}
		}

		z(+2) {
			y(-1) {
				x(-1).anyCopperVariant()
				x(+0).sponge()
				x(+1).anyCopperVariant()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+3) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).aluminumBlock()
				x(+1).anyGlassPane()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).type(Material.ANVIL)
				x(+1).anyGlassPane()
			}
		}

		z(+4) {
			y(-1) {
				x(-1).anyCopperVariant()
				x(+0).sponge()
				x(+1).anyCopperVariant()
			}

			y(+0) {
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
			}
		}

		z(+5) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anySlab()
				x(+0).grindstone()
				x(+1).anySlab()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
			}

			y(0) {
				x(+0).anyPipedInventory()
			}
		}
	}

	override val name = "ammopress"

	override val signText = createSignText(
		line1 = "&6Ammo",
		line2 = "&8Press",
		line3 = null,
		line4 = null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): AmmoPressMultiblockEntity {
		return AmmoPressMultiblockEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	abstract val maxPower: Int

	class AmmoPressMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: AmmoPressMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, multiblock.maxPower), SyncTickingMultiblockEntity, StatusTickedMultiblockEntity, LegacyMultiblockEntity,
		DisplayMultiblockEntity {
		override val tickingManager: TickingManager = TickingManager(interval = 20)
		override val statusManager: StatusManager = StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this, +0.0, +0.0, +0.0, 0.45f) },
			{ StatusDisplayModule(it, statusManager, +0.0, -0.10, +0.0, 0.45f) }
		).register()

		override fun tick() {
			val furnaceInventory = getInventory(0, 0, 0) as? FurnaceInventory ?: return sleepWithStatus(text("No Furnace!"), 250)
			val smelting = furnaceInventory.smelting
			val fuel = furnaceInventory.fuel
			val fuelCustomItem = fuel?.customItem

			if (powerStorage.getPower() == 0 ||
				smelting == null ||
				smelting.type != Material.PRISMARINE_CRYSTALS ||
				fuel == null ||
				fuelCustomItem == null
			) {
				return
			}

		if (!fuelCustomItem.hasComponent(CustomComponentTypes.AMMUNITION_STORAGE)) return
		val ammoComponent = fuelCustomItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE)
		val ammoRefillAttribute = fuelCustomItem.getAttributes(fuel).filterIsInstance<AmmunitionRefillType>().firstOrNull() ?: return

			// deposit blaster/magazine into output if full
			if (ammoComponent.getAmmo(fuel) == ammoComponent.balancingSupplier.get().capacity) {
				val result = furnaceInventory.result
				if (result != null && result.type != Material.AIR) return
				furnaceInventory.result = furnaceInventory.fuel
				furnaceInventory.fuel = null
				return
			}

			// refill item check
			val inventory = getInventory(0, 0, 6) ?: return
			val typeRefill = ammoRefillAttribute.type
			if (!inventory.containsAtLeast(ItemStack(typeRefill), 1)) return

			tickingManager.sleep(200)
			furnaceInventory.holder?.burnTime = 200
			furnaceInventory.holder?.update()

			val ammoToSet = min(
				ammoComponent.balancingSupplier.get().capacity - ammoComponent.getAmmo(fuel),
				ammoComponent.balancingSupplier.get().ammoPerRefill
			)
			ammoComponent.setAmmo(fuel, fuelCustomItem, ammoComponent.getAmmo(fuel) + ammoToSet)
			inventory.removeItemAnySlot(ItemStack(typeRefill))
			powerStorage.removePower(250)
			powerStorage.removePower(250)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
