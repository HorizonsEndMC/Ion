package net.horizonsend.ion.server.miscellaneous

import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.server.features.cache.nations.PlayerCache
import net.starlegacy.feature.starship.StarshipSchematic
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.util.*
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Blue
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.*

val Player.slPlayerId: SLPlayerId get() = uniqueId.slPlayerId

operator fun SLPlayer.Companion.get(player: Player): SLPlayer = SLPlayer[player.uniqueId]
	?: error("Missing SLPlayer for online player ${player.name}")

fun PlayerStarshipData.bukkitWorld(): World = requireNotNull(Bukkit.getWorld(levelName)) {
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

fun Blueprint.Companion.get(owner: SLPlayerId, name: String): Blueprint? {
	return Blueprint.col.findOne(and(Blueprint::owner eq owner, Blueprint::name eq name))
}

fun Blueprint.Companion.create(owner: SLPlayerId, name: String, type: StarshipType, pilotLoc: Vec3i, size: Int, data: String) {
	Blueprint.col.insertOne(Blueprint(objId(), owner, name, type.name, pilotLoc, size, data))
}

fun Blueprint.loadClipboard(): Clipboard {
	return Blueprint.parseData(blockData)
}

fun Blueprint.canAccess(player: Player): Boolean {
	val slPlayerId = player.slPlayerId
	return slPlayerId == owner || trustedPlayers.contains(slPlayerId) || trustedNations.contains(PlayerCache[player].nationOid)
}

class Vec3i: DBVec3i {
	constructor(a: DBVec3i) : this(a.x, a.y, a.z)
	constructor(x: Int, y: Int, z: Int) : super(x, y, z)
	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	constructor(blockKey: Long) : super(blockKeyX(blockKey), blockKeyY(blockKey), blockKeyZ(blockKey))

	constructor(vector: Vector) : super(vector.blockX, vector.blockY, vector.blockZ)

	constructor(location: Location) : super(location.blockX, location.blockY, location.blockZ)

	override fun toString() = "$x,$y,$z"

	fun toLocation(world: World?): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	fun toBlockKey(): Long = blockKey(x, y, z)

	fun toVector(): Vector = Vector(x, y, z)
	fun toCenterVector(): Vector = Vector(x.toDouble() + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)

	fun distance(x: Int, y: Int, z: Int): Double = distance(this.x, this.y, this.z, x, y, z)

	operator fun plus(other: Vec3i) = Vec3i(x + other.x, y + other.y, z + other.z)
	operator fun minus(other: Vec3i) = Vec3i(x - other.x, y - other.y, z - other.z)
}
