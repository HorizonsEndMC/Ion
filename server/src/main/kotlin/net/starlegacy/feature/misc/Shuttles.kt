package net.starlegacy.feature.misc

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.trade.CargoCrates
import net.horizonsend.ion.common.database.schema.misc.Shuttle
import net.starlegacy.feature.nations.gui.openConfirmMenu
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.sharedDataFolder
import net.starlegacy.util.Tasks
import net.starlegacy.util.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.Vec3i
import net.starlegacy.util.action
import net.starlegacy.util.actionAndMsg
import net.starlegacy.util.colorize
import net.starlegacy.util.getChunkAtIfLoaded
import net.starlegacy.util.isInRange
import net.starlegacy.util.msg
import net.starlegacy.util.placeSchematicEfficiently
import net.starlegacy.util.readSchematic
import net.horizonsend.ion.server.miscellaneous.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.setLoreAndGet
import net.starlegacy.util.toCreditsString
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlin.collections.set

object Shuttles : IonServerComponent() {
	const val TICKET_COST = 20

	private lateinit var SCHEMATIC_FOLDER: File

	private val schematicCache: LoadingCache<String, Clipboard> = CacheBuilder.newBuilder()
		.build(
			CacheLoader.from { name ->
				requireNotNull(name)
				requireNotNull(getSchematicFile(name)).exists()
				return@from readSchematic(getSchematicFile(name))
					?: error("Failed to read schematic $name")
			}
		)

	val invalidateSchematicCache = { schematicName: String ->
		schematicCache.invalidate(schematicName)
	}.registerRedisAction("misc-shuttle-update-schematic-cache")

	override fun onEnable() {
		SCHEMATIC_FOLDER = File(sharedDataFolder, "shuttle_schematics")
		SCHEMATIC_FOLDER.mkdirs()

		// run updateShuttles() every minute
		// Tasks.asyncRepeat(20 * 60, 20 * 60, ::updateShuttles)
	}

	val line1 = "&b&lShuttle".colorize()
	val line2 = "&b&lTicket".colorize()
	val line3 = "&b&lVendor".colorize()
	val line4 = "&d&o[Right Click]".colorize()

	@EventHandler
	fun onPlayerInteractEventA(event: PlayerInteractEvent) {
		if (!event.player.isOp) return
		if ((event.clickedBlock?.state as? Sign)?.getLine(0) != "[ticketvendor]") return

		val sign = event.clickedBlock?.state as? Sign ?: return

		sign.setLine(0, line1)
		sign.setLine(1, line2)
		sign.setLine(2, line3)
		sign.setLine(3, line4)
		sign.update()
	}

	@EventHandler
	fun onPlayerInteractEventB(event: PlayerInteractEvent) {
		val sign = event.clickedBlock?.state as? Sign ?: return
		if (sign.getLine(0) == line1 && sign.getLine(1) == line2 && sign.getLine(2) == line3 && sign.getLine(3) == line4) {
			val player = event.player
			player.openConfirmMenu("Buy ticket for ${TICKET_COST.toCreditsString()}?", onConfirm = {
				playerClicker.also { p ->
					if (!VAULT_ECO.has(p, TICKET_COST.toDouble())) {
						p msg "You don't have enough credits! Cost: ${TICKET_COST.toCreditsString()}"
					} else {
						VAULT_ECO.withdrawPlayer(p, TICKET_COST.toDouble())
						p.world.dropItem(p.eyeLocation, createTicket())
					}
					p.closeInventory()
				}
			}, onCancel = {})
		}
	}

	private val previousMinuteCache = mutableMapOf<Oid<Shuttle>, Int>()

	private fun messageNearby(location: Location, message: String) {
		for (player in location.world.players) {
			if (player.location.isInRange(location, 250.0)) {
				player msg message
			}
		}
	}

	private fun updateShuttles() {
		if (true) {
			return
		}

		for (shuttle in Shuttle.all()) {
			if (shuttle.destinations.none()) {
				continue
			}

			val currentPosition = shuttle.currentPosition

			val currentDest = shuttle.destinations[currentPosition]

			// only update shuttles with the world loaded on this server
			val world = Bukkit.getWorld(currentDest.world) ?: continue
			val currentLoc =
				Location(world, currentDest.x.toDouble(), currentDest.y.toDouble(), currentDest.z.toDouble())

			val minutes = (Instant.now().epochSecond - shuttle.lastMove.toInstant().epochSecond).toInt() / 60

			// don't do the same phase twice for one shuttle
			if (previousMinuteCache[shuttle._id] == minutes) {
				continue
			}

			previousMinuteCache[shuttle._id] = minutes

			val nextDest = shuttle.destinations[shuttle.nextPosition()].name

			val shuttleName = shuttle.name

			when (minutes) {
				0 -> messageNearby(currentLoc, "&6Shuttle &b$shuttleName&6 departing to &d$nextDest&6 in &a3 minutes")
				1 -> messageNearby(currentLoc, "&6Shuttle &b$shuttleName&6 departing to &d$nextDest&6 in &e2 minutes")
				2 -> messageNearby(currentLoc, "&6Shuttle &b$shuttleName&6 departing to &d$nextDest&6 in &c1 minute")
				// 3 or more (unless it's somehow negative)
				else -> {
					previousMinuteCache.remove(shuttle._id)
					moveShuttle(shuttle, shuttle.nextPosition())
					val time = (shuttle.destinations.size - 1) * 5

					// run after a second delay so that it doesn't message passengers
					Tasks.syncDelay(20) {
						messageNearby(
							currentLoc,
							"&6Shuttle &b$shuttleName&6 departed. Scheduled return in $time minutes"
						)
					}
				}
			}
		}
	}

	private val TICKET_DISPLAY_NAME = "&bShuttle &eTicket".colorize()

	fun createTicket() = ItemStack(Material.PAPER, 1)
		.setDisplayNameAndGet(TICKET_DISPLAY_NAME)
		.setLoreAndGet(listOf("&5Ticket ID&b: &d${UUID.randomUUID()}".colorize()))

	fun isTicket(item: ItemStack): Boolean = item.itemMeta?.displayName == TICKET_DISPLAY_NAME

	fun getSchematicFile(name: String): File {
		return File(SCHEMATIC_FOLDER, "$name.schematic")
	}

	fun getAllSchematics(): List<String> {
		return SCHEMATIC_FOLDER
			.listFiles { f -> f.isFile && f.extension == "schematic" }
			.map { it.nameWithoutExtension }
	}

	private fun getCachedSchematic(schematicName: String): Clipboard = schematicCache[schematicName]

	// use this in case the shuttle is on a different server
	private val removeShuttleAction = { (schematicName, worldName, coords): Triple<String, String, Vec3i> ->
		val world = Bukkit.getWorld(worldName)
		if (world != null) {
			val schematic = getCachedSchematic(schematicName)
			placeSchematicEfficiently(schematic, world, coords, true)
		}
	}.registerRedisAction("misc-shuttle-update-remove", runSync = true)

	// use this in case the shuttle is on a different server
	private val pasteShuttleAction = { (schematic, worldName, coords): Triple<String, String, Vec3i> ->
		val world = Bukkit.getWorld(worldName)
		if (world != null) {
			val schem: Clipboard = getCachedSchematic(schematic)
			placeSchematicEfficiently(schem, world, coords, false)
		}
	}.registerRedisAction("misc-shuttle-paste", runSync = true)

	fun removeShuttleFromWorld(shuttle: Shuttle) = moveShuttle(shuttle, -1)

	private data class ShuttleTeleportPlayersData(
		val shuttle: String,
		val schematic: String,
		val world1: String,
		val pos1: Vec3i,
		val world2: String,
		val pos2: Vec3i
	)

	private val teleportPlayers = { (shuttle, schem, world1Name, pos1, world2Name, pos2): ShuttleTeleportPlayersData ->
		Bukkit.getWorld(world1Name)?.also { world1 ->
			// teleporting players between servers is not yet supported
			val world2 = Bukkit.getWorld(world2Name) ?: error("$world1Name is loaded but not $world2Name!")
			val schematic: Clipboard = getCachedSchematic(schem)
			val (x1, y1, z1) = pos1
			val (x2, y2, z2) = pos2

			val dx1 = x1 - schematic.origin.blockX
			val dy1 = y1 - schematic.origin.blockY
			val dz1 = z1 - schematic.origin.blockZ

			val dx2 = x2 - schematic.origin.blockX
			val dy2 = y2 - schematic.origin.blockY
			val dz2 = z2 - schematic.origin.blockZ

			val region: Region = schematic.region.clone()
			region.shift(BlockVector3.at(dx1, dy1, dz1))

			region.chunks.asSequence()
				.map { world1.getChunkAtIfLoaded(it.blockX, it.blockZ) }
				.filterNotNull()
				.map { it.entities }
				.flatMap { it.asSequence() }
				.filterIsInstance(Player::class.java)
				.filter { player ->
					val (x, y, z) = Vec3i(player.location)
					return@filter region.contains(BlockVector3.at(x, y, z))
				}.filter { player ->
					if (player.inventory.any { CargoCrates[it] != null }) {
						player msg "&cCan't ride a shuttle with crates in your inventory!"
						return@filter false
					}
					for (item: ItemStack? in player.inventory) {
						if (item != null && isTicket(item)) {
							item.amount--
							"&a1 ticket used!".let {
								player msg it
								player action it
							}
							return@filter true
						}
					}
					player actionAndMsg "&eYou don't have a ticket so you were not able to ride with the shuttle! " +
						"To get a shuttle ticket, use the ticker vendor."
					return@filter false
				}.forEach { player ->
					val newLocation: Location = player.location
						.subtract(dx1.toDouble(), dy1.toDouble(), dz1.toDouble())
						.add(dx2.toDouble(), dy2.toDouble(), dz2.toDouble())
					newLocation.world = world2
					player.teleport(newLocation)
				}

			messageNearby(pos2.toLocation(world2), "&6Shuttle &b$shuttle&6 has arrived")
		}
	}.registerRedisAction("misc-shuttle-teleport-players")

	/** Moves the shuttle to the new position, deleting the old copy. If new position is -1,
	 * sets position to 0 in the database and does not paste the shuttle */
	fun moveShuttle(shuttle: Shuttle, newPosition: Int) {
		require(newPosition == -1 || newPosition in 0 until shuttle.destinations.size) { "$newPosition is not within interval [0,${shuttle.destinations.size})" }

		val schematicName = shuttle.schematic
		getCachedSchematic(schematicName) // require the schematic to exist

		val oldPosition = shuttle.currentPosition

		// only teleport players and paste if it's not -1
		val newDest = if (newPosition == -1) null else shuttle.destinations[newPosition]

		// only delete and teleport players if it's a different position
		val oldDest = if (oldPosition == newPosition) null else shuttle.destinations[oldPosition]

		// paste new one first so that the players dont fall on the floor in the old one
		// only do if newDest is not set to null
		if (newDest != null) {
			pasteShuttleAction(Triple(schematicName, newDest.world, Vec3i(newDest.x, newDest.y, newDest.z)))
		}

		// if we are moving somewhere new, move players too
		if (oldDest != null && newDest != null) {
			teleportPlayers(
				ShuttleTeleportPlayersData(
					shuttle.name, schematicName,
					oldDest.world, Vec3i(oldDest.x, oldDest.y, oldDest.z),
					newDest.world, Vec3i(newDest.x, newDest.y, newDest.z)
				)
			)
		}

		// remove old one if oldDest is not set to null
		if (oldDest != null) {
			removeShuttleAction(Triple(schematicName, oldDest.world, Vec3i(oldDest.x, oldDest.y, oldDest.z)))
		}

		// update database
		Shuttle.moveLocation(shuttle._id, if (newPosition == -1) 0 else newPosition)
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
