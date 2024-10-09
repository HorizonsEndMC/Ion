package net.horizonsend.ion.server.features.multiblock.type.farming.harvester

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.multiblock.type.farming.Crop
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Ageable
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class HarvesterMultiblock(val tierMaterial: Material, val tierNumber: Int) : Multiblock(), NewPoweredMultiblock<HarvesterMultiblock.HarvesterEntity> {
	override val name: String = "harvester"
	override val signText: Array<Component?> = arrayOf(
		ofChildren(text("Auto ", GRAY), text("Harvester", GREEN)),
		ofChildren(text("Tier ", DARK_AQUA), text(tierNumber, AQUA)),
		null,
		null
	)

	val powerPerCrop: Int = 10
	abstract val regionDepth: Int

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-1).ironBlock()
				x(0).noteBlock()
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
	) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), PoweredMultiblockEntity, SyncTickingMultiblockEntity, LegacyMultiblockEntity, StatusMultiblock {
		override val statusManager: StatusMultiblock.StatusManager = StatusMultiblock.StatusManager()
		override val storage: PowerStorage = loadStoredPower(data)
		override val tickingManager: TickingManager = TickingManager(interval = 20)

		private val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
		).register()

		private fun cancelWithStatus(status: Component, sleepTicks: Int) {
			setStatus(status)
			tickingManager.sleep(sleepTicks)
		}

		override fun tick() {
			val inventory = getInventory(leftRight = 0, upDown = 0, backFourth = 2) ?: return tickingManager.sleep(1000)
			var broken = 0

			val initialPower = storage.getPower()
			if (initialPower == 0) return cancelWithStatus(text("No Power", RED), 500)

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
					cancelWithStatus(text("No Space", RED), 500)
					break
				}

				if (broken >= multiblock.tierNumber) break
			}

			if (broken == 0) return cancelWithStatus(text("Sleeping", BLUE, ITALIC), 100)

			storage.removePower(broken * multiblock.powerPerCrop)
			setStatus(text("Working", GREEN))
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			savePowerData(store)
		}

		override fun onLoad() {
			displayHandler.update()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
