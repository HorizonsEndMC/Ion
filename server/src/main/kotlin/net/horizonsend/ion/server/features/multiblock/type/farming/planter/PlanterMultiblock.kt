package net.horizonsend.ion.server.features.multiblock.type.farming.planter

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.farming.Crop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory

abstract class PlanterMultiblock(val tierMaterial: Material, val tierNumber: Int, tierColor: TextColor) : Multiblock(), EntityMultiblock<PlanterMultiblock.PlanterEntity> {
	override val name: String = "planter"
	override val signText: Array<Component?> = arrayOf(
		ofChildren(text("Auto ", GRAY), text("Planter", GREEN)),
		ofChildren(text("Tier ", DARK_AQUA), text(tierNumber, tierColor)),
		null,
		null
	)

	abstract val regionDepth: Int
	private val powerPerCrop: Int = 10

	abstract val maxPower: Int

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).anyStairs()
				x(0).powerInput()
				x(+1).anyStairs()
			}
			y(0) {
				x(-1).anyStairs()
				x(0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).sponge()
				x(0).type(tierMaterial)
				x(+1).sponge()
			}
			y(0) {
				x(-1).anyGlass()
				x(0).type(tierMaterial)
				x(+1).anyGlass()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).sponge()
				x(0).type(tierMaterial)
				x(+1).sponge()
			}
			y(0) {
				x(-1).anyGlass()
				x(0).type(tierMaterial)
				x(+1).anyGlass()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).dispenser()
				x(0).dispenser()
				x(+1).dispenser()
			}
			y(0) {
				x(-1).anyStairs()
				x(0).anyStairs()
				x(+1).anyStairs()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): PlanterEntity {
		return PlanterEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class PlanterEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: PlanterMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, multiblock.maxPower), PoweredMultiblockEntity, SyncTickingMultiblockEntity, LegacyMultiblockEntity, StatusTickedMultiblockEntity {
		override val tickingManager: TickingManager = TickingManager(interval = 20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
		).register()

		override fun tick() {
			var planted = 0
			val initialPower = powerStorage.getPower()
			if (initialPower == 0) return sleepWithStatus(text("No Power", RED), 500)

			val inventory: FurnaceInventory = getInventory(0, 0, 0) as? FurnaceInventory ?: return tickingManager.sleep(800)

			val seedItem = inventory.fuel ?: return sleepWithStatus(text("No Seeds", RED), 500)
			val crop = Crop.findBySeed(seedItem.type) ?: return  sleepWithStatus( text("Unknown Crop", RED), 1000)

			val region = getRegionWithDimensions(-1 ,-1 ,4, 3, 1, multiblock.regionDepth)

			for (block in region) {
				if (block.type != Material.AIR) continue
				if (seedItem.amount <= 0) break
				if (!crop.canBePlanted(block)) continue
				if (block.lightFromBlocks < 7 && block.lightFromSky < 7) continue

				if ((planted + 1) * multiblock.powerPerCrop > initialPower) {
					tickingManager.sleep(500)
					break
				}

				planted++
				seedItem.amount--

				crop.plant(block)

				if (planted >= multiblock.tierNumber) break
			}

			if (planted == 0) return sleepWithStatus(text("Sleeping", BLUE, ITALIC), 100)

			powerStorage.removePower(planted * multiblock.powerPerCrop)
			setStatus(text("Working", GREEN))
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
