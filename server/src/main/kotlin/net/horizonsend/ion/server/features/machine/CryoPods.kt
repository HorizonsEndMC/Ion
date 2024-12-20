package net.horizonsend.ion.server.features.machine

import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.bukkitLocation
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toChunkLocal
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.litote.kmongo.and
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.Instant
import java.util.Date

object CryoPods: SLEventListener() {
	fun updateOrCreate(player: Player, worldName: String, pos: Vec3i) = Tasks.async {
		val (x, y, z) = pos

		val existing = Cryopod.findOne(and(Cryopod::x eq x, Cryopod::y eq y, Cryopod::z eq z))

		// Update existing cryopod
		if (existing != null) {
			if (existing.owner != player.slPlayerId) {
				player.userError("That's not your cryopod!")
				return@async
			}

			Cryopod.updateById(existing._id, setValue(Cryopod::lastSelectedAt, Date.from(Instant.now())))
			player.success("Selected Cryopod")
			return@async
		}

		Cryopod.create(player.slPlayerId, pos, ConfigurationFiles.serverConfiguration().serverName ?: "Survival", worldName)
		player.success("Set Cryopod")
	}

	private fun removeCryoPod(world: World, location: Vec3i) {
		Cryopod.delete(location, world.name)
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlayerRespawn(event: PlayerRespawnEvent) {
		val player = event.player

		val cryopod = Cryopod.find(Cryopod::owner eq player.slPlayerId).sort(descending(Cryopod::lastSelectedAt)).firstOrNull() ?: return

		val world = Bukkit.getWorld(cryopod.worldName)

		if (world == null) {
			player.serverError("World ${cryopod.worldName} is missing!")
			return
		}

		val chunk = cryopod.bukkitLocation().chunk
		val vec = Vec3i(cryopod.x, cryopod.y, cryopod.z).toChunkLocal()
		val block = chunk.getBlock(vec.x, vec.y, vec.z)

		val signPosition = cryopod.bukkitLocation()
		val sign = block.state as? Sign

		if (sign == null) {
			player.serverError("Cryopod sign at ${cryopod.x}, ${cryopod.y}, ${cryopod.z} in ${cryopod.worldName} is missing!")
			return
		}

		if (!CryoPodMultiblock.signMatchesStructure(sign, loadChunks = true)) {
			player.serverError("Cryopod at ${cryopod.x}, ${cryopod.y}, ${cryopod.z} in ${cryopod.worldName} is not intact!")
			return
		}

		event.respawnLocation = signPosition.add(0.5, -1.0, 0.5)
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		if (event.block.state !is Sign) return

		removeCryoPod(event.block.world, Vec3i(event.block.location))
	}
}
