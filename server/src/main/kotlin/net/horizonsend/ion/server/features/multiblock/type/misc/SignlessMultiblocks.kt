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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.CauldronLevelChangeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerMoveEvent
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.scheduler.BukkitTask
import java.util.ArrayDeque
import java.util.UUID

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
 * There are two scans one is a periodic scanner intended to revive already loaded chunks that has dormant.
 * The other is a faster loader for when a player moves into a fresh chunk then stays there for X time to then activate the scan.
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

	private const val MAX_PRIORITY_CHUNKS_PER_TICK = 1
	//translates to 10 seconds
	private const val PLAYER_PRIORITY_DELAY_TICKS = 200
	//debug control
	private const val DEBUG_PRIORITY_SCAN_PLAYER_LOG = true
	//debug control
	private const val DEBUG_PRIORITY_SCAN_SUMMARY_LOG = true
	//debug control
	private const val PRIORITY_SCAN_REPORT_INTERVAL_TICKS = 5 * 60 * 20

	//Chunk coordinates are stored instead of Chunk objects. This avoids retaining a chunk object after the server unloads that chunk.
	private data class ChunkTarget(
		val world: World,
		val x: Int,
		val z: Int,
	)

	//Tracks a player's current 10-second check to see whether they remain in the same exact chunk before starting a priority scan.
	private data class PendingPlayerPriorityCheck(
		val playerId: UUID,
		val world: World,
		val chunkX: Int,
		val chunkZ: Int,
		val dueTick: Long,
	)

	//Keeps the newly sent still relevant chunk targets for one player while their 10-second check restarts as they move between chunks.
	private data class PlayerPriorityBacklog(
		val world: World,
		val targets: ArrayDeque<ChunkTarget> = ArrayDeque(),
		val queuedTargets: MutableSet<ChunkTarget> = linkedSetOf(),
	)

	/*Represents one verified player's chunk backlog after it is ready to be processed fairly alongside other players' requests since
	* the scanner takes one chunk from each request per turn, spreading work across separate ticks instead of finishing one player's entire request first.
	* For example Player A request: A1, A2, A3, Player B request: B1, B2: tick 1: A1, tick 2: B1, tick 3: A2, tick 4: B2, tick 5: A3.
	*/
	private data class PlayerPriorityScanRequest(
		val targets: ArrayDeque<ChunkTarget>,
	)

	//Debug-only counters used by the five-minute console summary to show how the player priority scanner is behaving.
	//start of debug
	private data class PriorityScanStats(
		var watchesStarted: Int = 0,
		var watchesRestarted: Int = 0,
		var watchesVerified: Int = 0,
		var watchesNotVerified: Int = 0,
		var targetsRecorded: Int = 0,
		var requestsStarted: Int = 0,
		var duplicateTargetsSkipped: Int = 0,
		var chunksScanned: Int = 0,
		var chunksSkippedUnloaded: Int = 0,
		var structuresActivated: Int = 0,
		var detectorCalls: Int = 0,
		var detectorNanos: Long = 0L,
	)
	//end of debug

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
	 * Right now Advanced Hoppers are the only detector. Future sign-less multiblocks can register here without changing normal multiblock code.
	 */
	private val detectors = linkedSetOf<SignlessMultiblockDetector>()

	//Deadline-driven queue used for startup and six-minute recovery passes.
	private val pendingFullScanTargets = ArrayDeque<ChunkTarget>()

	//Prevents one chunk appearing many times in the same full pass.
	private val queuedFullScanTargets = linkedSetOf<ChunkTarget>()

	//Stores verified player scan requests waiting to take their fair turn.
	private val pendingPlayerPriorityScanRequests = ArrayDeque<PlayerPriorityScanRequest>()
	//Prevents the same chunk being priority-scanned more than once while requests are still pending.
	private val queuedPriorityScanTargets = linkedSetOf<ChunkTarget>()

	//Stores each player's current 200-tick (pre scan see: "private const val PLAYER_PRIORITY_DELAY_TICKS" if the amount changed) check for staying in one exact chunk.
	private val pendingPlayerPriorityChecks = mutableMapOf<UUID, PendingPlayerPriorityCheck>()
	//Keeps each player's newly sent chunk targets until they pass the "val PLAYER_PRIORITY_DELAY_TICKS" same-chunk check.
	private val playerPriorityBacklogs = mutableMapOf<UUID, PlayerPriorityBacklog>()
	/*This is the timing for the "PLAYER_PRIORITY_DELAY_TICKS" tick checks, by default the tick amount is/was 200, it splits pending player checks into 200 tick buckets so each server tick
	*only checks the players due now. It creates 200 buckets, one for each possible tick position in the delay cycle.
	* When a player’s check is due at a certain tick, their UUID is placed into dueTick % 200. This makes it so each server tick only
	* needs to inspect one bucket rather than looping through every player’s pending check.
	*/
	private val playerPriorityCheckBuckets = Array(PLAYER_PRIORITY_DELAY_TICKS) { linkedSetOf<UUID>() }

	private var serverTick = 0L
	private var nextPeriodicStartTick = PERIODIC_SCAN_INTERVAL_TICKS.toLong()
	//debug
	private var nextPriorityScanReportTick = PRIORITY_SCAN_REPORT_INTERVAL_TICKS.toLong()
	//debug
	private var priorityScanStats = PriorityScanStats()
	private var fullPass: FullScanPass? = null
	private var scanTask: BukkitTask? = null

	override fun onEnable() {
		serverTick = 0L
		nextPeriodicStartTick = PERIODIC_SCAN_INTERVAL_TICKS.toLong()
		//debug
		nextPriorityScanReportTick = PRIORITY_SCAN_REPORT_INTERVAL_TICKS.toLong()
		//debug
		priorityScanStats = PriorityScanStats()

		startFullPass("startup")

		scanTask = Tasks.syncRepeatTask(1L, 1L, ::tick)
	}

	override fun onDisable() {
		scanTask?.cancel()
		scanTask = null

		detectors.clear()
		pendingFullScanTargets.clear()
		queuedFullScanTargets.clear()
		pendingPlayerPriorityScanRequests.clear()
		queuedPriorityScanTargets.clear()
		pendingPlayerPriorityChecks.clear()
		playerPriorityBacklogs.clear()
		playerPriorityCheckBuckets.forEach { bucket -> bucket.clear() }
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

	//This is the front half of the "PLAYER_PRIORITY_DELAY_TICKS" system, it decides which chunks may later be priority-scanned.

	//Player loads a new chunk (it happens every time the player leaves a chunk)
	@EventHandler
	fun onPlayerChunkLoad(event: PlayerChunkLoadEvent) {
		val player = event.player
		val location = player.location
		//Player’s current location in the world. shr 4 converts to chunk coord. Essentially means “shift right by four binary places,” which effectively divides by 16.
		val chunkX = location.blockX shr 4
		val chunkZ = location.blockZ shr 4

		//This makes sure the player has a current "PLAYER_PRIORITY_DELAY_TICKS" tick watch for the chunk they are physically standing in.
		startPlayerPriorityCheck(player, player.world, chunkX, chunkZ)
		//Remember this sent chunk for a possible future priority scan.
		recordPlayerPriorityTarget(player, event.chunk)
	}

	private fun recordPlayerPriorityTarget(player: Player, chunk: Chunk) {
		//Stores the player’s current world for easier use below.
		val world = player.world

        /*Immediately stops this function with return so it protects against recording a chunk from the wrong world. For example player is now in hyperspace
        *but an old overworld chunk event arrives = do not save that old overworld chunk.
        */
		if (chunk.world != world) return

		val location = player.location
		val chunkX = location.blockX shr 4
		val chunkZ = location.blockZ shr 4
		val simulationDistance = world.simulationDistance

		//This checks whether the sent chunk is too far from the player’s current chunk and stop wanting to record if so.
		if (
			kotlin.math.abs(chunk.x - chunkX) > simulationDistance ||
			kotlin.math.abs(chunk.z - chunkZ) > simulationDistance
		) return

		//Looks up this player’s current saved backlog.
		var backlog = playerPriorityBacklogs[player.uniqueId]

		//This checks 1. The player has no backlog yet. 2. Their existing backlog belongs to another world.
		if (backlog == null || backlog.world != world) {
			//Creates a fresh empty backlog for that player and saves it in the map.
			backlog = PlayerPriorityBacklog(world)
			playerPriorityBacklogs[player.uniqueId] = backlog
		}

		//Creates a small record identifying this chunk. It's fine for unloading. Because it's a set it prevent duplication.
		val target = ChunkTarget(chunk.world, chunk.x, chunk.z)

		if (backlog.queuedTargets.add(target)) {
			//First added = first processed later
			backlog.targets.addLast(target)
			//debug
			priorityScanStats.targetsRecorded++
		}
	}

	/*
    * Restarts the player's "PLAYER_PRIORITY_DELAY_TICKS" priority scan check only when they enter a
    * different chunk or world. Moving around inside the same chunk does nothing.
    * Ignore cancelled means if another codes or plugins blocks someone from entering a region, this code will not pretend they successfully moved into that new area. Like plot squared.
    */
	@EventHandler(ignoreCancelled = true)
	fun onPlayerMove(event: PlayerMoveEvent) {
		val from = event.from
		val to = event.to ?: return
		//Safely top if bukkit does not provide a destination location.
		val toWorld = to.world ?: return
		//Same chunk converting
		val fromChunkX = from.blockX shr 4
		val fromChunkZ = from.blockZ shr 4
		val toChunkX = to.blockX shr 4
		val toChunkZ = to.blockZ shr 4

		//Ignore normal movement inside the same chunk.
		if (
			from.world == toWorld &&
			fromChunkX == toChunkX &&
			fromChunkZ == toChunkZ
		) return

		//The player crossed into a new chunk so begin a fresh "PLAYER_PRIORITY_DELAY_TICKS"
		startPlayerPriorityCheck(event.player, toWorld, toChunkX, toChunkZ)
	}

	/*
    * Creates or restarts a player's "PLAYER_PRIORITY_DELAY_TICKS" check for remaining in one exact
    * chunk, while keeping only their backlog targets still near that new area.
    */
	private fun startPlayerPriorityCheck(
		player: Player,
		world: World,
		chunkX: Int,
		chunkZ: Int,
	) {
		//Looks up whether this player already has a pending "PLAYER_PRIORITY_DELAY_TICKS" check.
		val existing = pendingPlayerPriorityChecks[player.uniqueId]

		//The player is already being checked for this exact chunk, so do not reset their timer.
		if (
			existing != null &&
			existing.world == world &&
			existing.chunkX == chunkX &&
			existing.chunkZ == chunkZ
		) return

		//Removes saved chunks that are no longer within this player's current sim dist.
		prunePlayerPriorityBacklog(
			player.uniqueId,
			world,
			chunkX,
			chunkZ,
			world.simulationDistance,
		)

		//If an older check exists, remove that player from its old timing bucket before replacing it.
		existing?.let { previous ->
			playerPriorityCheckBuckets[
			     //Uses the due tick's remainder to find which of the 200 timing buckets holds this player. see "playerPriorityCheckBuckets"
				(previous.dueTick % PLAYER_PRIORITY_DELAY_TICKS.toLong()).toInt()
			].remove(player.uniqueId)
			//debug
			priorityScanStats.watchesRestarted++
		}

		//Sets the exact future server tick when this player should be verified.
		val dueTick = serverTick + PLAYER_PRIORITY_DELAY_TICKS

		pendingPlayerPriorityChecks[player.uniqueId] = PendingPlayerPriorityCheck(
			playerId = player.uniqueId,
			world = world,
			chunkX = chunkX,
			chunkZ = chunkZ,
			dueTick = dueTick,
		)

		//Adds the player to one timing bucket so only players due on that tick are checked.
		playerPriorityCheckBuckets[
			(dueTick % PLAYER_PRIORITY_DELAY_TICKS.toLong()).toInt()
		].add(player.uniqueId)
		//debug
		priorityScanStats.watchesStarted++
	}

	/*
    * Keeps only saved chunk targets that are still inside the player's current
    * simulation-distance square, preventing old travel targets from building up.
    */
	private fun prunePlayerPriorityBacklog(
		playerId: UUID,
		world: World,
		chunkX: Int,
		chunkZ: Int,
		simulationDistance: Int,
	) {
		//There is nothing to prune when this player has not recorded any chunk targets yet.
		val backlog = playerPriorityBacklogs[playerId] ?: return

		//Chunks from another world are never relevant to the player's current area.
		if (backlog.world != world) {
			playerPriorityBacklogs.remove(playerId)
			return
		}

		//Builds a temporary list containing only chunk targets that are still nearby.
		val retained = backlog.targets.filter { target ->
			target.world == world &&
			    //Keeps chunks no farther than simulation distance away on both chunk axes, forming a square area.
				kotlin.math.abs(target.x - chunkX) <= simulationDistance &&
				kotlin.math.abs(target.z - chunkZ) <= simulationDistance
		}

		//Replaces the old scan order with only the nearby chunk targets that survived pruning.
		backlog.targets.clear()
		backlog.targets.addAll(retained)
		//Rebuilds the duplicate-prevention set so it exactly matches the remaining scan queue.
		backlog.queuedTargets.clear()
		backlog.queuedTargets.addAll(retained)
	}

	@EventHandler
	fun onWorldUnload(event: WorldUnloadEvent) {
	    //Remove work for the closing world. The scanner must not retain an old world reference after that world is gone.
		removeWorldTargets(
			event.world,
			pendingFullScanTargets,
			queuedFullScanTargets,
		)

		//Removes player-priority work and saved player chunk targets for a world that is unloading, so the scanner does not keep references to a closed world.a
		removeWorldPriorityRequests(event.world)

		//Cancels "PLAYER_PRIORITY_DELAY_TICKS" player checks that were waiting for players in the unloading world.
		pendingPlayerPriorityChecks.entries.removeAll { entry ->
			entry.value.world == event.world
		}

		//Removes saved chunk backlogs belonging to players who were loading chunks in the unloading world.
		playerPriorityBacklogs.entries.removeAll { entry ->
			entry.value.world == event.world
		}
	}

	private fun tick() {
		serverTick++
		processPendingPlayerPriorityChecks()

		//Full passes never overlap. If an earlier one takes too long, it gets to finish and report its result before another pass starts.
		if (fullPass == null && serverTick >= nextPeriodicStartTick) {
			startFullPass("periodic")
			nextPeriodicStartTick = serverTick + PERIODIC_SCAN_INTERVAL_TICKS
		}


		val tickStartNanos = System.nanoTime()

		fullPass?.let(::processFullPass)
		processPriorityQueue(tickStartNanos)

		//start of debug
		if (serverTick >= nextPriorityScanReportTick) {
			if (DEBUG_PRIORITY_SCAN_SUMMARY_LOG) {
				reportPriorityScanStats()
			} else {
				priorityScanStats = PriorityScanStats()
			}
			nextPriorityScanReportTick = serverTick + PRIORITY_SCAN_REPORT_INTERVAL_TICKS
		}
		//end of debug

		//Finishes a full scan once every queued chunk has been handled, or logs one warning when the scan falls behind its six-minute deadline.
		//Stops this part of the tick when there is no active startup or periodic full scan running.
		val activePass = fullPass ?: return

		//All chunks from the current full scan have now been handled or skipped.
		if (pendingFullScanTargets.isEmpty()) {
			//Writes the completed full-scan report with totals such as scanned chunks, activations, and timing.
			finishFullPass(activePass)
			//Marks the full scan as finished so a later periodic scan can begin.
			fullPass = null
			return
		}

		//Logs one warning if this scan is still unfinished after its deadline, without repeating the warning every tick.
		if (serverTick > activePass.deadlineTick && !activePass.deadlineWarningLogged) {
			activePass.deadlineWarningLogged = true

			//Warns console if the periodic full scanner falling behind
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

	    /*
        * Checks only the players whose "PLAYER_PRIORITY_DELAY_TICKS" same-chunk timer is due this tick.
        * Verified players hand their retained previous front end nearby chunk backlog to the spread priority scan queue.
        */
	private fun processPendingPlayerPriorityChecks() {
		//Uses the current tick's remainder to select one of the queued timing buckets due now.
		val bucketIndex = (serverTick % PLAYER_PRIORITY_DELAY_TICKS.toLong()).toInt()
		//Gets the small group of player UUIDs whose checks could be due on this tick.
		val bucket = playerPriorityCheckBuckets[bucketIndex]
		//Keeps only players whose stored due tick matches this exact server tick.
		val playerIds = bucket.filter { playerId ->
			pendingPlayerPriorityChecks[playerId]?.dueTick == serverTick
		}

		//Removes expired, completed, or replaced player UUIDs from this timing bucket.
		bucket.removeAll { playerId ->
			//Skips this UUID if its pending check was removed or replaced before processing.
			val pending = pendingPlayerPriorityChecks[playerId]
			pending == null || pending.dueTick <= serverTick
		}

		for (playerId in playerIds) {
			val pending = pendingPlayerPriorityChecks[playerId] ?: continue
			//Removes the completed check before deciding whether it passed or needs restarting.
			pendingPlayerPriorityChecks.remove(playerId)

			//Looks up the currently online Bukkit player from their stored UUID.
			val player = Bukkit.getPlayer(playerId)
			//Discard saved targets when the player is offline.
			if (player == null) {
				playerPriorityBacklogs.remove(playerId)
				//debug
				priorityScanStats.watchesNotVerified++
				continue
			}

		    //Fails the check when the player changed worlds after their timer began.
			if (player.world != pending.world) {
				playerPriorityBacklogs.remove(playerId)
				//debug
				priorityScanStats.watchesNotVerified++
				continue
			}

			//Converts the player's current block position into chunk coords.
			val location = player.location
			val chunkX = location.blockX shr 4
			val chunkZ = location.blockZ shr 4

			//The player left the exact chunk being verified, so restart the "PLAYER_PRIORITY_DELAY_TICKS" timer in their new chunk.
			if (chunkX != pending.chunkX || chunkZ != pending.chunkZ) {
				//Starts a fresh same-chunk check without discarding the player's retained nearby backlog.
				startPlayerPriorityCheck(player, player.world, chunkX, chunkZ)
				//debug
				priorityScanStats.watchesNotVerified++
				continue
			}

			//Keeps only saved chunk targets still inside the player's current sim distance area.
			prunePlayerPriorityBacklog(
				playerId,
				player.world,
				chunkX,
				chunkZ,
				player.world.simulationDistance,
			)

			//Takes ownership of this player's retained targets, or uses an empty queue when none were recorded.
			val targets = playerPriorityBacklogs.remove(playerId)?.targets
				?: ArrayDeque<ChunkTarget>()

			//debug
			priorityScanStats.watchesVerified++
			if (DEBUG_PRIORITY_SCAN_PLAYER_LOG) {
				log.info(
					"Sign-less player priority scan: pre-prio check verified for " +
						"${player.name}; moving on to scan ${targets.size} chunks."
				)
			}
			//end of debug

			//There is no useful chunk work to submit even though the player passed the pre-prio scan check.
			if (targets.isEmpty()) continue

			//Starts a fresh duplicate tracking period when no earlier priority requests are still active.
			if (pendingPlayerPriorityScanRequests.isEmpty()) {
				queuedPriorityScanTargets.clear()
			}

			//Adds this verified player's targets to the end of the spread out priority request queue.
			pendingPlayerPriorityScanRequests.addLast(
				PlayerPriorityScanRequest(targets),
			)
			//debug
			priorityScanStats.requestsStarted++
		}
	}

	    /*
        * Scans at most the configured number of priority targets this tick, taking
        * one target per player request in spread queue order to spread work fairly.
        */
	private fun processPriorityQueue(tickStartNanos: Long) {
		//There is nothing to scan when no sign-less multiblock detectors are registered.
		if (detectors.isEmpty()) return
		//No verified player requests are waiting for priority scanning.
		if (pendingPlayerPriorityScanRequests.isEmpty()) return
		//Leaves priority work for a later tick when the shared scanner time budget has already been used.
		if (System.nanoTime() - tickStartNanos >= MAX_SCAN_WORK_NANOS_PER_TICK) return

		//Takes a stable copy so detector registration changes cannot affect this scan loop halfway through. Counts priority targets handled this tick so work stays capped.
		val detectorSnapshot = detectors.toList()
		var processed = 0

		//Keeps taking turns until this tick reaches its target limit or no requests remain.
		while (
			processed < MAX_PRIORITY_CHUNKS_PER_TICK &&
			pendingPlayerPriorityScanRequests.isNotEmpty()
		) {
			//Takes the next player's request from the front of the spread queue.
			val request = pendingPlayerPriorityScanRequests.removeFirst()

			//Ignore requests that no longer contain any chunks.
			if (request.targets.isEmpty()) continue

			//Takes one chunk from this player's request rather than scanning their entire backlog at once.
			val target = request.targets.removeFirst()

			//Moves unfinished player requests to the back so other players get their turn first. see "private data class PlayerPriorityScanRequest"
			if (request.targets.isNotEmpty()) {
				pendingPlayerPriorityScanRequests.addLast(request)
			}

			//Skip this target when another active request already claimed the same chunk.
			if (!queuedPriorityScanTargets.add(target)) {
				//debug
				priorityScanStats.duplicateTargetsSkipped++
				//Counts the duplicate as handled this tick so many duplicate requests cannot create an unbounded loop.
				processed++
				continue
			}

			//debug
			val result = scanTarget(target, detectorSnapshot)

			//Records that the chunk unloaded before its turn arrived, so no detector scan was possible.
			if (!result.scanned) {
				//debug
				priorityScanStats.chunksSkippedUnloaded++
			} else {
				//start of debug
				priorityScanStats.chunksScanned++
				priorityScanStats.structuresActivated += result.activated
				priorityScanStats.detectorCalls += result.detectorCalls
				priorityScanStats.detectorNanos += result.detectorNanos
				//end of debug
			}

			processed++
		}

		if (pendingPlayerPriorityScanRequests.isEmpty()) {
			queuedPriorityScanTargets.clear()
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
	 * The scanner does not ask Bukkit to load anything here. It simply queues chunks the server is already keeping active.
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

	//start of debug
	private fun reportPriorityScanStats() {
		val stats = priorityScanStats
		val averageDetectorMicros = if (stats.detectorCalls == 0) {
			0L
		} else {
			stats.detectorNanos / stats.detectorCalls / 1_000L
		}

		val pendingTargets = pendingPlayerPriorityScanRequests.sumOf { request ->
			request.targets.size
		}

		log.info(
			"Sign-less player priority scan (last 5m): " +
				"watches-started=${stats.watchesStarted}, " +
				"watches-restarted=${stats.watchesRestarted}, " +
				"watches-verified=${stats.watchesVerified}, " +
				"watches-not-verified=${stats.watchesNotVerified}, " +
				"targets-recorded=${stats.targetsRecorded}, " +
				"requests-started=${stats.requestsStarted}, " +
				"chunks-scanned=${stats.chunksScanned}, " +
				"duplicate-targets-skipped=${stats.duplicateTargetsSkipped}, " +
				"chunks-skipped-unloaded=${stats.chunksSkippedUnloaded}, " +
				"activated=${stats.structuresActivated}, " +
				"avg-detector-us=$averageDetectorMicros, " +
				"pending-requests=${pendingPlayerPriorityScanRequests.size}, " +
				"pending-targets=$pendingTargets."
		)

		priorityScanStats = PriorityScanStats()
	}
	//end of debug

	//Removes chunks from a world that is unloading from active player priority scan requests, then removes requests left with no chunks to scan.
	private fun removeWorldPriorityRequests(world: World) {
		for (request in pendingPlayerPriorityScanRequests) {
			val retained = request.targets.filter { target -> target.world != world }
			request.targets.clear()
			request.targets.addAll(retained)
		}

		pendingPlayerPriorityScanRequests.removeAll { request -> request.targets.isEmpty() }
		queuedPriorityScanTargets.removeAll { target -> target.world == world }
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
 * END: SIGN-LESS MULTIBLOCK SCANNERS FOR RECOVERY AND AUTO DETECTION
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
