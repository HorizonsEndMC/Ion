package net.starlegacy.feature.gear

import java.util.EnumSet
import java.util.UUID
import java.util.stream.Collectors
import net.starlegacy.PLUGIN
import net.starlegacy.util.FENCE_TYPES
import net.starlegacy.util.LOG_TYPES
import net.starlegacy.util.WOOD_TYPES
import net.starlegacy.util.isLeaves
import net.starlegacy.util.isNetherWart
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.scheduler.BukkitRunnable

private const val INSTANT_CUT = false
private const val MAX_LOGS_PER_CUT = 200
var currentFellers: MutableList<UUID> = ArrayList()

class TreeCutter(private val player: Player, private val startBlock: Block) : BukkitRunnable() {
	companion object {
		val materials: Set<Material>

		init {
			val materialSet = EnumSet.noneOf(Material::class.java)
			materialSet.addAll(LOG_TYPES)
			materialSet.addAll(WOOD_TYPES)
			materialSet.addAll(FENCE_TYPES)
			materialSet.add(Material.CRIMSON_HYPHAE)
			materialSet.add(Material.WARPED_HYPHAE)
			materialSet.add(Material.WARPED_STEM)
			materialSet.add(Material.CRIMSON_STEM)
			materialSet.add(Material.WARPED_WART_BLOCK)
			materialSet.add(Material.NETHER_WART_BLOCK)
			materialSet.add(Material.SHROOMLIGHT)
			materials = materialSet
		}

		fun isApplicable(material: Material) = materials.contains(material)
	}

	private val comparisonBlockArray = ArrayList<String>()
	private val comparisonBlockArrayLeaves = ArrayList<String>()
	private var blocks: MutableList<Block> = ArrayList()
	private var indexed = 0
	private var loop = false

	private val isTree: Boolean
		get() = comparisonBlockArrayLeaves.size * 1.0 / (blocks.size * 1.0) > 0.3

	private fun runLoop(b1: Block, x1: Int, z1: Int) {
		for (x in -3..3) {
			for (y in -3..3) {
				for (z in -3..3) {
					if (x == 0 && y == 0 && z == 0)
						continue
					val b2 = b1.getRelative(x, y, z)
					val s = b2.x.toString() + ":" + b2.y + ":" + b2.z

					val type = b2.type
					if (type.isLeaves || type.isNetherWart && !comparisonBlockArrayLeaves.contains(s)) {
						comparisonBlockArrayLeaves.add(s)
						blocks.add(b2)
					}

					if (!isApplicable(type)) {
						continue
					}

					val searchSquareSize = 25
					if (b2.x > x1 + searchSquareSize || b2.x < x1 - searchSquareSize || b2
							.z > z1 + searchSquareSize || b2.z < z1 - searchSquareSize
					)
						break
					if (!comparisonBlockArray.contains(s)) {
						comparisonBlockArray.add(s)
						blocks.add(b2)
						this.runLoop(b2, x1, z1)
					}
				}
			}
		}
	}

	override fun run() {
		blocks.add(startBlock)
		runLoop(startBlock, startBlock.x, startBlock.z)
		if (isTree) {
			cutDownTree()
			//            stop();
		} else {
			object : BukkitRunnable() {

				override fun run() {
					val center = startBlock.location.add(0.5, 0.5, 0.5)
					for (stack in startBlock.drops)
						startBlock.world.dropItem(center, stack)
					startBlock.world.playEffect(center, Effect.STEP_SOUND, startBlock.type)
					startBlock.type = Material.AIR
					stop()
				}
			}.runTask(PLUGIN)
		}
	}

	private fun stop() {
		if (currentFellers.contains(player.uniqueId)) {
			currentFellers.remove(player.uniqueId)
		}
	}

	private fun cutDownTree() {
		if (!currentFellers.contains(player.uniqueId)) {
			currentFellers.add(player.uniqueId)
		}

		blocks = blocks.stream().sorted { b, b2 -> b2.y - b.y }.collect(Collectors.toList())
		val speed: Long = 1

		object : BukkitRunnable() {
			var blocksCut = 0

			override fun run() {
				if (INSTANT_CUT && !loop) {
					for (i in blocks.indices) {
						loop = true
						run()
					}
					this.cancel()
					return
				}

				if (!player.isOnline || blocks.size < indexed - 2) {
					this.cancel()
					return
				}

				val block = blocks[indexed++]

				val animationEvent = PlayerAnimationEvent(player, PlayerAnimationType.ARM_SWING)
				Bukkit.getPluginManager().callEvent(animationEvent)
				val event = BlockBreakEvent(block, player)
				Bukkit.getPluginManager().callEvent(event)

				if (!event.isCancelled) {
					val center = block.location.add(0.5, 0.5, 0.5)
					startBlock.world.playEffect(center, Effect.STEP_SOUND, block.type)

					for (drop in block.drops) {
						startBlock.world.dropItem(player.location, drop)
					}

					blocksCut++
					block.setType(Material.AIR, false)
				}

				if (blocks.size <= indexed || blocksCut >= MAX_LOGS_PER_CUT) {
					this.cancel()
				}
			}

			override fun cancel() {
				stop()
				super.cancel()
			}
		}.runTaskTimer(PLUGIN, 0L, speed)
	}
}
