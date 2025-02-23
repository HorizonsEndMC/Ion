package net.horizonsend.ion.server.features.multiblock.type.autocrafter

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.server.IonServer
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
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.level.block.CrafterBlock
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.inventory.CraftItemStack as CBItemStack
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger

private const val POWER_USAGE_PER_INGREDIENT = 15

abstract class AutoCrafterMultiblock(
	tierText: Component,
	private val tierMaterial: Material,
	private val iterations: Int,
) : Multiblock(), EntityMultiblock<AutoCrafterMultiblock.AutoCrafterEntity>, DisplayNameMultilblock {
	override val name = "autocrafter"
	override val requiredPermission: String? = "ion.multiblock.autocrafter"
	open val mirrored: Boolean = false

	abstract val maxPower: Int

	override val displayName: Component = ofChildren(tierText, text(" Auto Crafter"))
	override val description: Component get() = text("Executes the recipe outlined in the center dropper. Input items are consumed to craft the output.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).powerInput()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}

			y(+0) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}
		}

		z(+1) {
			y(-1) {
				x(-2).anyGlass() // input pipe
				x(-1).titaniumBlock()
				x(+0).extractor()
				x(+1).titaniumBlock()
				x(+2).extractor()
			}

			y(+0) {
				x(-2).anyPipedInventory()
				x(-1).endRod()
				x(+0).anyType(Material.DISPENSER, Material.DROPPER, alias= "dispenser or dropper")
				x(+1).endRod()
				x(+2).anyPipedInventory()
			}
		}

		z(+2) {
			y(-1) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}

			y(+0) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}
		}
	}

	override val signText: Array<Component?> = arrayOf(
		ofChildren(text("Auto ", GRAY), text("Crafter", AQUA)),
		tierText,
		null,
		null
	)

	companion object {
		private val recipeCache: LoadingCache<List<ItemStack?>, Optional<ItemStack>> = CacheBuilder.newBuilder().build(
			CacheLoader.from { items ->
				requireNotNull(items)
				val level = Bukkit.getWorlds().first().minecraft
				val input = CraftingInput.of(3, 3, items.map(CBItemStack::asNMSCopy))

				// Get results for the recipe
				CrafterBlock.getPotentialResults(level, input).map { recipe -> recipe.value.assemble(input, level.registryAccess()).asBukkitCopy() }
			}
		)
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): AutoCrafterEntity {
		return AutoCrafterEntity(data, manager, this, x, y, z, world, structureDirection)
	}

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

		private fun getInput(): Inventory? = getInventory(-2, 0, 1)
		private fun getRecipeHolder(): Inventory? = getInventory(0, 0, 1)
		private fun getOutput(): Inventory? = getInventory(+2, 0, 1)

		private var resultHash: Int? = null

		override fun tick() {
			val inputInventory: Inventory = getInput() ?: return sleepWithStatus(text("Not Intact", RED), 500)
			val recipeHolder: Inventory = getRecipeHolder() ?: return sleepWithStatus(text("Not Intact", RED), 500)
			val output: Inventory = getOutput() ?: return sleepWithStatus(text("Not Intact", RED), 500)

			// material data of each item in the recipe holder, used as the crafting transportNetwork
			val grid: List<ItemStack?> = recipeHolder.contents.toList()

			val startPower = powerStorage.getPower()
			if (startPower < POWER_USAGE_PER_INGREDIENT) return sleepWithStatus(text("Low Power", RED), 250)

			// result item of this recipe
			val result = recipeCache[grid].orElse(null)?.clone()

			if (result == null) {
				sleepWithStatus(text("Invalid Recipe", RED), 200)
				return
			}

			val powerUsage = grid.filterNotNull().distinct().count() * POWER_USAGE_PER_INGREDIENT
			var power = startPower

			try {
				for (iteration in (1..multiblock.iterations)) {
					if (power < powerUsage) {
						sleepWithStatus(text("Low Power", RED), 150)
						break
					}

					val consumed = runCraftingIteration(powerUsage, grid, inputInventory, output, result)
					if (consumed == -1) break

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
					tickingManager.sleepForTicks(200)
				}
			}
		}

		/**
		 * Runs an iteration of crafting, the number of iterations depends on the tier of them multiblock
		 * Returns power used
		 **/
		private fun runCraftingIteration(
			powerUsage: Int,
			grid: List<ItemStack?>,
			inputInventory: Inventory,
			outputInventory: Inventory,
			result: ItemStack
		): Int {
			// Slots to amounts.
			val removeSlots = mutableMapOf<Int, AtomicInteger>()

			var requiredIngredients = 0
			var matchedIngredients = 0

			// for each slot in the crafting grid,
			ingredientLoop@
			for (ingredient: ItemStack? in grid) {
				// if it's not null,
				if (ingredient == null) continue

				// increment required ingredients to keep track of how many are needed,
				requiredIngredients++

				// Loop through the input inventory
				for ((index: Int, item: ItemStack?) in inputInventory.withIndex()) {
					// if it matches AND we haven't already taken too much from it, use it
					if (item == null) continue

					// if an item's data matches the ingredient,
					if (!item.isSimilar(ingredient)) continue

					// Boxed int to keep track
					val atomic = removeSlots.getOrPut(index) { AtomicInteger() }

					// And has enough
					if (item.amount < atomic.get() + 1) continue

					// flag that slot for removal,
					atomic.incrementAndGet()

					// Increment matched ingredient.
					// This will only be called once per ingredient, since at this point it will continue onto the next one.
					matchedIngredients++

					// and move on to the next ingredient
					continue@ingredientLoop
				}
			}

			// stop iterating if not all the ingredients were found
			if (matchedIngredients != requiredIngredients) {
				return 0
			}

			val remaining: HashMap<Int, ItemStack> = outputInventory.addItem(result.clone())

			if (remaining.isNotEmpty()) {
				// Only 1 ingredient is added, it will be the 0 index of the param.
				val notAdded = remaining[0]!!.amount
				check(notAdded >= 0)

				// Remove the amount that was actually added, since the ingredients will not be consumed
				val toRemove = result.amount - notAdded
				outputInventory.removeItem(result.asQuantity(toRemove))

				resultHash = null
				sleepWithStatus(text("Output Full", RED), 100)
				return -1
			}

			// below code leaves incomplete stacks, but might be faster
			/*// attempt to add the item to the output inventory (we already checked that we can remove it from the input)
			var fit = false
			for ((index: Int, item: ItemStack?) in output.inventory.contents.withIndex()) {
				if (item == null) {
					output.inventory.setItem(index, result)
					fit = true
					break
				} else if(item.isSimilar(result) && item.amount + result.amount <= item.maxStackSize) {
					item.amount += result.amount
					fit = true
					break
				}
			}

			if (!fit) {
				return
			}*/

			// remove the items
			for ((index, count) in removeSlots) {
				// it will automatically remove the item if the amount hits 0
				inputInventory.getItem(index)!!.amount -= count.get()
				println("Consuming ${count.get()}")
			}

			return powerUsage
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
			multiblock.signText.withIndex().forEach { sign.front().line(it.index, it.value.orEmpty()) }
		}
	}
}

abstract class AutoCrafterMultiblockMirrored(
	tierText: Component,
	private val tierMaterial: Material,
	iterations: Int,
) : AutoCrafterMultiblock(tierText, tierMaterial, iterations) {
	override val mirrored = true
	override val displayName: Component = ofChildren(tierText, text(" Auto Crafter (Mirrored)"))

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).powerInput()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}

			y(+0) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}
		}

		z(+1) {
			y(-1) {
				x(-2).extractor()
				x(-1).titaniumBlock()
				x(+0).extractor()
				x(+1).titaniumBlock()
				x(+2).anyGlass() // input pipe
			}

			y(+0) {
				x(-2).anyPipedInventory()
				x(-1).endRod()
				x(+0).anyType(Material.DISPENSER, Material.DROPPER, alias = "Dispenser or Dropper")
				x(+1).endRod()
				x(+2).anyPipedInventory()
			}
		}

		z(+2) {
			y(-1) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}

			y(+0) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
				x(+2).type(tierMaterial)
			}
		}
	}
}
