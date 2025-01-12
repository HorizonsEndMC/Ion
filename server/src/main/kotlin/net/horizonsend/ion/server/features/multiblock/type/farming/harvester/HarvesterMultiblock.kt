package net.horizonsend.ion.server.features.multiblock.type.farming.harvester

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.farming.Crop
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
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
import org.bukkit.block.data.Ageable

abstract class HarvesterMultiblock(val tierMaterial: Material, val tierNumber: Int, tierColor: TextColor) : Multiblock(), EntityMultiblock<HarvesterMultiblock.HarvesterEntity> {
	override val name: String = "harvester"
	override val signText: Array<Component?> = arrayOf(
		ofChildren(text("Auto ", GRAY), text("Harvester", GREEN)),
		ofChildren(text("Tier ", DARK_AQUA), text(tierNumber, tierColor)),
		null,
		null
	)

	val powerPerCrop: Int = 10
	abstract val regionDepth: Int

	abstract val maxPower: Int

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).ironBlock()
				x(0).powerInput()
				x(+1).ironBlock()
			}
			y(0) {
				x(-1).anyStairs()
				x(0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).anyGlassPane()
				x(0).type(tierMaterial)
				x(+1).anyGlassPane()
			}
			y(0) {
				x(-1).anyGlassPane()
				x(0).type(tierMaterial)
				x(+1).anyGlassPane()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).dispenser()
				x(0).dispenser()
				x(+1).dispenser()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).anyPipedInventory()
				x(+1).ironBlock()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).type(Material.STONECUTTER)
				x(0).type(Material.STONECUTTER)
				x(+1).type(Material.STONECUTTER)
			}
			y(0) {
				x(-1).anyStairs()
				x(+1).anyStairs()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): HarvesterEntity {
		return HarvesterEntity(
			data,
			manager,
			this,
			x,
			y,
			z,
			world,
			structureDirection
		)
	}

	class HarvesterEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: HarvesterMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, multiblock.maxPower), SyncTickingMultiblockEntity, LegacyMultiblockEntity, StatusTickedMultiblockEntity {
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()
		override val tickingManager: TickingManager = TickingManager(interval = 20)

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this, +0.0, +0.0, +0.0, 0.45f) },
			{ StatusDisplayModule(it, statusManager, +0.0, -0.10, +0.0, 0.45f) }
		).register()

		override fun tick() {
			val inventory = getInventory(right = 0, up = 0, forward = 2) ?: return tickingManager.sleep(1000)
			var broken = 0

			val initialPower = powerStorage.getPower()
			if (initialPower == 0) return sleepWithStatus(text("No Power", RED), 500)

			val region = getRegionWithDimensions(-1 ,-1 ,4, 3, 1, multiblock.regionDepth)

			for (block in region) {
				val data = block.blockData

				if (data !is Ageable) continue
				if (data.age != data.maximumAge) continue
				val crop = Crop[block.type] ?: continue

				val drops = crop.getDrops(block).toTypedArray()

				for (item in drops) {
					if (!LegacyItemUtils.canFit(inventory, item)) {
						tickingManager.sleep(800)
						break
					}
				}

				if ((broken + 1) * multiblock.powerPerCrop > initialPower) {
					tickingManager.sleep(500)
					break
				}

				crop.harvest(block)
				broken++

				val didNotFit = inventory.addItem(*drops)

				if (didNotFit.isNotEmpty()) {
					sleepWithStatus(text("No Space", RED), 500)
					break
				}

				if (broken >= multiblock.tierNumber) break
			}

			if (broken == 0) return sleepWithStatus(text("Sleeping", BLUE, ITALIC), 100)

			powerStorage.removePower(broken * multiblock.powerPerCrop)
			setStatus(text("Working", GREEN))
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
