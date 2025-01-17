package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.ProgressManager
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity.StatusManager
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataAdapterContext

abstract class IndustryEntity(data: PersistentMultiblockData, multiblock: Multiblock, manager: MultiblockManager, x: Int, y: Int, z: Int, world: World, structureDirection: BlockFace, maxPower: Int) :
	SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, maxPower),
	LegacyMultiblockEntity,
	SyncTickingMultiblockEntity,
	RecipeProcessingMultiblockEntity<FurnaceEnviornment>,
	ProgressMultiblock,
	StatusTickedMultiblockEntity {

	override var lastRecipe: MultiblockRecipe<FurnaceEnviornment>? = null

	override val progressManager: ProgressManager = ProgressManager(data)
	override val tickingManager: TickingManager = TickingManager(20)
	override val statusManager: StatusManager = StatusManager()

	final override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
		this,
		{ PowerEntityDisplayModule(it, this) },
		{ StatusDisplayModule(it, statusManager) }
	)

	override fun loadFromSign(sign: Sign) {
		migrateLegacyPower(sign)
	}

	override fun buildRecipeEnviornment(): FurnaceEnviornment {
		return FurnaceEnviornment(this)
	}

	override fun tick() {
		tryProcessRecipe()
	}

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		progressManager.saveProgressData(store)
		super.storeAdditionalData(store, adapterContext)
	}
}
