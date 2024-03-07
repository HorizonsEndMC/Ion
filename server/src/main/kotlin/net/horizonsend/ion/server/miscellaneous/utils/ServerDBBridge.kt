package net.horizonsend.ion.server.miscellaneous.utils

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.database.SLTextStyleDB
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.luckPerms
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.StarshipSchematic
import net.horizonsend.ion.server.features.starship.StarshipType
import net.kyori.adventure.text.Component
import net.luckperms.api.model.user.User
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Base64
import java.util.UUID

val SLTextStyleDB.actualStyle get() = SLTextStyle.valueOf(this)

val StarshipTypeDB.actualType get() = StarshipType.valueOf(this)

val Player.slPlayerId: SLPlayerId get() = uniqueId.slPlayerId

operator fun SLPlayer.Companion.get(player: Player): SLPlayer = SLPlayer[player.uniqueId]
	?: error("Missing SLPlayer for online player ${player.name}")

fun StarshipData.bukkitWorld(): World = requireNotNull(Bukkit.getWorld(levelName)) {
	"World $levelName is not loaded, but tried getting it for computer $_id"
}

fun PlayerStarshipData.isPilot(player: Player): Boolean {
	val id = player.slPlayerId
	return captain == id || pilots.contains(id)
}

fun Cryopod.bukkitLocation() = Location(Bukkit.getWorld(worldName)!!, x.toDouble(), y.toDouble(), z.toDouble())

fun Blueprint.Companion.createData(schematic: Clipboard): String {
	return Base64.getEncoder().encodeToString(StarshipSchematic.serializeSchematic(schematic))
}

fun Blueprint.Companion.parseData(data: String): Clipboard {
	return StarshipSchematic.deserializeSchematic(Base64.getDecoder().decode(data))
}

fun Blueprint.loadClipboard(): Clipboard {
	return Blueprint.parseData(blockData)
}

fun Blueprint.canAccess(player: Player): Boolean {
	val slPlayerId = player.slPlayerId
	return slPlayerId == owner || trustedPlayers.contains(slPlayerId) || trustedNations.contains(PlayerCache[player].nationOid)
}

class CommonPlayerWrapper(private val inner: Player) : CommonPlayer {
	override val name: String = inner.name
	override val uniqueId: UUID = inner.uniqueId

	override fun getDisplayName(): Component  = inner.displayName()
	override fun getUser(): User = luckPerms.getPlayerAdapter(Player::class.java).getUser(inner)

	companion object {
		fun Player.common(): CommonPlayer = CommonPlayerWrapper(this)
	}
}
