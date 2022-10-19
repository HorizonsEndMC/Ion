package net.starlegacy.feature.tutorial

import com.destroystokyo.paper.Title
import java.io.File
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.collections.set
import kotlin.math.abs
import net.starlegacy.SLComponent
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.StarshipDestruction
import net.starlegacy.feature.starship.event.StarshipRotateEvent
import net.starlegacy.feature.starship.event.StarshipStartCruisingEvent
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.feature.starship.event.StarshipUnpilotEvent
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.execConsoleCmd
import net.starlegacy.util.gray
import net.starlegacy.util.msg
import net.starlegacy.util.nms
import net.starlegacy.util.paste
import net.starlegacy.util.readSchematic
import net.starlegacy.util.red
import net.starlegacy.util.title
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkUnloadEvent

object TutorialManager : SLComponent() {
	private var playersInTutorials = mutableMapOf<Player, TutorialPhase>()
	private var readTimes = mutableMapOf<UUID, Long>()

	private const val WORLD_NAME = "Tutorial"

	private fun getWorld(): World = Bukkit.getWorld(WORLD_NAME) ?: error("Tutorial world not found")

	fun isWorld(world: World): Boolean = world.name == WORLD_NAME

	override fun onEnable() {
		subscribe<PlayerJoinEvent> { event ->
			val player = event.player
			player.resetTitle()
			playersInTutorials.remove(player) // who knows...
			if (isWorld(player.world)) {
				teleportToStart(player)
			}
		}

		subscribe<PlayerQuitEvent> { event ->
			stop(event.player)
		}

		subscribe<BlockBreakEvent> { event ->
			if (isWorld(event.block.world)) {
				event.isCancelled = true
			}
		}

		subscribe<StarshipRotateEvent> { event ->
			val player = event.player
			if (isWorld(player.world) && (getPhase(player) ?: TutorialPhase.LAST) < TutorialPhase.TURN_RIGHT) {
				event.isCancelled = true
			}
		}

		subscribe<StarshipStartCruisingEvent> { event ->
			val player = event.player
			if (isWorld(player.world) && (getPhase(player) ?: TutorialPhase.LAST) < TutorialPhase.CRUISE_START) {
				event.isCancelled = true
			}
		}

		subscribe<StarshipTranslateEvent> { event ->
			val player = event.player
			if (isWorld(player.world) && (getPhase(player) ?: TutorialPhase.LAST) < TutorialPhase.SHIFT_FLY_FORWARD
			) {
				event.isCancelled = true
			}
		}

		// disable all damage in the world
		subscribe<EntityDamageEvent> { event ->
			if (isWorld(event.entity.world)) {
				event.isCancelled = true
			}
		}

		// erase chunks in the world
		subscribe<ChunkUnloadEvent> { event ->
			if (!isWorld(event.world)) {
				return@subscribe
			}

			val chunk = event.chunk
			val chunkReference = WeakReference(chunk)

			val datas = DeactivatedPlayerStarships.getInChunk(chunk)
			if (datas.any()) {
				log.warn("Deleting " + datas.size + " starship computers in tutorial world")
				DeactivatedPlayerStarships.destroyManyAsync(datas) {
					clearChunk(chunkReference)
				}
				return@subscribe
			}

			clearChunk(chunkReference)
		}

		subscribe<StarshipUnpilotEvent>(priority = EventPriority.LOW) { event ->
			val player = event.player

			if (!isWorld(player.world) || playersInTutorials[player] == TutorialPhase.LAST) {
				return@subscribe
			}

			player msg "&eYou unpiloted your starship, stopping tutorial"

			stop(player)

			StarshipDestruction.vanish(event.starship)
			event.isCancelled = true

			Tasks.syncDelay(10) {
				player title Title.builder()
					.title(red("Tutorial Canceled"))
					.subtitle(gray("Unpiloted (right clicked computer) before the tutorial end"))
					.fadeIn(10)
					.stay(40)
					.fadeOut(10)
					.build()
			}
		}

		// if someone places a ship computer in an existing one, overwrite it
		subscribe<BlockPlaceEvent>(priority = EventPriority.LOWEST) { event ->
			if (isWorld(event.block.world) && event.block.type == Material.JUKEBOX) {
				val loc = event.block.location
				DeactivatedPlayerStarships[loc.world, loc.blockX, loc.blockY, loc.blockZ]?.let { state ->
					log.warn("Deleted computer ${loc.world.name}@${Vec3i(loc)} because someone placed over it")
					DeactivatedPlayerStarships.destroyAsync(state)
				}
			}
		}

//		TutorialPhase.values().forEach(TutorialPhase::setupHandlers)
	}

	private fun clearChunk(chunkReference: WeakReference<Chunk>) {
		val chunk = chunkReference.get() ?: return
		val nmsChunk = chunk.nms
//		val sections = nmsChunk.sections
		for (it in nmsChunk.blockEntities.keys.toList()) {
			nmsChunk.level.removeBlockEntity(it)
		}
//		for (i in 0..sections.lastIndex) {
//			sections[i] = NMSLevelChunk.EMPTY_CHUNK_SECTION
//		}
	}

	fun start(player: Player) {
		require(PilotedStarships[player] == null)

		playersInTutorials[player] = TutorialPhase.FIRST

		val loc: Location = getSafeLocation()
		loadShip(loc)
		player.teleport(loc)
		player.teleport(loc) // teleport a second time, because, well... minecraft

		startPhase(player, TutorialPhase.FIRST)
	}

	fun startPhase(player: Player, phase: TutorialPhase) {
		require(playersInTutorials.containsKey(player))

		playersInTutorials[player] = phase

		phase.onStart(player)

		var time = 0L
		for ((index, message) in phase.messages.withIndex()) {
			Tasks.syncDelay(time) {
				if (getPhase(player) == phase) {
					message.show(player)
				}
			}

			if (index == phase.messages.lastIndex) {
				break
			}

			time += (message.seconds * 20).toInt()
		}

		val uuid = player.uniqueId

		Tasks.syncDelay(time + 1) { readTimes.remove(uuid) }

		// add 1 second since this is just in case it doesn't get removed automatically somehow
		readTimes[uuid] = System.currentTimeMillis() + time * 50L + 1000L // 50 ms per tick
	}

	fun stop(player: Player) {
		readTimes.remove(player.uniqueId)

		val phase: TutorialPhase? = playersInTutorials.remove(player)

		if (!isWorld(player.world)) {
			return
		}

		player.resetTitle()

		Tasks.syncDelay(10) {
			when (phase) {
				TutorialPhase.LAST -> teleportToEnd(player)
				else -> teleportToStart(player)
			}
		}

		return
	}

	private fun teleportToStart(player: Player) {
		execConsoleCmd("warp tutorialstart ${player.name}")
	}

	private fun teleportToEnd(player: Player) {
		execConsoleCmd("warp tutorialend ${player.name}")
		player msg "&l&o&cCheck out this tutorial too&8:&b https://youtu.be/AFfDmmpMXQw"
	}

	private const val distance = 1000

	private fun getSafeLocation(): Location {
		var x = distance
		while (getWorld().players.any { abs(it.location.blockX - x) <= distance }) {
			x += distance
		}
		return Location(getWorld(), x.toDouble() + 0.5, (getWorld().maxHeight / 2).toDouble(), 0.5)
	}

	private fun loadShip(loc: Location) {
		val file = File(plugin.dataFolder, "tutorial_ship.schematic")

		if (!file.exists()) {
			error("${file.absolutePath} doesn't exist!")
		}

		val clipboard = readSchematic(file) ?: error("Failed to read ${file.path}")
		clipboard.paste(loc.world, loc.blockX, loc.blockY, loc.blockZ)
	}

	fun isReading(player: Player): Boolean = (readTimes[player.uniqueId] ?: 0L) >= System.currentTimeMillis()

	fun getPhase(player: Player): TutorialPhase? = playersInTutorials[player]
}
