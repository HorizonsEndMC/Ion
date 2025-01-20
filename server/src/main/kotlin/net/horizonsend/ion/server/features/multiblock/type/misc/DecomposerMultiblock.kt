package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.button
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.machine.DecomposeTask
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity.StatusManager
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity.UserManager
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.listener.misc.ProtectionListener.isRegionDenied
import net.horizonsend.ion.server.miscellaneous.utils.CHISELED_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object DecomposerMultiblock : Multiblock(), EntityMultiblock<DecomposerMultiblock.DecomposerEntity>, InteractableMultiblock {
	override val name: String = "decomposer"
	override val signText = createSignText(
		"&cDecomposer",
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		at(0, 0, 0).ironBlock()
		at(0, -1, -1).anyPipedInventory()
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return

		val entity = getMultiblockEntity(sign) ?: return
		entity.handleClick(player)
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): DecomposerEntity {
		return DecomposerEntity(data, manager, x, y, z, world, structureDirection)
	}

	const val MAX_LENGTH = 100
	const val BLOCKS_PER_SECOND = 100
	private val FRAME_MATERIAL = CHISELED_TYPES

	class DecomposerEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : SimplePoweredEntity(data, DecomposerMultiblock, manager, x, y, z, world, structureDirection, 75_000), LegacyMultiblockEntity, StatusMultiblockEntity, UserManagedMultiblockEntity {
		override val multiblock = DecomposerMultiblock
		override val statusManager: StatusManager = StatusManager()
		override val userManager: UserManager = UserManager(data, false)

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this) },
			{ StatusDisplayModule(it, statusManager) }
		).register()

		var currentTask: DecomposeTask? = null

		fun handleClick(player: Player) {
			if (userManager.currentlyUsed()) {
				if (userManager.getUserId() != player.uniqueId) return player.userError("Decomposer in use!")
				return player.sendMessage(ofChildren(text("Would you like to cancel?" ), button(text("Confirm", RED)) { currentTask?.cancel() }))
			}

			if (isRegionDenied(player, getOrigin().location)) return player.userError("You can't use that here!")
			if (powerStorage.getPower() < 10) return player.userError("Insufficient Power!")

			val width = checkArm(structureDirection.rightFace)
			val height = checkArm(BlockFace.UP)
			val depth = checkArm(structureDirection)

			if (width == 0 || height == 0 || depth == 0) {
				player.userError("Invalid decomposer! It contains zero blocks. Build frames with chiseled blocks to the right to outline the region.")
				return
			}

			userManager.setUser(player)
			currentTask = DecomposeTask(
				this,
				width,
				height,
				depth,
			)

			currentTask?.runTaskTimer(IonServer, 20L, 20L)

			player.success("Started Decomposer")
		}

		private fun checkArm(direction: BlockFace): Int {
			var dimension = 0
			var tempBlock = getOrigin()

			while (dimension < MAX_LENGTH) {
				tempBlock = tempBlock.getRelativeIfLoaded(direction) ?: return dimension

				if (!FRAME_MATERIAL.contains(tempBlock.type)) {
					return dimension
				}

				dimension++
			}

			return dimension
		}

		fun getStorage() = getInventory(0, -1, -1)

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
