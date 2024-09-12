package net.horizonsend.ion.server.features.multiblock.type.power.charger

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.PowerStorage
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.ItemStack

abstract class ChargerMultiblock(val tierText: String) : Multiblock(), NewPoweredMultiblock<ChargerMultiblock.ChargerEntity>, FurnaceMultiblock {
	protected abstract val tierMaterial: Material

	protected abstract val powerPerSecond: Int

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
				x(+1).anyGlassPane()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).type(tierMaterial)
				x(+1).anyGlassPane()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).type(tierMaterial)
				x(+1).anyGlassPane()
			}
		}
	}

	override val name = "charger"

	override val signText = createSignText(
		line1 = "&6Item",
		line2 = "&8Charger",
		line3 = null,
		line4 = tierText
	)

	override fun onFurnaceTick(
		event: FurnaceBurnEvent,
		furnace: Furnace,
		sign: Sign
	) {
		val entity = getMultiblockEntity(sign) ?: return

		entity.handleCharging(event, furnace)
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): ChargerEntity {
		return ChargerEntity(
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

	class ChargerEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: ChargerMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace,
	) : MultiblockEntity(manager, multiblock, x, y, z, world, signDirection), PoweredMultiblockEntity {
		override val storage: PowerStorage = loadStoredPower(data)

		private val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		fun handleCharging(event: FurnaceBurnEvent, furnace: Furnace) {
			val availablePower = storage.getPower()
			if (availablePower == 0) return

			val item = event.fuel
			if (isPowerable(item)) {
				handleLegacy(item, event, furnace, availablePower)
			}

			val custom = item.customItem

			val custom = item.customItem ?: return
			if (custom.hasComponent(CustomComponentTypes.POWER_STORAGE))
				handleModern(item, custom, custom.getComponent(CustomComponentTypes.POWER_STORAGE), event, furnace, inventory, sign, power)
		}

		fun handleModern(
			item: ItemStack,
			customItem: CustomItem,
			powerManager: PowerStorage,
			event: FurnaceBurnEvent,
			furnace: Furnace,
			inventory: FurnaceInventory,
			sign: Sign,
			power: Int
		) {
			if (powerManager.getMaxPower(customItem, item) == powerManager.getPower(item)) {
				val result = inventory.result
				if (result != null && result.type != Material.AIR) return
				inventory.result = event.fuel
				inventory.fuel = null
				return
			}

			var multiplier = powerPerSecond
			multiplier /= item.amount

			if (item.amount * multiplier > power) return

			powerManager.addPower(item, customItem, multiplier)

			storage.setPower(power - multiplier * item.amount)

			furnace.cookTime = 20.toShort()

			event.isCancelled = false
			event.isBurning = false
			event.burnTime = 20
		}

		override fun onLoad() {
			displayHandler.update()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData) {
			savePowerData(store)
		}

		override val powerInputOffset: Vec3i = Vec3i(0, -1, 0)
	}
}
