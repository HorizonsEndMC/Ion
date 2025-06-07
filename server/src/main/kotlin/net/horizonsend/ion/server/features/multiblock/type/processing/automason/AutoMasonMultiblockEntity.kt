package net.horizonsend.ion.server.features.multiblock.type.processing.automason

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.crafting.input.AutoMasonRecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.FurnaceBasedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryParent.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataAdapterContext

class AutoMasonMultiblockEntity(data: PersistentMultiblockData, override val multiblock: AutoMasonMultiblock, manager: MultiblockManager, x: Int, y: Int, z: Int, world: World, structureFace: BlockFace) : SimplePoweredEntity(
	data,
	multiblock,
	manager,
	x,
	y,
	z,
	world,
	structureFace,
	150_000
), SyncTickingMultiblockEntity, RecipeProcessingMultiblockEntity<AutoMasonRecipeEnviornment>, StatusTickedMultiblockEntity, FurnaceBasedMultiblockEntity {
	override var lastRecipe: MultiblockRecipe<AutoMasonRecipeEnviornment>? = null
	override var hasTicked: Boolean = false

	override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)
	override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

	override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
		this,
		{ PowerEntityDisplayModule(it, this) },
		{ StatusDisplayModule(it, statusManager) }
	)

	val mergeEnd = createLinkage(
		offsetRight = if (multiblock is AutoMasonMultiblock.StandardAutoMasonMirroredMergableMultiblock) -3 else 3,
		offsetUp = -1,
		offsetForward = 3,
		linkageDirection = if (multiblock is AutoMasonMultiblock.StandardAutoMasonMirroredMergableMultiblock) RelativeFace.LEFT else RelativeFace.RIGHT,
		predicate = { multiblock.mergeEnabled },
		AdvancedShipFactoryEntity::class
	)

	companion object {
		private const val PROCESSING_COUNT = 4
	}

	override fun tick() {
		repeat(PROCESSING_COUNT) { tryProcessRecipe() }
	}

	override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
		savePowerData(store)
	}

	fun getInputInventory(): Inventory? = multiblock.inputOffset?.let { getInventory(it.x, it.y, it.z) }

	override fun buildRecipeEnviornment(): AutoMasonRecipeEnviornment? {
		val input = getInputInventory() ?: return null
		val output = multiblock.outputOffset?.let { getInventory(it.x, it.y, it.z) } ?: return null

		return AutoMasonRecipeEnviornment(
			multiblock = this,
			inputInventory = input,
			outputInventory = output,
			powerStorage = powerStorage,
			tickingManager = tickingManager
		)
	}
}
