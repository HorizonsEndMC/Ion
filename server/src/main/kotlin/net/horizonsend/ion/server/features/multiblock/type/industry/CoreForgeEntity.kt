package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.input.FutureInputResult
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.input.PotentiallyFutureResult
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BARGE_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BATTLECRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.LARGE_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MEDIUM_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MINI_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SMALL_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

abstract class CoreForgeEntity (
	data: PersistentMultiblockData,
	override val multiblock: CoreForgeMultiblock,
	manager: MultiblockManager,
	world: World,
	x: Int,
	y: Int,
	z: Int,
	structureDirection: BlockFace
	) : MultiblockEntity(
	manager,
	multiblock,
	world,
	x,
	y,
	z,
	structureDirection
	), DisplayMultiblockEntity {
	abstract val guiTitle: String
	abstract var targetCore: ItemStack
	val userManager: UserManagedMultiblockEntity.UserManager = UserManagedMultiblockEntity.UserManager(data, persistent = false)
	val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

	private fun getInput(): Inventory? = getInventory(-2, 1, 0)
	private fun getOutput(): Inventory? = getInventory(2, 1, 0)


	fun openMenu(player: Player) {
		CoreForgeGui(player, this).openGui()
	}

	fun changeTargetCore(selectedCore: ItemStack) {
		val newCore = selectedCore
		targetCore = newCore
	}


	val isRunning get() = userManager.currentlyUsed()

	fun enable(user: Player, gui: CoreForgeGui?) {
		if (userManager.currentlyUsed()) return

		Tasks.async {
			Tasks.sync {
				userManager.setUser(user)
				startTask(user, targetCore, gui)
			}
		}
	}

	fun openGui(player: Player) {
		CoreForgeGui(player, this).openGui()
	}

	fun disable(message: Component) {
		if (!userManager.currentlyUsed()) return
		val player = userManager.getUserPlayer()
		player?.sendMessage(message)
		userManager.clear()
	}


	fun checkEnableButton(user: Player): PotentiallyFutureResult {
		val future = FutureInputResult()

		Tasks.async {
			future.complete(InputResult.InputSuccess)
		}

		return future
	}
	fun startTask(player: Player, currentCore: ItemStack, gui: CoreForgeGui?) {
		val currentRecipe = when (currentCore) {
			MINI_REACTOR_CORE.getValue().constructItemStack() -> CoreRecipes.miniReactorRecipe
			SMALL_REACTOR_CORE.getValue().constructItemStack() -> CoreRecipes.smallReactorRecipe
			MEDIUM_REACTOR_CORE.getValue().constructItemStack() -> CoreRecipes.mediumReactorRecipe
			LARGE_REACTOR_CORE.getValue().constructItemStack() -> CoreRecipes.largeReactorRecipe
			else -> CoreRecipes.tier1ReactorRecipe
		}
		val input: Inventory = getInput() ?: return disable(Component.text("Incorrectly built core forge.", HE_DARK_ORANGE))
		val output: Inventory = getOutput() ?: return disable(Component.text("Incorrectly built core forge.", HE_DARK_ORANGE))

		if (currentCore.customItem?.key == LARGE_REACTOR_CORE ||
			currentCore.customItem?.key == BARGE_REACTOR_CORE ||
			currentCore.customItem?.key == BATTLECRUISER_REACTOR_CORE ||
			currentCore.customItem?.key == CRUISER_REACTOR_CORE){
			val playerRegion = Regions.findFirstOf<RegionDominionTerritory>(player.location) ?: return disable(
				Component.text(
					"You must be in your dominion territory to create a super capital reactor core!",
					HE_DARK_ORANGE
				)
			)
			if (playerRegion.nation != PlayerCache[player].nationOid) return disable(
				Component.text(
					"You must be in your dominion territory to create a super capital reactor core!",
					HE_DARK_ORANGE
				)
			)
		}
		val missingItems = mutableListOf<String>()

		for (item in currentRecipe) {
			val neededAmount = item.value
			val recipeItem = item.key.clone().apply { amount = 1 }

			if (recipeItem.customItem is CustomItem) {
				val total = input.storageContents
					.filterNotNull()
					.filter { it.customItem?.key?.key == recipeItem.customItem?.key?.key }
					.sumOf { it.amount }

				if (total < neededAmount) {
					val itemName = recipeItem.customItem?.key?.key ?: "Unknown"
					missingItems.add("$itemName x${neededAmount - total}")
				}
			} else {
				val total = input.storageContents
					.filterNotNull()
					.filter { it.isSimilar(recipeItem) }
					.sumOf { it.amount }

				if (total < neededAmount) {
					val itemName = recipeItem.itemMeta?.displayName()
						?.let { component -> net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component) }
						?.takeIf { it.isNotBlank() }
						?: recipeItem.type.name
							.split('_')
							.joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
					missingItems.add("$itemName x${neededAmount - total}")
				}
			}
		}

		if (missingItems.isNotEmpty()) {
			player.userError("Missing resources: ${missingItems.joinToString(", ")}")
			disable(Component.text("Insufficient resources.", HE_DARK_ORANGE))
			return
		}

		// second loop to remove items - only reached if all items are present
		for ((recipeItem, neededAmount) in currentRecipe) {
			var remaining = neededAmount
			val item = recipeItem.clone().apply { amount = 1 }
			val isCustomItem = item.customItem is CustomItem

			for (i in input.storageContents.indices) {
				val stack = input.storageContents[i] ?: continue
				if (isCustomItem && stack.customItem is CustomItem) { if (stack.customItem?.key?.key != item.customItem!!.key.key) continue }

				if (!stack.isSimilar(item)) continue

				val take = minOf(stack.amount, remaining)
				stack.amount -= take
				remaining -= take

				if (stack.amount <= 0) {
					input.storageContents[i] = null
				}
				if (remaining <= 0) break
			}
		}
		output.addItem(currentCore)
		disable(Component.text("Success!", HE_LIGHT_BLUE))
		return
	}
}

