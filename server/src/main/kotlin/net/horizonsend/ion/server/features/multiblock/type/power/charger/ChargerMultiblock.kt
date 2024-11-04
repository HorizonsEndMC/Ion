package net.horizonsend.ion.server.features.multiblock.type.power.charger

import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.powered.PoweredItem
import net.horizonsend.ion.server.features.gear.addPower
import net.horizonsend.ion.server.features.gear.getMaxPower
import net.horizonsend.ion.server.features.gear.getPower
import net.horizonsend.ion.server.features.gear.isPowerable
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.ItemStack

abstract class ChargerMultiblock(val tierText: String) : Multiblock(), EntityMultiblock<ChargerMultiblock.ChargerEntity>, FurnaceMultiblock, DisplayNameMultilblock {
	protected abstract val tierMaterial: Material

	protected abstract val powerPerSecond: Int

	override val displayName: Component = ofChildren(legacyAmpersand.deserialize(tierText), Component.text(" Item Charger"))

	abstract val maxPower: Int

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
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, signDirection, multiblock.maxPower), PoweredMultiblockEntity, LegacyMultiblockEntity {
		override val displayHandler = standardPowerDisplay(this)

		fun handleCharging(event: FurnaceBurnEvent, furnace: Furnace) {
			val availablePower = powerStorage.getPower()
			if (availablePower == 0) return

			val item = event.fuel
			if (isPowerable(item)) {
				handleLegacy(item, event, furnace, availablePower)
			}

			val custom = item.customItem

			if (custom is PoweredItem) {
				handleModern(item, custom, event, furnace, availablePower)
			}
		}

		private fun handleLegacy(
			item: ItemStack,
			event: FurnaceBurnEvent,
			furnace: Furnace,
			power: Int
		) {
			val inventory = furnace.inventory

			if (getMaxPower(item) == getPower(item)) {
				val result = inventory.result
				if (result != null && result.type != Material.AIR) return

				inventory.result = event.fuel
				inventory.fuel = null

				return
			}

			var multiplier = multiblock.powerPerSecond
			multiplier /= item.amount
			if (item.amount * multiplier > power) return

			addPower(item, multiplier)

			powerStorage.setPower(power - multiplier * item.amount)

			furnace.cookTime = 20.toShort()

			event.isCancelled = false
			event.isBurning = false
			event.burnTime = 20
		}

		private fun handleModern(
			item: ItemStack,
			customItem: PoweredItem,
			event: FurnaceBurnEvent,
			furnace: Furnace,
			power: Int
		) {
			val inventory = furnace.inventory

			if (customItem.getPowerCapacity(item) == customItem.getPower(item)) {
				val result = inventory.result
				if (result != null && result.type != Material.AIR) return

				inventory.result = event.fuel
				inventory.fuel = null

				return
			}

			var multiplier = multiblock.powerPerSecond
			multiplier /= item.amount

			if (item.amount * multiplier > power) return

			customItem.addPower(item, multiplier)

			powerStorage.setPower(power - multiplier * item.amount)

			furnace.cookTime = 20.toShort()

			event.isCancelled = false
			event.isBurning = false
			event.burnTime = 20
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
