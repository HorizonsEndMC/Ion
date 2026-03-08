package net.horizonsend.ion.server.features.multiblock.type.economy

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.transport.inputs.IOData
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory

object RemoteSellerMultiblock : Multiblock(), EntityMultiblock<RemoteSellerMultiblock.RemoteSellerMultiblockEntity>, DisplayNameMultilblock, InteractableMultiblock {
	override val name = "remoteseller"

	override val signText = createSignText(
		line1 = "&5Remote",
		line2 = "&7Seller",
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Remote Seller")
	override val description: Component get() = text("Sells items from remote destinations.")

	override fun onSignInteract( sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = RemoteSellerMultiblock.getMultiblockEntity(sign) ?: return
		entity.startTask(player)
	}

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).powerInput()
				x(1).anyPipedInventory()
				x(2).ironBlock()
			}
			y(0) {
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.FORWARD, RelativeFace.RIGHT))
				x(0).anyGlass()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.FORWARD, RelativeFace.RIGHT))
				x(2).ironBlock()
			}
		}
		z(1) {
			y(-1) {
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-1).redstoneBlock()
				x(0).titaniumBlock()
				x(1).redstoneBlock()
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.FORWARD, RelativeFace.BACKWARD))
			}
			y(0) {
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-1).redstoneBlock()
				x(0).titaniumBlock()
				x(1).redstoneBlock()
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.FORWARD, RelativeFace.BACKWARD))
			}
			y(1) {
				x(1).anyWall()
			}
			y(2) {
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.UP, example = Material.END_ROD.createBlockData()))
			}
			y(3) {
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.DOWN, example = Material.END_ROD.createBlockData()))
			}
		}
		z(2) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).sponge()
				x(0).ironBlock()
				x(1).sponge()
				x(2).ironBlock()
			}
			y(0) {
				x(-2).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(2).ironBlock()
			}
		}
	}

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace,
	): RemoteSellerMultiblockEntity {
		return RemoteSellerMultiblockEntity(data, manager, x, y, z, world, structureDirection, this)
	}

	class RemoteSellerMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
		override val multiblock: RemoteSellerMultiblock = RemoteSellerMultiblock
	) : MultiblockEntity(manager, multiblock,  world, x, y, z, structureDirection) {
		override val ioData: IOData = none()
		val userManager: UserManagedMultiblockEntity.UserManager = UserManagedMultiblockEntity.UserManager(data, persistent = false)
		val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()
		private fun getInput(): Inventory? = getInventory(1, -1, 0)
		val isRunning get() = userManager.currentlyUsed()

		fun startTask(player: Player) {
			if (!ConfigurationFiles.featureFlags().economy) return
			if (isRunning) {
				player.userError("This machine is already in use!")
				return
			}
			if (!isAlive) return
			val inventory = getInput() ?: return
			userManager.setUser(player)
			if (!inventory.isEmpty) {
				for (item in inventory) {
					val sellPrice = Merchants.getPrice(GlobalCompletions.toItemString(item)) ?: 1.0
					Tasks.async { VAULT_ECO.depositPlayer(player, sellPrice) }
					inventory.remove(item)
				}
				player.information("Sold items.")
			}
			userManager.clear()
			return
		}
	}
}
