package net.horizonsend.ion.server.features.multiblock.type.power.generator

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.machine.GeneratorFuel
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
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class GeneratorMultiblock(tierText: String, private val tierMaterial: Material) : Multiblock(), NewPoweredMultiblock<GeneratorMultiblock.GeneratorMultiblockEntity> {
	override val name = "generator"
	abstract val speed: Double

	override val signText = createSignText(
		line1 = "&2Power",
		line2 = "&8Generator",
		line3 = null,
		line4 = tierText
	)

	override fun MultiblockShape.buildStructure() {
		at(x = -1, y = -1, z = +0).extractor()
		at(x = +0, y = -1, z = +0).wireInputComputer()
		at(x = +1, y = -1, z = +0).extractor()
		at(x = -1, y = -1, z = +1).type(tierMaterial)
		at(x = +0, y = -1, z = +1).redstoneBlock()
		at(x = +1, y = -1, z = +1).type(tierMaterial)

		at(x = -1, y = +0, z = +0).anyGlassPane()
		at(x = +0, y = +0, z = +0).machineFurnace()
		at(x = +1, y = +0, z = +0).anyGlassPane()
		at(x = -1, y = +0, z = +1).anyGlassPane()
		at(x = +0, y = +0, z = +1).redstoneBlock()
		at(x = +1, y = +0, z = +1).anyGlassPane()
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): GeneratorMultiblockEntity {
		return GeneratorMultiblockEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class GeneratorMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: GeneratorMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), SyncTickingMultiblockEntity, PoweredMultiblockEntity, StatusTickedMultiblockEntity, LegacyMultiblockEntity, DisplayMultiblockEntity {
		override val powerStorage: PowerStorage = loadStoredPower(data)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(interval = 20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
		).register()

		override fun tick() {
			val furnaceInventory = getInventory(0, 0, 0) as? FurnaceInventory ?: return sleepWithStatus(text("No Furnace"), 250)

			if (powerStorage.getPower() >= powerStorage.capacity) {
				world.playEffect(getOrigin().location.add(0.5, 0.5, 0.5), Effect.SMOKE, 4)
				sleepWithStatus(text("Power Full", RED), 200)
				return
			}

			val fuelItem = furnaceInventory.fuel ?: return sleepWithStatus(text("No Fuel", RED), 100)
			val fuel = GeneratorFuel.getFuel(fuelItem) ?: return sleepWithStatus(text("Invalid Fuel", RED), 100)

			val sleepTicks = (fuel.cooldown / multiblock.speed).toInt()
			sleepWithStatus(text("Working", GREEN), sleepTicks)

			fuelItem.amount--

			val furnace = furnaceInventory.holder

			furnace?.burnTime = sleepTicks.toShort()
			furnace?.update()

			powerStorage.addPower(fuel.power)
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			savePowerData(store)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
