package net.horizonsend.ion.server.miscellaneous.utils

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.common.database.SLTextStyleDB
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.StarshipSchematic
import net.horizonsend.ion.server.features.starship.StarshipType
import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.Base64

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

class Vec3i: DBVec3i {
	constructor(a: DBVec3i) : super(a.x, a.y, a.z)
	constructor(x: Int, y: Int, z: Int) : super(x, y, z)
	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	constructor(blockKey: Long) : super(blockKeyX(blockKey), blockKeyY(blockKey), blockKeyZ(blockKey))

	constructor(vector: Vector) : super(vector.blockX, vector.blockY, vector.blockZ)

	constructor(location: Location) : super(location.blockX, location.blockY, location.blockZ)

	constructor(blockPos: BlockPos) : super(blockPos.x, blockPos.y, blockPos.z)

	constructor(vector: BlockVector3) : super(vector.blockX, vector.blockY, vector.blockZ)

	override fun toString() = "$x,$y,$z"

	fun toLocation(world: World): Location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

	@Deprecated("Star Legacy's blockKey is not the same as Minecraft's blockKey")
	fun toBlockKey(): Long = blockKey(x, y, z)

	fun toVector(): Vector = Vector(x, y, z)
	fun toCenterVector(): Vector = Vector(x.toDouble() + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)

	fun distance(x: Int, y: Int, z: Int): Double = distance(this.x, this.y, this.z, x, y, z)

	/**
	 * @param other Vector that should be added from this one
	 * @return A new vector with the values added
	 **/
	operator fun plus(other: Vec3i) = Vec3i(x + other.x, y + other.y, z + other.z)

	/**
	 * @param other Vector that should be subtracted from this one
	 * @return A new vector with the values subtracted
	 **/
	operator fun minus(other: Vec3i) = Vec3i(x - other.x, y - other.y, z - other.z)
}
