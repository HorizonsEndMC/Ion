package net.horizonsend.ion.server.features.multiblock.type.drills

import net.horizonsend.ion.common.extensions.alertSubtitle
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.extensions.userErrorSubtitle
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.UserManagedMultiblockEntity.UserManager
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.EnumSet

abstract class DrillMultiblock(val tierText: String, val tierMaterial: Material) : Multiblock(), EntityMultiblock<DrillMultiblock.DrillMultiblockEntity>, InteractableMultiblock, DisplayNameMultilblock {
	override val description: Component = text("Drills blocks in a ${javaClass.simpleName.substringBefore("Mirrored").substringAfter("DrillMultiblockTier").toInt() + 2} block radius.")

	abstract val radius: Int
	abstract val coolDown: Int
	abstract val mirrored: Boolean
	abstract val maxPower: Int

	override val name = "drill"

	override val signText = createSignText(
		line1 = "&8Drill&6Module",
		line2 = tierText,
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = ofChildren(legacyAmpersand.deserialize(tierText), text(" Drill"), if (mirrored) text(" (Mirrored)") else empty())

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		val entity = getMultiblockEntity(sign) ?: return

		entity.handleClick(sign, player)
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			if (!mirrored) {
				y(+0) {
					x(-1).anyPipedInventory()
					x(+0).machineFurnace()
					x(+1).powerInput()
				}
			} else {
				y(+0) {
					x(-1).powerInput()
					x(+0).machineFurnace()
					x(+1).anyPipedInventory()
				}
			}
		}

		z(+1) {
			y(+0) {
				x(-1).type(tierMaterial)
				x(+0).redstoneBlock()
				x(+1).type(tierMaterial)
			}
		}

		z(+2) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).redstoneLamp()
				x(+1).anyGlassPane()
			}
		}
		z(+3) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.front().line(3, DISABLED)
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): DrillMultiblockEntity {
		return DrillMultiblockEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class DrillMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: DrillMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, multiblock.maxPower), UserManagedMultiblockEntity, SyncTickingMultiblockEntity, LegacyMultiblockEntity {
		override val tickingManager: TickingManager = TickingManager(interval = 5)
		override val userManager: UserManager = UserManager(data, persistent = true)
		override val displayHandler: TextDisplayHandler = standardPowerDisplay(this)

		override val inputsData: InputsData = InputsData.builder(this)
			.registerSignInputs()
			.addPowerInput(if (multiblock.mirrored) -1 else 1, 0, 0)
			.build()

		override fun onLoad() {
			world.ion.multiblockManager.register(this)
		}

		override fun handleRemoval() {
			world.ion.multiblockManager.deregister(this)
		}

		override fun onUnload() {
			world.ion.multiblockManager.deregister(this)
		}

		override fun tick() {
			if (!userManager.currentlyUsed()) return
			// Logout condition
			val player = userManager.getUserPlayer() ?: return disable()

			if (CombatTimer.isPvpCombatTagged(player)) {
				player.userError("Cannot enable drills while in combat")
				disable()
				return
			}

			if (!isIntact(false)) {
				disable()
				return
			}

			val drills = world.ion.multiblockManager[DrillMultiblockEntity::class].count { it.userManager.getUserId() == userManager.getUserId() }

			if (drills > 16) return player.userErrorAction("You cannot use more than 16 drills at once!")
			if (!isEnabled()) return

			val power = powerStorage.getPower()
			if (power == 0) {
				disable()
				return player.alertSubtitle("Your drill at $globalVec3i ran out of power! It was disabled.")
			}

			val inSpace = world.ion.hasFlag(WorldFlag.SPACE_WORLD)
			if (inSpace) tickingManager.sleepForTicks(15)

			val toDestroy = getBlocksToDestroy()

			// set to 1 block broken per furnace tick in space
			val maxBroken = if (!inSpace) 10 else 1

			val broken = breakBlocks(
				maxBroken,
				toDestroy,
				getInventory(if (multiblock.mirrored) +1 else -1, 0, 0) ?: return run {
					player.userError("Drill output inventory destroyed")
					disable()
				},
				{
					val testEvent = BlockBreakEvent(it, player)
					testEvent.isDropItems = false

					return@breakBlocks testEvent.callEvent()
				},
				{
					player.userErrorSubtitle("Not enough space.")
					disable()
				}
			)

			powerStorage.removePower(broken * 50)
		}

		fun handleClick(sign: Sign, player: Player) {
			val previousUser = userManager.getUserId()
			if (previousUser == null) {
				// Toggle on
				enable(player, sign)
				return
			}

			// Toggle off
			disable()
		}

		fun enable(player: Player, sign: Sign) {
			userManager.setUser(player)
			sign.front().line(3, text(player.name))
			sign.update(false, false)
		}

		fun disable() {
			val sign = getSign() ?: return

			userManager.clear()
			sign.front().line(3, DISABLED)
			sign.update(false, false)
		}

		private fun isEnabled(): Boolean {
			return userManager.currentlyUsed()
		}

		private fun getBlocksToDestroy(): MutableList<Block> {
			val toDestroy = getSquareRegion(offsetRight = 0, offsetUp = 0, offsetForward = 4, radius = multiblock.radius, depth = 1) {
				it.type.isAir || isBlacklisted(it)
			}

			val origin = getBlockRelative(0, 0, 4)
			toDestroy.sortBy { distanceSquared(it.x, it.y, it.z, origin.x, origin.y, origin.z) }

			return toDestroy
		}

		fun getOutput(): Inventory {
			return (getBlockRelative(0, -1, 0).getState(false) as InventoryHolder).inventory
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}

	companion object {
		private val DISABLED = text("[DISABLED]", RED)
		private val blacklist = EnumSet.of(
			Material.BARRIER,
			Material.BEDROCK,
			Material.VOID_AIR
		)

		fun isBlacklisted(block: Block): Boolean {
			return blacklist.contains(block.type)
		}

		fun breakBlocks(
			maxBroken: Int,
			toDestroy: MutableList<Block>,
			output: Inventory,
			canBuild: (Block) -> Boolean,
			cancel: () -> Unit
		): Int {
			var broken = 0

			for (block in toDestroy) {
				if (isBlacklisted(block)) {
					continue
				}

				val customBlock = CustomBlocks.getByBlock(block)
				var drops = customBlock?.drops?.getDrops(null, false) ?: if (block.type == Material.SNOW_BLOCK) listOf() else block.drops

				if (block.type.isShulkerBox) drops = listOf()
				if (block.type == Material.END_PORTAL_FRAME) drops = listOf(ItemStack(Material.END_PORTAL_FRAME))

				if (!canBuild(block)) {
					continue
				}

				for (item in drops) {
					if (!LegacyItemUtils.canFit(output, item)) {
						cancel()
						return broken
					}

					LegacyItemUtils.addToInventory(output, item)
				}

				val applyPhysics = block.type == Material.COBBLESTONE
				block.setType(Material.AIR, applyPhysics)

				broken++
				if (broken >= maxBroken) {
					break
				}
			}

			return broken
		}
	}
}
