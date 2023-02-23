package net.starlegacy.feature.misc

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.gson.Gson
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.miscellaneous.extensions.alert
import net.horizonsend.ion.server.miscellaneous.extensions.information
import net.horizonsend.ion.server.miscellaneous.extensions.serverError
import net.horizonsend.ion.server.miscellaneous.extensions.userError
import net.starlegacy.SLComponent
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.misc.CryoPodMultiblock
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Optional
import java.util.UUID

object CryoPods : SLComponent() {
	private val folder = File(Ion.dataFolder, "cryopods")

	override fun onEnable() {
		folder.mkdir()
	}

	private fun getFile(playerID: UUID): File {
		val file = File(folder, "$playerID.json")
		return file
	}

	data class CryoPod(val player: UUID, val world: String, val pos: Vec3i)

	private val cryoPods: LoadingCache<UUID, Optional<CryoPod>> = CacheBuilder.newBuilder()
		.weakKeys()
		.build(CacheLoader.from { id -> if (id == null) Optional.empty() else loadCryoPod(id) })

	private fun loadCryoPod(playerID: UUID): Optional<CryoPod> {
		val file = getFile(playerID)
		if (!file.exists()) {
			return Optional.empty()
		}
		FileReader(file).use { reader ->
			return Optional.of(Gson().fromJson(reader, CryoPod::class.java))
		}
	}

	private fun getCryoPod(playerID: UUID): CryoPod? {
		return cryoPods.get(playerID).orElse(null)
	}

	fun setCryoPod(playerID: UUID, worldName: String, pos: Vec3i) {
		val cryoPod = CryoPod(playerID, worldName, pos)
		cryoPods.put(playerID, Optional.of(cryoPod))
		val file = getFile(playerID)
		FileWriter(file).use { writer ->
			Gson().toJson(cryoPod, writer)
		}
	}

	private fun removeCryoPod(playerID: UUID) {
		cryoPods.put(playerID, Optional.empty())
		val file = getFile(playerID)
		file.delete()
	}

	operator fun get(sign: Sign, checkStructure: Boolean = false): CryoPod? {
		if (Multiblocks[sign, checkStructure] !is CryoPodMultiblock) {
			return null
		}
		val playerID: UUID = CryoPodMultiblock.getOwner(sign)
			?: return null
		return getCryoPod(playerID)
	}

	@EventHandler
	fun onPlayerJoinLoadCryoPodAsynchronously(event: PlayerJoinEvent) {
		Tasks.async {
			getCryoPod(event.player.uniqueId)
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerInteractSelectOrDeselectCryoPod(event: PlayerInteractEvent) {
		val sign = event.clickedBlock?.state as? Sign ?: return
		val multiblock = Multiblocks[sign] as? CryoPodMultiblock ?: return
		val player = event.player
		val pos = Vec3i(sign.location)
		if (!multiblock.isOwner(sign, player)) {
			player.userError("You aren't the owner of this cryo pod!")
			return
		}

		val cryoPod: CryoPod? = getCryoPod(player.uniqueId)

		if (event.action == Action.LEFT_CLICK_BLOCK) {
			if (cryoPod?.pos != pos) {
				player.userError("This is not your selected cryo pod!")
				return
			}

			removeCryoPod(player.uniqueId)
			player.information("Deselected cryo pod!")
		} else if (event.action == Action.RIGHT_CLICK_BLOCK) {
			if (cryoPod?.pos == pos) {
				player.userError("This is already your selected cryo pod!")
				return
			}

			setCryoPod(player.uniqueId, sign.world.name, pos)
			player.information("Selected cryo pod!")
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlayerRespawnSetLocationToCryoPod(event: PlayerRespawnEvent) {
		val player = event.player

		val cryoPod: CryoPod = getCryoPod(player.uniqueId) ?: return

		val world = Bukkit.getWorld(cryoPod.world)
			?: return player.serverError("World ${cryoPod.world} is missing")

		val pos = cryoPod.pos
		val loc = pos.toLocation(world)

		val sign = loc.block.state as? Sign
			?: return player.alert("Cryo pod sign at $pos is missing")

		if (Multiblocks[sign, true, true] !is CryoPodMultiblock) {
			return player.alert("Cryo pod at $pos is not intact")
		}

		event.respawnLocation = loc.add(0.5, -1.0, 0.5)
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		val sign = event.block.state as? Sign ?: return
		val pod = get(sign) ?: return
		if (Vec3i(sign.location) == pod.pos) {
			val uuid = pod.player
			removeCryoPod(uuid)
			Notify.player(uuid, "Cryo pod at ${pod.pos} destroyed")
		}
	}

	fun isSelected(pod: CryoPod): Boolean {
		return getCryoPod(pod.player) == pod
	}
}
