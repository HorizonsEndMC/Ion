package net.horizonsend.ion.server.features.multiblock.type.drills

import net.horizonsend.ion.common.extensions.alertSubtitle
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.extensions.userErrorSubtitle
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.PowerStoringMultiblock
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.DRILL_USER
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.persistence.PersistentDataType
import java.util.EnumSet
import java.util.UUID
import kotlin.math.max

abstract class DrillMultiblock(tierText: String, val tierMaterial: Material) :
	Multiblock(),
	PowerStoringMultiblock,
	FurnaceMultiblock,
	InteractableMultiblock {

	companion object {
		private val DISABLED = text("[DISABLED]", RED)
		private val blacklist = EnumSet.of(
			Material.BARRIER,
			Material.BEDROCK,
			Material.VOID_AIR
		)

		private var lastDrillCount: Map<UUID, Int> = mutableMapOf()
		private var drillCount: MutableMap<UUID, Int> = mutableMapOf()

		init {
			// TODO: do something less stupid for this
			Tasks.syncRepeat(delay = 1, interval = 1) {
				lastDrillCount = drillCount
				drillCount = mutableMapOf()
			}
		}

		fun isEnabled(sign: Sign): Boolean {
			return sign.getSide(Side.FRONT).line(3) != DISABLED
		}

		fun setUser(sign: Sign, player: String?) {
			sign.getSide(Side.FRONT).line(3, player?.let { text(it) } ?: DISABLED)

			if (player == null) sign.persistentDataContainer.remove(DRILL_USER) else sign.persistentDataContainer.set(DRILL_USER, PersistentDataType.STRING, player)

			sign.update(false, false)
		}

		fun isBlacklisted(block: Block): Boolean {
			return blacklist.contains(block.type)
		}


		fun breakBlocks(
			sign: Sign,
			maxBroken: Int,
			toDestroy: MutableList<Block>,
			output: Inventory,
			canBuild: (Block) -> Boolean,
			cancel: (Sign) -> Unit
		): Int {
			var broken = 0

			for (block in toDestroy) {
				if (isBlacklisted(block)) {
					continue
				}

				val customBlock = CustomBlocks.getByBlock(block)
				var drops = customBlock?.drops?.getDrops(null, false) ?: if (block.type == Material.SNOW_BLOCK) listOf() else block.drops

				if (block.type.isShulkerBox) drops = listOf()

				if (!canBuild(block)) {
					continue
				}

				for (item in drops) {
					if (!LegacyItemUtils.canFit(output, item)) {
						cancel(sign)

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

	abstract val radius: Int

	abstract val coolDown: Int

	abstract val mirrored: Boolean

	override val name = "drill"

	override val signText = createSignText(
		line1 = "&8Drill&6Module",
		line2 = tierText,
		line3 = null,
		line4 = null
	)

	fun getOutput(sign: Block): Inventory {
		val direction = (sign.getState(false) as Sign).getFacing().oppositeFace
		return if (!mirrored) {
			(sign.getRelative(direction)
				.getRelative(direction.leftFace)
				.getState(false) as InventoryHolder
					)
				.inventory
		} else {
			(sign.getRelative(direction)
				.getRelative(direction.rightFace)
				.getState(false) as InventoryHolder
					)
				.inventory
		}
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return

		val furnace = event.clickedBlock!!.getRelative(sign.getFacing().oppositeFace).getState(false) as? Furnace
			?: return

		if (furnace.inventory.let { it.fuel == null || it.smelting?.type != Material.PRISMARINE_CRYSTALS }) {
			event.player.userErrorAction(
				"You need Prismarine Crystals in both slots of the furnace!"
			)
			return
		}

		if (CombatTimer.isPvpCombatTagged(player)) {
			player.userError("Cannot enable drills while in combat")
			return
		}

		val user = when {
			isEnabled(sign) -> null
			else -> event.player.name
		}

		setUser(sign, user)
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			if (!mirrored) {
				y(+0) {
					x(-1).anyPipedInventory()
					x(+0).machineFurnace()
					x(+1).wireInputComputer()
				}
			} else {
				y(+0) {
					x(-1).wireInputComputer()
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
		sign.getSide(Side.FRONT).line(3, DISABLED)
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 0

		val fuel = furnace.inventory.fuel
		val smelting = furnace.inventory.smelting
		if (fuel == null || smelting == null) return

		val pdcUser = sign.persistentDataContainer.get(DRILL_USER, PersistentDataType.STRING) ?: return
		val player = Bukkit.getPlayer(pdcUser)

		if (player == null) {
			setUser(sign, null)
			return
		}

		/*
		if (SpaceWorlds.contains(furnace.world) && !furnace.world.name.contains("plots", ignoreCase = true)) {
			player.userError("Starship drills are not optimized for use in outer space! The starship drill was not enabled.")
			setUser(sign, null)
			return
		}
		 */

		drillCount[player.uniqueId] = drillCount.getOrDefault(player.uniqueId, 0) + 1
		val drills = lastDrillCount.getOrDefault(player.uniqueId, 1)

		if (drills > 16) {
			player.userErrorAction("You cannot use more than 16 drills at once!")
			return
		}

		if (!isEnabled(sign) || smelting.type != Material.PRISMARINE_CRYSTALS) {
			return
		}

		event.isCancelled = true
		val power = PowerMachines.getPower(sign, true)
		if (power == 0) {
			setUser(sign, null)
			player.alertSubtitle("Your drill at ${sign.location.toVector()} ran out of power! It was disabled.")
			return
		}

		val inSpace = furnace.world.ion.hasFlag(WorldFlag.SPACE_WORLD)

		event.isBurning = false
		event.burnTime = if (!inSpace) 5 else 20
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		val toDestroy = getBlocksToDestroy(sign)

		// set to 1 block broken per furnace tick in space
		val maxBroken = if (!inSpace) {
			max(1, if (drills > 5) (5 + drills) / drills + 15 / drills else 10 - drills)
		} else 1

		val broken = breakBlocks(
			sign,
			maxBroken,
			toDestroy,
			getOutput(sign.block),
			{
				val testEvent = BlockBreakEvent(it, player)
				testEvent.isDropItems = false

				return@breakBlocks testEvent.callEvent()
			},
			{
				player.userErrorSubtitle("Not enough space.")

				setUser(sign, null)
			}
		)

		val powerUsage = broken * 50
		PowerMachines.setPower(sign, power - powerUsage, true)
	}

	private fun getBlocksToDestroy(sign: Sign): MutableList<Block> {
		val direction = sign.getFacing().oppositeFace

		val right = direction.rightFace
		val center = sign.block.getRelative(direction, 5)

		val toDestroy = mutableListOf<Block>()

		for (h in -this.radius..this.radius) {
			for (v in -this.radius..this.radius) {
				val block = center.getRelative(right, h).getRelative(UP, v)
				if (block.type == Material.AIR) continue
				if (block.type == Material.BEDROCK) continue
				toDestroy.add(block)
			}
		}

		toDestroy.sortBy { it.location.distanceSquared(center.location) }

		return toDestroy
	}
}
