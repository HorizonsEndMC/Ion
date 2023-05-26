package net.horizonsend.ion.server.features.cryopods

import com.google.gson.Gson
import net.horizonsend.ion.common.database.Cryopod
import net.horizonsend.ion.common.database.DBLocation
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.miscellaneous.bukkit
import net.horizonsend.ion.server.miscellaneous.db
import net.horizonsend.ion.server.miscellaneous.get
import net.horizonsend.ion.server.miscellaneous.vec3i
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.util.Vec3i
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileReader
import java.io.FileWriter
import java.util.Optional
import java.util.UUID

object CryoPods: Listener {
	fun setCryoPod(playerID: UUID, worldName: String, pos: Vec3i) =
		transaction {
			val cryo = Cryopod[playerID].firstOrNull() ?: Cryopod.new {
				location = DBLocation(worldName, pos.triple())
				owner = PlayerData[playerID]!!
			}

			cryo.location = DBLocation(worldName, pos.triple())
			cryo.active = true
		}

	private fun removeCryoPod(playerID: UUID) = transaction {
		PlayerData[playerID]?.selectedCryopod?.delete()
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

		val selectedCryo = transaction { PlayerData[player].selectedCryopod }

		if (event.action == Action.LEFT_CLICK_BLOCK) {
			if (selectedCryo?.location?.vec3i() != pos) {
				player.userError("This is not your selected cryo pod!")
				return
			}

			removeCryoPod(player.uniqueId)
			player.information("Deselected cryo pod!")
		} else if (event.action == Action.RIGHT_CLICK_BLOCK) {
			if (selectedCryo?.location?.vec3i() == pos) {
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
		val cryoPod = transaction { PlayerData[player].selectedCryopod } ?: return

		Bukkit.getWorld(cryoPod.location.world)
			?: return player.serverError("World ${cryoPod.location.world} is missing")

		val pos = cryoPod.location.bukkit()
		val sign = pos.block.state as? Sign
			?: return player.alert("Cryo pod sign at $pos is missing")

		if (Multiblocks[sign, true, true] !is CryoPodMultiblock) {
			return player.alert("Cryo pod at $pos is not intact")
		}

		event.respawnLocation = pos.add(0.5, -1.0, 0.5)
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		val sign = event.block.state as? Sign ?: return

		transaction {
			Cryopod[sign.location.db()]?.delete()
		}
	}
}
