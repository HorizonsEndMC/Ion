package net.starlegacy.feature.multiblock.drills

import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.misc.CustomBlocks
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.LegacyItemUtils
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.isShulkerBox
import net.starlegacy.util.leftFace
import net.starlegacy.util.rightFace
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*
import kotlin.math.max

abstract class DrillMultiblock(tierText: String, val tierMaterial: Material) :
	Multiblock(),
	PowerStoringMultiblock,
	FurnaceMultiblock {
	override val inputComputerOffset = Vec3i(0, -1, 0)

	companion object {
		private val DISABLED = ChatColor.RED.toString() + "[DISABLED]"
		private val blacklist = EnumSet.of(
			Material.BARRIER,
			Material.BEDROCK
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
			return sign.getLine(3) != DISABLED
		}

		fun setUser(sign: Sign, player: String?) {
			sign.setLine(3, player ?: DISABLED)
			sign.update(false, false)
		}

		fun isBlacklisted(block: Block): Boolean {
			return blacklist.contains(block.type)
		}

		fun getOutput(sign: Block): Inventory {
			val direction = (sign.getState(false) as Sign).getFacing().oppositeFace
			return (
				sign.getRelative(direction)
					.getRelative(direction.leftFace)
					.getState(false) as InventoryHolder
				)
				.inventory
		}

		fun breakBlocks(
			sign: Sign,
			maxBroken: Int,
			toDestroy: MutableList<Block>,
			output: Inventory,
			player: Player,
			vararg people: Player = emptyArray()
		): Int {
			var broken = 0

			for (block in toDestroy) {
				if (isBlacklisted(block)) {
					continue
				}

				val testEvent = BlockBreakEvent(block, player)
				testEvent.isDropItems = false
				if (!testEvent.callEvent()) {
					continue
				}

				val customBlock = CustomBlocks[block]
				var drops = if (customBlock == null) block.drops else listOf(*customBlock.getDrops())

				if (block.type.isShulkerBox) drops = listOf()

				for (item in drops) {
					if (!LegacyItemUtils.canFit(output, item)) {
						player.sendMessage(ChatColor.RED.toString() + "Not enough space.")
						people.forEach { it.sendMessage(ChatColor.RED.toString() + "Not enough space.") }

						setUser(sign, null)

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

	override val name = "drill"

	override val signText = createSignText(
		line1 = "&8Drill&6Module",
		line2 = tierText,
		line3 = null,
		line4 = null
	)

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyPipedInventory()
				x(+0).machineFurnace()
				x(+1).wireInputComputer()
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
		super<PowerStoringMultiblock>.onTransformSign(player, sign)
		sign.setLine(3, DISABLED)
	}

	override fun onFurnaceTick(event: FurnaceBurnEvent, furnace: Furnace, sign: Sign) {
		event.isBurning = false
		event.burnTime = 0
		val fuel = furnace.inventory.fuel
		val smelting = furnace.inventory.smelting
		if (fuel == null || smelting == null) return
		val thirdLine = sign.getLine(3)
		if (thirdLine == DISABLED) {
			return
		}
		val player = Bukkit.getPlayer(thirdLine)
		if (player == null) {
			setUser(sign, null)
			return
		}
		drillCount[player.uniqueId] = drillCount.getOrDefault(player.uniqueId, 0) + 1
		val drills = lastDrillCount.getOrDefault(player.uniqueId, 1)

		if (drills > 16) {
			player.sendMessage(ChatColor.RED.toString() + "You cannot use more than 16 drills at once!")
			return
		}

		if (!isEnabled(sign) || smelting.type != Material.PRISMARINE_CRYSTALS) {
			return
		}

		event.isCancelled = true
		val power = PowerMachines.getPower(sign, true)
		if (power == 0) {
			setUser(sign, null)
			player.sendMessage(
				String.format(
					"%sYour drill at %s ran out of power! It was disabled.",
					ChatColor.RED, sign.location.toVector()
				)
			)
			return
		}

		event.isBurning = false
		event.burnTime = 5
		furnace.cookTime = (-1000).toShort()
		event.isCancelled = false

		val toDestroy = getBlocksToDestroy(sign)

		val maxBroken = max(1, if (drills > 5) (5 + drills) / drills + 15 / drills else 10 - drills)

		val broken = breakBlocks(sign, maxBroken, toDestroy, getOutput(sign.block), player)

		val powerUsage = broken * 10
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
