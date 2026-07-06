package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.CauldronLevelChangeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitTask
import java.util.ArrayDeque

/*
 * =======================================================================================================
 * BEGIN: SIGN-LESS MULTIBLOCK RECOVERY SCANNER
 * =======================================================================================================
 *
 * When i first attempted signless, everything was going great after ironing out a few
 * bugs for small things like pistons moving a part of the block which ruined it even after being pulled back
 * but at the end of bug checking there was one i couldn't fix related to explosion reversal i used /setblock to simulate regenerating
 * a blown up block and sign-less multiblock doesn't register the blocks back as its needed with just the multiblock part so
 * this scanner is meant to recover dormant sign-less multiblocks on a timer of roughly 6 minutes equivalent
 * so that it happens a bit after explosion reversals interval. Normal Ion multiblocks have signs, their signs give the normal multiblock
 * system somewhere to store data and somewhere to look when a chunk loads. This dormant scanner is purely to re-register the sign-less
 * multiblocks that have fallen asleep due to whatever reasons that aren't covered already like the explosion reversal.
 *
 * The whitelist matters it's intended for normal sign multiblocks to not be touched by this scanning.
 * ========================================================================================================
 */

/**
 * A sign-less multiblock opts into the shared scanner by implementing this.
 * The detector should use a cheap anchor. For example, Advanced Hoppers use output inventories rather than checking every block in a chunk.
 */
interface SignlessMultiblockDetector {
	//Used in logs so a scanner problem can be traced to one detector.
	val detectorName: String

	/**
	 * Inspect one chunk that is already loaded by the server.
	 * @return how many previously inactive structures became active.
	 */
	fun scanLoadedChunk(chunk: Chunk): Int
}

//Server loaded chunks only
object SignlessMultiblockScanner : IonServerComponent() {
	/**
	 * Six minutes at 20 TPS which is shortly after explosion reversal.
		* At 90,000 loaded chunks, it starts at 13 chunks per tick because that is enough to finish within this window.
		* search "private fun processFullPass" for more info
	 */
	private const val PERIODIC_SCAN_INTERVAL_TICKS = 6 * 60 * 20

	/**
	 * This stops a huge queue from demanding unlimited work in one tick.
	 * Missing the deadline is better than creating a large lag spike.
	 * The scanner reports that clearly in its periodic-pass log line.
	 */
	private const val MAX_PERIODIC_CHUNKS_PER_TICK = 64

	//This is the scanner's own work budget per tick. A chunk can still be slow on its own, but once the budget is used the scanner leaves later chunks for another tick instead of piling on.
	private const val MAX_SCAN_WORK_NANOS_PER_TICK = 2_000_000L

	//Chunk coordinates are stored instead of Chunk objects. This avoids retaining a chunk object after the server unloads that chunk.
	private data class ChunkTarget(
		val world: World,
		val x: Int,
		val z: Int,
	)

	/**
	 * This is the report card for one startup or periodic full pass.
	 * It lets the live console show whether the scanner is meeting its target and whether any chunks are unusually expensive.
	 */
	private data class FullScanPass(
		val reason: String,
		val startedAtTick: Long,
		val deadlineTick: Long,
		var targetsQueued: Int = 0,
		var chunksScanned: Int = 0,
		var chunksSkippedUnloaded: Int = 0,
		var structuresActivated: Int = 0,
		var detectorCalls: Int = 0,
		var detectorNanos: Long = 0L,
		var slowestChunkNanos: Long = 0L,
		var maximumRequiredQuota: Int = 0,
		var timeBudgetYields: Int = 0,
		var deadlineWarningLogged: Boolean = false,
	)

	//This keeps scanTarget small and makes it clear what the caller gets back.
	private data class TargetResult(
		val scanned: Boolean,
		val activated: Int,
		val detectorCalls: Int,
		val detectorNanos: Long,
		val elapsedNanos: Long,
	)

	/**
	 * This is the entire sign-less multiblock whitelist.
	 *
	 * Right now Advanced Hoppers are the only detector. Future sign-less
	 * multiblocks can register here without changing normal multiblock code.
	 */
	private val detectors = linkedSetOf<SignlessMultiblockDetector>()

	//Deadline-driven queue used for startup and six-minute recovery passes.
	private val pendingFullScanTargets = ArrayDeque<ChunkTarget>()

	//Prevents one chunk appearing many times in the same full pass.
	private val queuedFullScanTargets = linkedSetOf<ChunkTarget>()

	private var serverTick = 0L
	private var nextPeriodicStartTick = PERIODIC_SCAN_INTERVAL_TICKS.toLong()
	private var fullPass: FullScanPass? = null
	private var scanTask: BukkitTask? = null

	override fun onEnable() {
		serverTick = 0L
		nextPeriodicStartTick = PERIODIC_SCAN_INTERVAL_TICKS.toLong()

		startFullPass("startup")

		scanTask = Tasks.syncRepeatTask(1L, 1L, ::tick)
	}

	override fun onDisable() {
		scanTask?.cancel()
		scanTask = null

		detectors.clear()
		pendingFullScanTargets.clear()
		queuedFullScanTargets.clear()
		fullPass = null
	}

	/**
	 * Add a detector to the whitelist.
	 * In normal startup order, the full pass already exists. This extra path is for a future detector that registers after startup has finished.
	 */
	fun register(detector: SignlessMultiblockDetector) {
		if (!detectors.add(detector)) return

		if (fullPass == null) {
			startFullPass("detector registration: ${detector.detectorName}")
		}
	}

	fun unregister(detector: SignlessMultiblockDetector) {
		detectors.remove(detector)
	}

	@EventHandler
	fun onWorldUnload(event: WorldUnloadEvent) {
	    //Remove work for the closing world. The scanner must not retain an old world reference after that world is gone.
		removeWorldTargets(
			event.world,
			pendingFullScanTargets,
			queuedFullScanTargets,
		)
	}

	private fun tick() {
		serverTick++

		//Full passes never overlap. If an earlier one takes too long, it gets to finish and report its result before another pass starts.
		if (fullPass == null && serverTick >= nextPeriodicStartTick) {
			startFullPass("periodic")
			nextPeriodicStartTick = serverTick + PERIODIC_SCAN_INTERVAL_TICKS
		}


		fullPass?.let(::processFullPass)

		val activePass = fullPass ?: return

		if (pendingFullScanTargets.isEmpty()) {
			finishFullPass(activePass)
			fullPass = null
			return
		}

		if (serverTick > activePass.deadlineTick && !activePass.deadlineWarningLogged) {
			activePass.deadlineWarningLogged = true

			log.warn(
				"Sign-less multiblock full scan '${activePass.reason}' missed its deadline: " +
					"${pendingFullScanTargets.size} chunks remain; " +
					"maximum required quota=${activePass.maximumRequiredQuota}; " +
					"time-budget yields=${activePass.timeBudgetYields}."
			)
		}
	}

	/**
	 * Process enough full-pass chunks to meet the pass deadline.
	 * For example: 90,000 remaining chunks with 7,200 ticks left needs
	 * ceil(90000 / 7200), which is 13 chunks this tick.
	 * 90000 is being used as the example because that's the higher end of the chunks loaded during spark peak survival player count
	 */
	private fun processFullPass(pass: FullScanPass) {
		if (detectors.isEmpty()) return

		val ticksRemaining = maxOf(1L, pass.deadlineTick - serverTick + 1L)
		val requiredQuota = (
			pendingFullScanTargets.size.toLong() + ticksRemaining - 1L
		).div(ticksRemaining).toInt()

		pass.maximumRequiredQuota = maxOf(pass.maximumRequiredQuota, requiredQuota)

		val quota = requiredQuota.coerceIn(1, MAX_PERIODIC_CHUNKS_PER_TICK)
		val detectorSnapshot = detectors.toList()
		val tickStartNanos = System.nanoTime()
		var processed = 0

		while (processed < quota && pendingFullScanTargets.isNotEmpty()) {
			//Always do the first chunk. After that, stop when the scanner hasused its own time budget for this tick.
			if (
				processed > 0 &&
				System.nanoTime() - tickStartNanos >= MAX_SCAN_WORK_NANOS_PER_TICK
			) {
				pass.timeBudgetYields++
				break
			}

			val target = pendingFullScanTargets.removeFirst()
			queuedFullScanTargets.remove(target)
			processed++

			val result = scanTarget(target, detectorSnapshot)

			if (!result.scanned) {
				pass.chunksSkippedUnloaded++
				continue
			}

			pass.chunksScanned++
			pass.structuresActivated += result.activated
			pass.detectorCalls += result.detectorCalls
			pass.detectorNanos += result.detectorNanos
			pass.slowestChunkNanos = maxOf(pass.slowestChunkNanos, result.elapsedNanos)
		}
	}

	/**
	 * Run every whitelisted detector against one chunk.
	 * A detector error is contained here so one future sign-less multiblock cannot stop every other detector from running.
	 */
	private fun scanTarget(
		target: ChunkTarget,
		detectorSnapshot: List<SignlessMultiblockDetector>,
	): TargetResult {
		if (!target.world.isChunkLoaded(target.x, target.z)) {
			return TargetResult(
				scanned = false,
				activated = 0,
				detectorCalls = 0,
				detectorNanos = 0L,
				elapsedNanos = 0L,
			)
		}

		val chunk = target.world.getChunkAt(target.x, target.z)
		val chunkStartNanos = System.nanoTime()

		var activated = 0
		var detectorCalls = 0
		var detectorNanos = 0L

		for (detector in detectorSnapshot) {
			val detectorStartNanos = System.nanoTime()

			try {
				activated += detector.scanLoadedChunk(chunk)
			} catch (exception: Exception) {
				log.error(
					"Sign-less detector '${detector.detectorName}' failed in " +
						"${target.world.name} chunk ${target.x}, ${target.z}",
					exception,
				)
			} finally {
				detectorCalls++
				detectorNanos += System.nanoTime() - detectorStartNanos
			}
		}

		return TargetResult(
			scanned = true,
			activated = activated,
			detectorCalls = detectorCalls,
			detectorNanos = detectorNanos,
			elapsedNanos = System.nanoTime() - chunkStartNanos,
		)
	}

	private fun startFullPass(reason: String) {
		check(fullPass == null) {
			"Attempted to start overlapping sign-less full scans."
		}

		val pass = FullScanPass(
			reason = reason,
			startedAtTick = serverTick,
			deadlineTick = serverTick + PERIODIC_SCAN_INTERVAL_TICKS,
		)

		fullPass = pass
		queueAllLoadedChunks(pass)
	}

	/**
	 * Take a snapshot of chunks loaded right now.
	 * The scanner does not ask Bukkit to load anything here. It simply queues
	 * chunks the server is already keeping active.
	 */
	private fun queueAllLoadedChunks(pass: FullScanPass) {
		for (world in Bukkit.getWorlds()) {
			for (chunk in world.loadedChunks) {
				queueFullChunk(chunk, pass)
			}
		}
	}

	private fun queueFullChunk(chunk: Chunk, pass: FullScanPass) {
		if (!chunk.isLoaded) return

		val target = ChunkTarget(chunk.world, chunk.x, chunk.z)

		if (queuedFullScanTargets.add(target)) {
			pendingFullScanTargets.addLast(target)
			pass.targetsQueued++
		}
	}

	private fun removeWorldTargets(
		world: World,
		pendingTargets: ArrayDeque<ChunkTarget>,
		queuedTargets: MutableSet<ChunkTarget>,
	) {
		val retained = pendingTargets.filter { target -> target.world != world }

		pendingTargets.clear()
		pendingTargets.addAll(retained)
		queuedTargets.removeAll { target -> target.world == world }
	}

	//Full passes log once when they finish. This is the line to inspect on the real server when checking 90,000-loaded-chunk behaviour.
	private fun finishFullPass(pass: FullScanPass) {
		val elapsedTicks = serverTick - pass.startedAtTick

		val averageDetectorMicros = if (pass.detectorCalls == 0) {
			0L
		} else {
			pass.detectorNanos / pass.detectorCalls / 1_000L
		}

		log.info(
			"Sign-less multiblock full scan '${pass.reason}' completed: " +
				"queued=${pass.targetsQueued}, scanned=${pass.chunksScanned}, " +
				"skipped-unloaded=${pass.chunksSkippedUnloaded}, " +
				"activated=${pass.structuresActivated}, elapsed-ticks=$elapsedTicks, " +
				"deadline-met=${serverTick <= pass.deadlineTick}, " +
				"max-required-quota=${pass.maximumRequiredQuota}, " +
				"time-budget-yields=${pass.timeBudgetYields}, " +
				"avg-detector-us=$averageDetectorMicros, " +
				"slowest-chunk-ms=${pass.slowestChunkNanos / 1_000_000.0}."
		)
	}
}

/*
 * ================================================================
 * END: SIGN-LESS MULTIBLOCK RECOVERY SCANNER
 * ================================================================
 *
 * Everything below is an actual sign-less multiblock implementation.
 *
 * Future sign-less multiblocks belong below this marker. They should implement
 * "interface SignlessMultiblockDetector" and register with SignlessMultiblockScanner.
 * =================================================================
 */

/*
 * ==================================================================
 * BEGIN: ADVANCED HOPPER
 * ==================================================================
 */

/**
 * A sign-less and deliberately non-pilotable item collector.
 * Structure:
 * y + 1: Chiseled Stone Bricks
 * y + 0: Empty cauldron, which is the origin, not to be confused with filled cauldrons which wont work but will still work if you empty them again.
 * y - 1: Any inventory holder, which is the output
 *
 * Collection area:
 * A 3x3 wide area centered around the origin block which starts at the block above the chiseled stone bricks and goes 16 high.
 */
object AdvancedHoppers : IonServerComponent(), SignlessMultiblockDetector {
	override val detectorName = "advanced hopper"

	/** Each active hopper checks for items once every 150 server ticks. */
	private const val COLLECTION_INTERVAL_TICKS = 150

	/**
	 * This is the memory record for a currently valid hopper.
	 * The phase gives each hopper its own place in the 150-tick cycle so all hoppers do not query nearby entities during the same server tick this is so it doesn't create lag spikes by doing all at once.
	 * Necessary for things like pistons and block states like cauldrons changing.
	 * It's because the code does not immediately test the sign-less structure for every affected block instead it puts each changed block’s world and coordinates into "pendingRefreshes" which is a
	 * "linkedSetOf" Because it is a set, the same location can only be queued once even if several events touch it in the same tick.
	 * Then one tick later it processes each saved position and checks the three possible hopper origins around that block. see "private fun refreshColumn"
	 */
	private data class ActiveHopper(
		val world: World,
		val x: Int,
		val y: Int,
		val z: Int,
		val phase: Int,
	) {
		val key: Long get() = toBlockKey(x, y, z)
	}

	/**
	 * Used to combine many event changes into one later refresh.
	 * This is just a small coordinate record for a block that needs checking later as keeping positions here avoids doing repeated structure checks.
	 */
	private data class BlockPosition(
		val world: World,
		val x: Int,
		val y: Int,
		val z: Int,
	)

	//Every active hopper, grouped by world and its origin block location.
	private val hoppersByWorld = mutableMapOf<World, MutableMap<Long, ActiveHopper>>()

	/**
	 * One bucket for each tick in the 150-tick collection cycle.
	 * A hopper belongs to one bucket based on its stable location hash.
	 */
	private val scanBuckets = Array(COLLECTION_INTERVAL_TICKS) { linkedSetOf<ActiveHopper>() }

	/**
	 * Refreshes queued by block events. These are the immediate path for normal
	 * changes such as placing, breaking, filling, or moving hopper blocks.
	 */
	private val pendingRefreshes = linkedSetOf<BlockPosition>()

	private var currentTick = 0
	private var tickTask: BukkitTask? = null
	private var refreshTask: BukkitTask? = null

	override fun onEnable() {
		currentTick = 0

		/**
		 * This explicit registration is what places Advanced Hoppers on the
		 * sign-less scanner whitelist. No normal multiblocks are included.
		 */
		SignlessMultiblockScanner.register(this)

		tickTask = Tasks.syncRepeatTask(1L, 1L, ::tick)
	}

	override fun onDisable() {
		SignlessMultiblockScanner.unregister(this)

		tickTask?.cancel()
		refreshTask?.cancel()

		tickTask = null
		refreshTask = null

		hoppersByWorld.clear()
		scanBuckets.forEach(MutableSet<ActiveHopper>::clear)
		pendingRefreshes.clear()
	}

	/*
	 * -------------------------------------------
	 * Immediate block-change handling
	 * -------------------------------------------
	 *
	 * These events cover ordinary gameplay. They are faster than waiting for
	 * the six-minute scanner fallback.
	 */

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockPlace(event: BlockPlaceEvent) {
		scheduleRefresh(event.block)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockBreak(event: BlockBreakEvent) {
		scheduleRefresh(event.block)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onCauldronLevelChange(event: CauldronLevelChangeEvent) {
	 //Water, lava, powder snow and buckets can change a cauldron without a normal place or break event. This normally breaks it without the individual fix or the scan.
		scheduleRefresh(event.block)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPistonExtend(event: BlockPistonExtendEvent) {
		schedulePistonRefresh(event.blocks, event.direction)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPistonRetract(event: BlockPistonRetractEvent) {
		schedulePistonRefresh(event.blocks, event.direction)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockExplode(event: BlockExplodeEvent) {
		event.blockList().forEach(::scheduleRefresh)
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onEntityExplode(event: EntityExplodeEvent) {
		event.blockList().forEach(::scheduleRefresh)
	}

	@EventHandler
	fun onChunkUnload(event: ChunkUnloadEvent) {
		clearChunk(event.world, event.chunk.x, event.chunk.z)
	}

	@EventHandler
	fun onWorldUnload(event: WorldUnloadEvent) {
		clearWorld(event.world)
	}

	/**
	 * Piston events need two checks. The first check quickly disables a hopper whose structure moved away.
	 * The second runs three ticks later because the final block state can still be settling during the original piston event since pistons aren't instant.
	 */
	private fun schedulePistonRefresh(blocks: List<Block>, direction: BlockFace) {
		val positions = linkedSetOf<BlockPosition>()

		for (block in blocks) {
			val destination = block.getRelative(direction)
			val previous = block.getRelative(direction.oppositeFace)

			positions.add(BlockPosition(block.world, block.x, block.y, block.z))
			positions.add(BlockPosition(destination.world, destination.x, destination.y, destination.z))
			positions.add(BlockPosition(previous.world, previous.x, previous.y, previous.z))
		}

		fun queueRefreshes() {
			for (position in positions) {
				if (!position.world.isChunkLoaded(position.x.shr(4), position.z.shr(4))) continue

				scheduleRefresh(position.world.getBlockAt(position.x, position.y, position.z))
			}
		}

		queueRefreshes()

		Tasks.syncDelay(3L) {
			queueRefreshes()
		}
	}

	//Delay refreshes by one tick so a batch of related block changes becomes one small set of structure checks.
	private fun scheduleRefresh(block: Block) {
		pendingRefreshes.add(BlockPosition(block.world, block.x, block.y, block.z))

		if (refreshTask != null) return

		refreshTask = Tasks.syncDelayTask(1L) {
			refreshTask = null

			val refreshes = pendingRefreshes.toList()
			pendingRefreshes.clear()

			for ((world, x, y, z) in refreshes) {
				if (!world.isChunkLoaded(x.shr(4), z.shr(4))) continue
				refreshColumn(world, x, y, z)
			}
		}
	}

	/**
	 * The changed block could be the cap, cauldron, or output inventory.
	 * Checking these three possible origin heights covers every part of the three-block structure without scanning unrelated nearby blocks.
	 */
	private fun refreshColumn(world: World, x: Int, y: Int, z: Int) {
		for (originY in (y - 1)..(y + 1)) {
			refreshAt(world, x, originY, z)
		}
	}

	/*
	 * ---------------------------------------------------------
	 * The sign-less scanner for Advanced Hoppers
	 * ---------------------------------------------------------
	 *
	 * See "interface SignlessMultiblockDetector" at the first section.
	 * This is the delayed fallback. It handles restored blocks that do not fire normal Bukkit place/break events, such as delayed explosion regen or direct commands like /setblock.
	 */

	/**
	 * Use inventory tile entities as anchors.
	 * Every Advanced Hopper has an inventory directly below its cauldron, so we avoid walking every block position in a loaded chunk.
	 */
	override fun scanLoadedChunk(chunk: Chunk): Int {
		if (!chunk.isLoaded) return 0

		var activated = 0

		for (state in chunk.tileEntities) {
			if (state !is InventoryHolder) continue

			if (refreshAt(chunk.world, state.x, state.y + 1, state.z)) {
				activated++
			}
		}

		return activated
	}

	//Remove active hopper objects when their chunk unloads.
	private fun clearChunk(world: World, chunkX: Int, chunkZ: Int) {
		val hoppers = hoppersByWorld[world]
			?.values
			?.filter { hopper -> hopper.x.shr(4) == chunkX && hopper.z.shr(4) == chunkZ }
			?.toList()
			.orEmpty()

		hoppers.forEach(::unregister)
	}

	private fun clearWorld(world: World) {
		val hoppers = hoppersByWorld.remove(world)?.values?.toList().orEmpty()

		for (hopper in hoppers) {
			scanBuckets[hopper.phase].remove(hopper)
		}
	}

	/**
	 * Check one possible cauldron origin.
	 * Return true only when this call activated a hopper that was previously inactive. The scanner uses that count in its pass telemetry.
	 */
	private fun refreshAt(world: World, x: Int, y: Int, z: Int): Boolean {
		if (y !in world.minHeight until world.maxHeight) return false

		val key = toBlockKey(x, y, z)
		val existing = hoppersByWorld[world]?.get(key)

		//First checks the cheap physical layout before checking active ships.
		//During large scanner passes most inventories are not hoppers, so this avoids doing a starship lookup for ordinary containers.
		//This -if- decides whether this location is allowed to be an active Advanced Hopper. (it's not allowed on ships)
		if (!isValidStructure(world, x, y, z) || isInsideActiveStarship(world, x, y, z)) {
			existing?.let(::unregister)
			return false
		}

		if (existing != null) return false

		val phase = phaseFor(x, y, z)
		val hopper = ActiveHopper(world, x, y, z, phase)

		hoppersByWorld.getOrPut(world, ::mutableMapOf)[hopper.key] = hopper
		scanBuckets[phase].add(hopper)

		return true
	}

	private fun unregister(hopper: ActiveHopper) {
		hoppersByWorld[hopper.world]?.let { worldHoppers ->
			worldHoppers.remove(hopper.key)

			if (worldHoppers.isEmpty()) {
				hoppersByWorld.remove(hopper.world)
			}
		}

		scanBuckets[hopper.phase].remove(hopper)
	}

	/*
	 * --------------------------------------
	 * Active hopper collection loop
	 * ---------------------------------------
	 */

	private fun tick() {
		val bucket = scanBuckets[currentTick].toList()
		currentTick = (currentTick + 1) % COLLECTION_INTERVAL_TICKS

		for (hopper in bucket) {
			collectItems(hopper)
		}
	}

	/**
	 * Collect items for one currently active hopper.
	 * This rechecks the structure first, because a block could have changed
	 * between the event refresh and this hopper's next scheduled collection.
	 */
	private fun collectItems(hopper: ActiveHopper) {
		if (
			!isValidStructure(hopper.world, hopper.x, hopper.y, hopper.z) ||
			isInsideActiveStarship(hopper.world, hopper.x, hopper.y, hopper.z)
		) {
			unregister(hopper)
			return
		}

		val inventory = getOutputInventory(hopper.world, hopper.x, hopper.y - 1, hopper.z) ?: run {
			unregister(hopper)
			return
		}

		/**
		 * Exact collection box:
		 * - x and z: one block either side of the origin
		 * - y: origin +2 through origin +17
		 * For a basic interpretation of the multiblock see "BEGIN: ADVANCED HOPPER"
		 */
		val center = Location(hopper.world, hopper.x + 0.5, hopper.y + 10.0, hopper.z + 0.5)

		val items = hopper.world
			.getNearbyEntities(center, 1.5, 8.0, 1.5)
			.filterIsInstance<Item>()
			.filter { item ->
				val location = item.location

				location.blockX in (hopper.x - 1)..(hopper.x + 1) &&
					location.blockY in (hopper.y + 2)..(hopper.y + 17) &&
					location.blockZ in (hopper.z - 1)..(hopper.z + 1)
			}

		for (item in items) {
			if (!item.isValid) continue

			/**
			 * This is the part that handles items moving into container amounts.
			 * Bukkit returns the exact amount that did not fit. Only then do it removes or shrinks the dropped item, which avoids item loss when the output is full or only has room for part of a stack.
			 */
			val remainder = try {
				inventory.addItem(item.itemStack.clone()).values.firstOrNull()
			} catch (exception: Exception) {
				log.warn(
					"Unable to insert into Advanced Hopper output at ${hopper.x}, ${hopper.y}, ${hopper.z}",
					exception,
				)
				return
			}
            //This is an extra check before changing the entity.
			if (!item.isValid) continue

			if (remainder == null) {
				item.remove()
			} else {
				item.itemStack = remainder
			}
		}
	}

	//The cauldron must be empty, the cap must be chiseled stone bricks, and the block beneath the cauldron must expose an inventory.
	private fun isValidStructure(world: World, x: Int, y: Int, z: Int): Boolean {
		if (world.getBlockAt(x, y, z).type != Material.CAULDRON) return false
		if (world.getBlockAt(x, y + 1, z).type != Material.CHISELED_STONE_BRICKS) return false

		return getOutputInventory(world, x, y - 1, z) != null
	}

	/**
	 * Chiseled Stone Bricks should already stop this structure being piloted.
	 * This is still checked as a fallback, so a valid-looking hopper never operates inside an active ship if that rule is bypassed somehow.
	 */
	private fun isInsideActiveStarship(world: World, x: Int, y: Int, z: Int): Boolean {
		return ActiveStarships.getInWorld(world).any { starship ->
			starship.contains(x, y, z)
		}
	}

	private fun getOutputInventory(world: World, x: Int, y: Int, z: Int): Inventory? {
		return (world.getBlockAt(x, y, z).getState(false) as? InventoryHolder)?.inventory
	}

	/**
	 * This gives each hopper a stable place in the 150-tick cycle.
	 * It is based only on location, so it does not reset when players leave, chunks unload, or the server restarts.
	 */
	private fun phaseFor(x: Int, y: Int, z: Int): Int {
		val hash = (x * 73_856_093) xor (y * 19_349_663) xor (z * 83_492_791)

		return Math.floorMod(hash, COLLECTION_INTERVAL_TICKS)
	}
}

/*
 * ====================
 * END: ADVANCED HOPPER
 * ====================
 */
