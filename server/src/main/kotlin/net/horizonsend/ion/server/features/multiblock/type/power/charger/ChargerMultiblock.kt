package net.horizonsend.ion.server.features.multiblock.type.power.charger

import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.PowerStorage
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
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset.pane
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
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
				x(-1).anyGlassPane(pane(RelativeFace.RIGHT, RelativeFace.FORWARD))
				x(+0).powerInput()
				x(+1).anyGlassPane(pane(RelativeFace.LEFT, RelativeFace.FORWARD))
			}

			y(+0) {
				x(-1).anyGlassPane(pane(RelativeFace.RIGHT, RelativeFace.FORWARD))
				x(+0).machineFurnace()
				x(+1).anyGlassPane(pane(RelativeFace.LEFT, RelativeFace.FORWARD))
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyGlassPane(pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(+0).type(tierMaterial)
				x(+1).anyGlassPane(pane(RelativeFace.LEFT, RelativeFace.BACKWARD))
			}

			y(+0) {
				x(-1).anyGlassPane(pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(+0).type(tierMaterial)
				x(+1).anyGlassPane(pane(RelativeFace.LEFT, RelativeFace.BACKWARD))
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

			var multiplier = multiblock.powerPerSecond
			multiplier /= item.amount

			if (item.amount * multiplier > power) return

			powerManager.addPower(item, customItem, multiplier)

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
