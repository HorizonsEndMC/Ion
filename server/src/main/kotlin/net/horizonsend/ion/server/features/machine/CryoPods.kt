package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.trx
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.misc.CryoPodMultiblock
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.bukkitLocation
import net.horizonsend.ion.server.miscellaneous.utils.get
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.litote.kmongo.addToSet
import org.litote.kmongo.setValue
import java.util.UUID

object CryoPods: SLEventListener() {
	fun setCryoPod(playerID: UUID, worldName: String, pos: Vec3i) = trx { session ->
		val slPlayer = SLPlayer[playerID] ?: return@trx

		val newCryoPod = Cryopod.create(slPlayer, pos, worldName)

		SLPlayer.updateById(
			session,
			slPlayer._id,
			addToSet(SLPlayer::cryopods, newCryoPod),
			setValue(SLPlayer::selectedCryopod, newCryoPod)
		)
	}

	private fun removeCryoPod(playerID: UUID) = SLPlayer[playerID]?.selectedCryopod?.let { Cryopod.delete(it) }

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerInteractSelectOrDeselectCryoPod(event: PlayerInteractEvent) {
		val sign = event.clickedBlock?.state as? Sign ?: return
		val multiblock = Multiblocks[sign] as? CryoPodMultiblock ?: return
		val player = event.player
		val pos = Vec3i(sign.location)
		if (!CryoPodMultiblock.isOwner(sign, player)) {
			player.userError("You aren't the owner of this cryo pod!")
			return
		}

		val selectedCryo = SLPlayer[player].selectedCryopod?.let { Cryopod.findById(it) }

		if (event.action == Action.LEFT_CLICK_BLOCK) {
			if (selectedCryo?.vec3i() != pos) {
				player.userError("This is not your selected cryo pod!")
				return
			}

			removeCryoPod(player.uniqueId)
			player.information("Deselected cryo pod!")
		} else if (event.action == Action.RIGHT_CLICK_BLOCK) {
			if (selectedCryo?.vec3i() == pos) {
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
		val cryoPod = SLPlayer[event.player].selectedCryopod?.let { Cryopod.findById(it) } ?: return

		Bukkit.getWorld(cryoPod.worldName)
			?: return player.serverError("World ${cryoPod.worldName} is missing")

		val pos = cryoPod.bukkitLocation()
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

		Cryopod[Vec3i(sign.location), sign.world.name]?.let { Cryopod.delete(it._id) }
	}
}
