package net.starlegacy.feature.transport

import co.aikar.timings.Timing
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.horizonsend.ion.server.IonServer
import net.starlegacy.SLComponent
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.feature.multiblock.areashield.AreaShield
import net.starlegacy.util.ADJACENT_BLOCK_FACES
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getBlockDataSafe
import net.starlegacy.util.getBlockTypeSafe
import net.starlegacy.util.getStateIfLoaded
import net.starlegacy.util.matchesAxis
import net.starlegacy.util.orNull
import net.starlegacy.util.randomEntry
import net.starlegacy.util.time
import net.starlegacy.util.timing
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import java.util.Optional
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.math.min

object Wires : SLComponent() {
	val INPUT_COMPUTER_BLOCK = Material.NOTE_BLOCK

	private lateinit var thread: ExecutorService

	private val completionTiming: Timing = timing("Wire Completion")
	private val powerMachineFindingTiming = timing("Power Machine Finding")
	private val powerMachineUpdateTiming = timing("Power Machine Update")

	// region cache stuff for sync code
	// should only be updated from the checkComputers
	private val powerSignUpdateCache = CacheBuilder.newBuilder()
		.build<Sign, Int>(
			CacheLoader.from { sign ->
				checkNotNull(sign)
				PowerMachines.getPower(sign, fast = true)
			}
		)

	data class CachedPowerStore(val multiblock: PowerStoringMultiblock, val sign: Sign)

	private val multiblockCache = CacheBuilder.newBuilder()
		.expireAfterWrite(5, TimeUnit.SECONDS)
		.build<Location, Optional<CachedPowerStore>>(
			CacheLoader.from { loc ->
				checkNotNull(loc)
				return@from powerMachineFindingTiming.time {
					for ((x, y, z) in offsets) {
						val state = getStateIfLoaded(loc.world, loc.blockX + x, loc.blockY + y, loc.blockZ + z)
						val sign = state as? Sign ?: continue
						val multiblock = Multiblocks[sign, true, false] as? PowerStoringMultiblock
							?: continue
						return@time Optional.of(CachedPowerStore(multiblock, sign))
					}
					return@time Optional.empty()
				}
			}
		)
	private val computerCheckQueue = ConcurrentLinkedQueue<() -> Unit>()

	//endregion

	private fun setCachedPower(sign: Sign, power: Int) = powerSignUpdateCache.put(sign, power)

	private val offsets = setOf(
		// most multiblocks have the sign a block up and out of the computer
		Vec3i(1, 1, 0), Vec3i(-1, 1, 0), Vec3i(0, 1, -1), Vec3i(0, 1, 1),
		// power cells have it on the block
		Vec3i(1, 0, 0), Vec3i(-1, 0, 0), Vec3i(0, 0, -1), Vec3i(0, 0, 1),
		// drills have it on a corner
		Vec3i(-1, 0, -1), Vec3i(1, 0, -1), Vec3i(1, 0, 1), Vec3i(-1, 0, 1)
	)

	override fun onEnable() {
		thread = Executors.newSingleThreadExecutor(Tasks.namedThreadFactory("sl-transport-wires"))

		scheduleUpdates()
	}

	/**
	 * Power updates can be slow. In fact, they're the only sync part of wires, and by far the most CPU-intensive.
	 * So, they're batched in a queue down below. It's easier to explain how it works with code,
	 * so just look at this file to get a feel for it.
	 *
	 * `powerUpdateRate` is the rate in ticks of the updates being batched.
	 * Higher values mean more updates being batched together and potentially better usage of caching.
	 * However, it also means larger delay, and could affect user usage.
	 */
	private fun scheduleUpdates() {
		val interval = transportConfig.wires.powerUpdateRate
		Tasks.syncRepeat(delay = interval, interval = interval) {
			val start = System.nanoTime()

			val maxTime = TimeUnit.MILLISECONDS.toNanos(transportConfig.wires.powerUpdateMaxTime)

			completionTiming.time {
				while (!computerCheckQueue.isEmpty() && System.nanoTime() - start < maxTime) {
					computerCheckQueue.poll().invoke()
				}

				if (System.nanoTime() - start > maxTime) {
					IonServer.slF4JLogger.warn("Power update took too long!")
				}
			}

			powerMachineUpdateTiming.time {
				for ((sign, power) in powerSignUpdateCache.asMap()) {
					PowerMachines.setPower(sign, power, fast = true)
				}
			}

			powerSignUpdateCache.invalidateAll()
		}
	}

	override fun onDisable() {
		if (::thread.isInitialized) thread.shutdown()
	}

	fun isAnyWire(material: Material) = when (material) {
		Material.END_ROD, Material.SPONGE, Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> true
		else -> false
	}

	fun startWireChain(world: World, x: Int, y: Int, z: Int, direction: BlockFace, computer: Vec3i?) {
		thread.submit {
			step(world, x, y, z, direction, computer, 0)
		}
	}

	private fun step(world: World, x: Int, y: Int, z: Int, direction: BlockFace, computer: Vec3i?, distance: Int) {
		if (distance > transportConfig.wires.maxDistance) {
			return
		}

		val nextX = x + direction.modX
		val nextY = y + direction.modY
		val nextZ = z + direction.modZ

		val nextType = getBlockTypeSafe(world, nextX, nextY, nextZ) ?: return

		val reverse = direction.oppositeFace // used for ensuring we're not going backwards when dealing w/ connectors

		val checkDirections = when (nextType) {
			Material.END_ROD -> setOf(direction)
			Material.SPONGE, Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> ADJACENT_BLOCK_FACES
			else -> return // if it's not one of the above blocks it's not a wire block, so end the wire chain
		}

		// directional wires go forward if possible, and don't go into sponges
		val isDirectional = nextType == Material.IRON_BLOCK || nextType == Material.REDSTONE_BLOCK

		val adjacentComputers = mutableSetOf<BlockFace>()
		val adjacentWires = mutableSetOf<BlockFace>()

		adjacentLoop@
		for (face: BlockFace in checkDirections) {
			if (face == reverse) continue

			val adjacentX = nextX + face.modX
			val adjacentY = nextY + face.modY
			val adjacentZ = nextZ + face.modZ

			val data = getBlockDataSafe(world, adjacentX, adjacentY, adjacentZ) ?: continue

			if (data.material == INPUT_COMPUTER_BLOCK) {
				adjacentComputers.add(face)
			} else if (canWiresTransfer(isDirectional, face, data)) {
				adjacentWires.add(face)
			}
		}

		// continue if there are no computers requiring main thread checks
		if (adjacentComputers.isEmpty()) {
			if (adjacentWires.isNotEmpty()) {
				val adjacentPipeDirection = pickDirection(isDirectional, adjacentWires, direction)
				step(world, nextX, nextY, nextZ, adjacentPipeDirection, computer, distance + 1)
			}
			return
		}

		// check computers on the main thread as their signs need to be accessed,
		// and tile entities aren't as easy to access in a thread-safe manner.
		// put it on the queue, see the top of the file for how that works.
		computerCheckQueue.offer {
			checkComputers(
				world = world, x = nextX, y = nextY, z = nextZ, isDirectional = isDirectional, direction = direction,
				computers = adjacentComputers, wires = adjacentWires, originComputer = computer, distance = distance + 1
			)
		}
	}

	/**
	 * @param isDirectional If the origin wire is a directional wire
	 * @param face The direction the origin wire was heading
	 * @param data The data of the next wire
	 */
	private fun canWiresTransfer(isDirectional: Boolean, face: BlockFace, data: BlockData): Boolean {
		return when (data.material) {
			// anything can go into end rod wires, but only if the rotation axis matches
			Material.END_ROD -> getWireRotation(data).matchesAxis(face)
			// anything can go into directional connectors
			Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> true
			// anything besides directional connectors can go into sponge wires
			Material.SPONGE -> !isDirectional
			// other stuff is a no
			else -> return false
		}
	}

	/**
	 * Should only be called in the update queue up top.
	 *
	 * @param world The world of the wire chain
	 * @param x The X-coordinate of the current spot in the wire chain
	 * @param y The Y-coordinate of the current spot in the wire chain
	 * @param z The Z-coordinate of the current spot in the wire chain
	 * @param isDirectional Whether the current wire block type is a directional connector
	 */
	private fun checkComputers(
		world: World,
		x: Int,
		y: Int,
		z: Int,
		isDirectional: Boolean,
		direction: BlockFace,
		computers: Set<BlockFace>,
		wires: Set<BlockFace>,
		originComputer: Vec3i?,
		distance: Int
	) {
		val validComputers = computers.asSequence()
			.mapNotNull { getStateIfLoaded(world, x + it.modX, y + it.modY, z + it.modZ) }
			.filter { it.type == INPUT_COMPUTER_BLOCK }
			.toList().shuffled(ThreadLocalRandom.current())

		if (validComputers.isNotEmpty()) {
			val originSign: Sign? = when {
				// if there's an origin computer, find its power machine, if it's not findable, end the chain
				originComputer != null -> multiblockCache[originComputer.toLocation(world)]
					.orNull()?.sign
					?: return

				else -> null
			}

			var originPower = when {
				originSign != null -> powerSignUpdateCache[originSign]
				else -> transportConfig.wires.solarPanelPower
			}

			// if it has no power then there is nothing to extract from it anymore
			if (originPower <= 0) {
				return
			}

			computerLoop@
			for (destination in validComputers) {
				val (destinationMultiblock, destinationSign) = multiblockCache[destination.location]
					.orNull() ?: continue@computerLoop

				// ensure we're not returning power to the same computer
				if (destinationSign.location == originSign?.location) {
					continue@computerLoop
				}

				val destinationPower = powerSignUpdateCache[destinationSign]
				val destinationPowerMax = destinationMultiblock.maxPower
				val destinationFreeSpace = destinationPowerMax - destinationPower

				val transferLimit = when (destinationMultiblock) {
					is AreaShield -> transportConfig.wires.maxShieldInput
					else -> transportConfig.wires.maxPowerInput
				}

				val amount: Int = min(transferLimit, min(originPower, destinationFreeSpace))

				setCachedPower(destinationSign, destinationPower + amount)

				originPower -= amount

				if (originSign != null) {
					setCachedPower(originSign, originPower)
				}

				if (originPower <= 0) {
					return // no more power to extract
				}
			}
		}

		// double check the wires to make sure they can still be pushed into
		val validWires = wires.filter {
			val data = getBlockDataSafe(world, x + it.modX, y + it.modY, z + it.modZ)
				?: return@filter false

			return@filter canWiresTransfer(isDirectional, direction, data)
		}.toSet()

		if (validWires.isEmpty()) return // end the chain if there's no more valid wires

		val newDirection = pickDirection(isDirectional, validWires, direction)

		thread.submit {
			step(world, x, y, z, newDirection, originComputer, distance + 1)
		}
	}

	private fun pickDirection(isDirectional: Boolean, adjacentWires: Set<BlockFace>, direction: BlockFace): BlockFace {
		return when {
			isDirectional && adjacentWires.contains(direction) -> direction
			else -> adjacentWires.randomEntry()
		}
	}

	private fun getWireRotation(data: BlockData): BlockFace = (data as Directional).facing
}
