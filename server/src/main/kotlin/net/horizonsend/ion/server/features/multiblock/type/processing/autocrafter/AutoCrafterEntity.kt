package net.horizonsend.ion.server.features.multiblock.type.processing.autocrafter

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.starship.factory.AvailableItemInformation
import net.horizonsend.ion.server.features.starship.factory.ShipFactoryTask
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.features.transport.items.util.canAddAll
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.item.ItemStack as NMSItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.level.block.CrafterBlock
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemStack as BukkitItemStack
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrNull

class AutoCrafterEntity(
	data: PersistentMultiblockData,
	manager: MultiblockManager,
	override val multiblock: AutoCrafterMultiblock,
	x: Int,
	y: Int,
	z: Int,
	world: World,
	structureDirection: BlockFace,
) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, multiblock.maxPower), SyncTickingMultiblockEntity, StatusTickedMultiblockEntity, LegacyMultiblockEntity {
	override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(interval = 20)
	override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

	override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
		this,
		{ PowerEntityDisplayModule(it, this) },
		{ StatusDisplayModule(it, statusManager) }
	).register()

	private fun getInput(): Inventory? = getInventory(if (multiblock.mirrored) +2 else -2, 0, 1)
	private fun getRecipeHolder(): Inventory? = getInventory(0, 0, 1)
	private fun getOutput(): Inventory? = getInventory(if (multiblock.mirrored) -2 else +2, 0, 1)

	private var resultHash: Int? = null

	companion object {
		private const val POWER_USAGE_PER_INGREDIENT = 15

		private val recipeCache: LoadingCache<List<BukkitItemStack?>, Optional<RecipeHolder<CraftingRecipe>>> = CacheBuilder.newBuilder().build(CacheLoader.from { items ->
			requireNotNull(items)
			val level = Bukkit.getWorlds().first().minecraft
			val input = CraftingInput.of(3, 3, items.map(CraftItemStack::asNMSCopy))

			// Get results for the recipe
			CrafterBlock.getPotentialResults(level, input)
		})
	}

	override fun tick() {
		val inputInventory: Inventory = getInput() ?: return sleepWithStatus(Component.text("Not Intact", NamedTextColor.RED), 50)
		val recipeHolder: Inventory = getRecipeHolder() ?: return sleepWithStatus(Component.text("Not Intact", NamedTextColor.RED), 50)
		val output: Inventory = getOutput() ?: return sleepWithStatus(Component.text("Not Intact", NamedTextColor.RED), 50)

		// material data of each item in the recipe holder, used as the crafting transportNetwork
		val grid: List<BukkitItemStack?> = recipeHolder.contents.toList()

		val startPower = powerStorage.getPower()
		if (startPower < POWER_USAGE_PER_INGREDIENT) return sleepWithStatus(Component.text("Low Power", NamedTextColor.RED), 50)

		val recipe = recipeCache.get(grid).getOrNull() ?: return sleepWithStatus(Component.text("Invalid Recipe", NamedTextColor.RED), 50)
		val input = CraftingInput.of(3, 3, grid.map(CraftItemStack::asNMSCopy))

		// result item of this recipe
		val result = recipe.value.assemble(input, world.minecraft.registryAccess()).asBukkitCopy()
		val remainingResults = recipe.value.getRemainingItems(input).map(NMSItemStack::asBukkitCopy)

		val powerUsage = grid.filterNotNull().distinct().count() * POWER_USAGE_PER_INGREDIENT
		var power = startPower

		val references = mutableMapOf<ItemStack, AvailableItemInformation>()
		getReferencesFromItem(inputInventory, references)

		try {
			for (iteration in (1..multiblock.craftingIterations)) {
				if (power < powerUsage) {
					sleepWithStatus(Component.text("Low Power", NamedTextColor.RED), 50)
					break
				}

				val consumed = runCraftingIteration(powerUsage, grid, references, output, result, remainingResults)
				if (consumed == -1) break // Inventory full

				power -= consumed
			}
		}
		catch (e: Throwable) {
			IonServer.slF4JLogger.warn("Auto crafter execution threw exception!")
			e.printStackTrace()
		}
		finally {
			if (startPower != power) {
				powerStorage.setPower(power)
				val newHash = result.hashCode()

				if (resultHash != newHash) {
					// Skip re-computing the display name, small but adds up with big factories
					resultHash = newHash
					// Nothing crafted, could be temporary resource shortage, pause for shorter time period
					statusManager.setStatus(result.displayName())
				}
			} else {
				sleepWithStatus(Component.text("Sleeping"), 50)
			}
		}
	}

	private fun getReferencesFromItem(inventory: Inventory, items: MutableMap<ItemStack, AvailableItemInformation>) {
		for ((index, item: ItemStack?) in inventory.contents.withIndex()) {
			if (item == null || item.type.isEmpty) continue
			val information = items.getOrPut(item.asOne()) { AvailableItemInformation(AtomicInteger(), mutableListOf()) }

			information.amount.addAndGet(item.amount)
			information.references.add(ItemReference(inventory as CraftInventory, index))
		}
	}

	/**
	 * Runs an iteration of crafting, the number of iterations depends on the tier of them multiblock
	 * Returns power used
	 **/
	private fun runCraftingIteration(
		powerUsage: Int,
		grid: List<BukkitItemStack?>,

		inputItemReferences: Map<ItemStack, AvailableItemInformation>,
		outputInventory: Inventory,

		result: BukkitItemStack,
		remainingResults: List<BukkitItemStack>
	): Int {
		var insufficientIngredients = false

		val consumption = mutableMapOf<ItemStack, AtomicInteger>()

		// for each slot in the crafting grid,
		ingredientLoop@
		for (ingredient: BukkitItemStack? in grid) {
			// if it's not null,
			if (ingredient == null) continue

			if (!checkAndDrecrementAvailableItems(ingredient.asOne(), consumption, inputItemReferences)) {
				insufficientIngredients = true
				break
			}
		}

		// stop iterating if not all the ingredients were found
		if (insufficientIngredients) {
			return 0
		}

		if (!canAddAll(outputInventory, listOf(result, *remainingResults.toTypedArray()))) {
			resultHash = null
			sleepWithStatus(Component.text("Output Full", NamedTextColor.RED), 100)
			return -1
		}

		outputInventory.addItem(result.clone(), *remainingResults.toTypedArray())

		for ((itemType, amount) in consumption) {
			ShipFactoryTask.consumeItemFromReferences(inputItemReferences[itemType]!!.references, amount.get())
		}

		return powerUsage
	}

	private fun checkAndDrecrementAvailableItems(itemType: ItemStack, consumption: MutableMap<ItemStack, AtomicInteger>, inputItemReferences: Map<ItemStack, AvailableItemInformation>): Boolean {
		val amount = inputItemReferences[itemType] ?: return false

		if (amount.amount.get() < 1) return false
		amount.amount.decrementAndGet()

		consumption.getOrPut(itemType) { AtomicInteger() }.incrementAndGet()
		return true
	}

	override fun loadFromSign(sign: Sign) {
		migrateLegacyPower(sign)
		multiblock.signText.withIndex().forEach { sign.front().line(it.index, it.value.orEmpty()) }
	}
}
