package net.horizonsend.ion.server.features.multiblock.autocrafter

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getStateIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.minecraft.core.NonNullList
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.crafting.RecipeType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

private const val POWER_USAGE_PER_INGREDIENT = 15

abstract class AutoCrafterMultiblock(
	tierText: String,
	private val tierMaterial: Material,
	private val iterations: Int,
) : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	override val name = "autocrafter"
	override val requiredPermission: String? = "ion.multiblock.autocrafter"

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-2).type(tierMaterial)
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
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
				x(+0).craftingTable()
				x(+1).titaniumBlock()
				x(+2).extractor()
			}

			y(+0) {
				x(-2).anyPipedInventory()
				x(-1).endRod()
				x(+0).anyType(Material.DISPENSER, Material.DROPPER)
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

	override val signText: Array<Component?> = createSignText(
		line1 = "&aAuto",
		line2 = "&6Crafter",
		line3 = null,
		line4 = tierText
	)

	private fun getInput(sign: Sign): InventoryHolder? {
		val forward = sign.getFacing().oppositeFace
		val left = forward.rightFace.oppositeFace
		val x = sign.x + forward.modX * 2 + left.modX * 2
		val y = sign.y + forward.modY * 2 + left.modY * 2
		val z = sign.z + forward.modZ * 2 + left.modZ * 2
		return getStateIfLoaded(sign.world, x, y, z) as? InventoryHolder
	}

	private fun getRecipeHolder(sign: Sign): InventoryHolder? {
		val forward = sign.getFacing().oppositeFace
		val x = sign.x + forward.modX * 2
		val y = sign.y + forward.modY * 2
		val z = sign.z + forward.modZ * 2
		return getStateIfLoaded(sign.world, x, y, z) as? InventoryHolder
	}

	private fun getOutput(sign: Sign): InventoryHolder? {
		val forward = sign.getFacing().oppositeFace
		val right = forward.rightFace
		val x = sign.x + forward.modX * 2 + right.modX * 2
		val y = sign.y + forward.modY * 2 + right.modY * 2
		val z = sign.z + forward.modZ * 2 + right.modZ * 2
		return getStateIfLoaded(sign.world, x, y, z) as? InventoryHolder
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isCancelled = false
		event.isBurning = false
		event.burnTime = 20

		if (furnace.inventory.smelting?.type != Material.PRISMARINE_CRYSTALS) return
		if (furnace.inventory.fuel?.type != Material.PRISMARINE_CRYSTALS) return

		val input: InventoryHolder = getInput(sign) ?: return
		val recipeHolder: InventoryHolder = getRecipeHolder(sign) ?: return
		val output: InventoryHolder = getOutput(sign) ?: return

		// material data of each item in the recipe holder, used as the crafting grid
		val grid: List<ItemStack?> = recipeHolder.inventory.map { it }

		var power = PowerMachines.getPower(sign, fast = true)

		// result item of this recipe
		val result: ItemStack = recipeCache[grid].orElse(null)?.clone() ?: run {
			return
		}

		val inputInventory = input.inventory

		val powerUsage = grid.filterNotNull().distinct().count() * POWER_USAGE_PER_INGREDIENT

		try {
			for (iteration in (1..iterations)) {
				if (power < powerUsage) {
					return
				}

				val removeSlots = mutableListOf<Int>() // can be multiple times per slot, so list, not set
				var requiredIngredients = 0
				var matchedIngredients = 0

				// for each slot in the crafting grid,
				// if it's not null,
				// increment required ingredients to keep track of how many are needed,
				// and loop through the input inventory,
				// if an item's data matches the ingredient,
				// flag that slot for removal,a
				// increment matched ingredients,
				// and move on to the next ingredient
				ingredientLoop@
				for (ingredient: ItemStack? in grid) {
					if (ingredient != null) {
						requiredIngredients++
						for ((index: Int, item: ItemStack?) in inputInventory.withIndex()) {
							// if it matches AND we haven't already taken too much from it, use it
							if (item?.isSimilar(ingredient) == true && item.amount >= removeSlots.count { it == index } + 1) {
								removeSlots += index
								matchedIngredients++
								continue@ingredientLoop
							}
						}
					}
				}

				// stop iterating if not all of the ingredients were found
				if (matchedIngredients != requiredIngredients) {
					return
				}

				val remaining: HashMap<Int, ItemStack> = output.inventory.addItem(result)

				if (remaining.isNotEmpty()) {
					val added = result.amount - remaining.values.sumOf { it.amount }
					check(added >= 0)
					if (added > 0) {
						output.inventory.removeItem(result.clone().apply { amount = added })
					}
					return
				}

				power -= powerUsage

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
				for (index in removeSlots) {
					// since AFAIK recipes only call for one item per ingredient, just decrement the amount
					// it will automatically remove the item if the amount hits 0
					input.inventory.getItem(index)!!.amount--
				}
			}
		} finally {
			PowerMachines.setPower(sign, power)
		}
	}

	companion object {															  /* Items */
		private val itemsField = CraftingContainer::class.java.getDeclaredField("c").apply { isAccessible = true }
		private fun getItems(inventoryCrafting: CraftingContainer): NonNullList<NMSItemStack> {
			@Suppress("UNCHECKED_CAST")
			return itemsField.get(inventoryCrafting) as NonNullList<NMSItemStack>
		}

		private val recipeCache: LoadingCache<List<ItemStack?>, Optional<ItemStack>> = CacheBuilder.newBuilder().build(
			CacheLoader.from { items ->
				requireNotNull(items)
				val inventoryCrafting = CraftingContainer(
					object : AbstractContainerMenu(null as MenuType<*>?, -1) {
						override fun quickMoveStack(player: Player, slot: Int): NMSItemStack = NMSItemStack.EMPTY
						override fun stillValid(player: Player): Boolean = false
						override fun getBukkitView(): InventoryView = throw NotImplementedError()
					},
					3,
					3
				)

				val inventoryItems: NonNullList<NMSItemStack> = getItems(inventoryCrafting)

				for ((index: Int, material: ItemStack?) in items.withIndex()) {
					val item: NMSItemStack = if (material != null) CBItemStack.asNMSCopy(material) else NMSItemStack.EMPTY
					inventoryItems[index] = item
				}

				val level = Bukkit.getWorlds().first().minecraft

				val result = MinecraftServer.getServer().recipeManager
					.getRecipeFor(RecipeType.CRAFTING, inventoryCrafting, level)
					.map { recipeCrafting -> recipeCrafting.assemble(inventoryCrafting, level.registryAccess()) }
					.getOrNull()

				return@from Optional.ofNullable(result?.asBukkitCopy())
			}
		)
	}
}

private typealias CBItemStack = CraftItemStack
private typealias NMSItemStack = net.minecraft.world.item.ItemStack
