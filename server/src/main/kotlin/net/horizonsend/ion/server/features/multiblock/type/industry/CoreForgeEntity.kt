package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.input.FutureInputResult
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.input.PotentiallyFutureResult
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.sendMessage
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BATTLECRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.LARGE_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MEDIUM_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MINI_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SMALL_REACTOR_CORE
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Color
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

	fun disable(success: Boolean) {
		if (!userManager.currentlyUsed()) return
		val player = userManager.getUserPlayer()
		if (success) player?.sendMessage(Component.text("Success!", HE_DARK_ORANGE))
		else player?.sendMessage(Component.text("Insufficient resources.", HE_MEDIUM_GRAY))
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
			CRUISER_REACTOR_CORE.getValue().constructItemStack() -> CoreRecipes.cruiserReactorRecipe
			BATTLECRUISER_REACTOR_CORE.getValue().constructItemStack() -> CoreRecipes.battlecruiserReactorRecipe
			else -> return player.userError("Something broke! Blame fell")
		}
		println(getInput())
		println(getOutput())
		println(getOrigin())
		val input: Inventory = getInput() ?: return disable(false)
		val output: Inventory = getOutput() ?: return disable(false)
		println("diddy")
		for (item in currentRecipe) {
			if (!input.contains(item.key, item.value)) {
				disable(false)
				return
			}
		} //Need a second loop to stop items being removed without checking if the input has all the items, thus failing the craft but still taking items
		for (item in currentRecipe) {
			val targetItem = item.key.asOne().apply{amount = item.value}
			input.removeItem(targetItem)
		}
		output.addItem(currentCore)
		disable(true)
		return
	}
}

