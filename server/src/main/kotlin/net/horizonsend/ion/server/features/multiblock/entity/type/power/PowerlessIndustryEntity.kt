package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.FurnaceBasedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.ProgressManager
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity.StatusManager
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.inputs.IOData
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class PowerlessIndustryEntity(
	data: PersistentMultiblockData,
	multiblock: Multiblock,
	manager: MultiblockManager,
	x: Int,
	y: Int,
	z: Int,
	world: World,
	structureDirection: BlockFace
) : MultiblockEntity(manager, multiblock, world, x, y, z, structureDirection),
	LegacyMultiblockEntity,
	SyncTickingMultiblockEntity,
	RecipeProcessingMultiblockEntity<FurnaceEnviornment>,
	ProgressMultiblock,
	DisplayMultiblockEntity,
	StatusTickedMultiblockEntity,
	FurnaceBasedMultiblockEntity {

	override var lastRecipe: MultiblockRecipe<FurnaceEnviornment>? = null
	override var hasTicked: Boolean = false

	override val progressManager: ProgressManager = ProgressManager(data)
	override val tickingManager: TickingManager = TickingManager(20)
	override val statusManager: StatusManager = StatusManager()

	override val ioData: IOData = IOData.Builder(this).build()

	@Suppress("LeakingThis")
	override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
		this,
		{ StatusDisplayModule(it, statusManager) }
	)

	override fun loadFromSign(sign: Sign) {
		migrateLegacyPower(sign)
	}

	override fun buildRecipeEnviornment(): FurnaceEnviornment? {
		val fakeHolder = object : PoweredMultiblockEntity {
			override val powerStorage: PowerStorage get() = throw UnsupportedOperationException()
			override val maxPower: Int = Int.MAX_VALUE
		}
		val fakePowerStorage = PowerStorage(fakeHolder, Int.MAX_VALUE, Int.MAX_VALUE)
		return FurnaceEnviornment(
			this,
			getInventory(0, 0, 0) as? FurnaceInventory ?: return null,
			fakePowerStorage,
			tickingManager,
			progressManager
		)
	}

	private var furnaceActive = false

	override fun tick() {
		if (!tryProcessRecipe()) {
			progressManager.reset()
			if (furnaceActive) putOutFurnace()
			return
		}
		furnaceActive = true
		stopCooking()
	}

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		progressManager.saveProgressData(store)
		super.storeAdditionalData(store, adapterContext)
	}
}
