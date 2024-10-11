package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.button
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.machine.DecomposeTask
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity.UserManager
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
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

object DecomposerMultiblock : Multiblock(), NewPoweredMultiblock<DecomposerMultiblock.DecomposerEntity>, InteractableMultiblock {
	override val maxPower: Int = 75_000
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
	) : MultiblockEntity(manager, DecomposerMultiblock, x, y, z, world, structureDirection), PoweredMultiblockEntity, LegacyMultiblockEntity, StatusMultiblock, UserManagedMultiblockEntity {
		override val multiblock = DecomposerMultiblock
		override val powerStorage: PowerStorage = loadStoredPower(data)
		override val statusManager: StatusMultiblock.StatusManager = StatusMultiblock.StatusManager()
		override val userManager: UserManager = UserManager(data, false)

		val regionOrigin get() = getBlockRelative(1, 1, 1)

		private val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
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

		override fun onLoad() {
			displayHandler.update()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
