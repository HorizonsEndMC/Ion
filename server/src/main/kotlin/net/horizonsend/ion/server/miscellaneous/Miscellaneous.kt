package net.horizonsend.ion.server.miscellaneous

import dev.cubxity.plugins.metrics.api.UnifiedMetricsProvider
import net.horizonsend.ion.common.database.DBLocation
import net.horizonsend.ion.common.database.DoubleLocation
import net.horizonsend.ion.common.database.PlayerData
import java.util.EnumSet
import net.horizonsend.ion.server.IonServer
import net.milkbowl.vault.economy.Economy
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Shulker
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkStatus
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import net.minecraft.world.level.chunk.LevelChunk
import net.starlegacy.util.Vec3i
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk
import org.bukkit.entity.Entity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

val vaultEconomy = try {
	Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
} catch (exception: NoClassDefFoundError) {
	null
}

val metrics =
	if (Bukkit.getPluginManager().isPluginEnabled("UnifiedMetrics")) UnifiedMetricsProvider.get() else null

inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> =
	EnumSet.noneOf(T::class.java).apply { addAll(elems) }

/** Used for catching when a function that is not designed to be used async is being used async. */
fun mainThreadCheck() {
	if (!Bukkit.isPrimaryThread()) {
		IonServer.slF4JLogger.warn(
			"This function may be unsafe to use asynchronously.",
			Throwable()
		)
	}
}

fun <K>Collection<Pair<K, *>>.firsts(): List<K> = this.map { it.first }
fun <V>Collection<Pair<*, V>>.seconds(): List<V> = this.map { it.second }

operator fun PlayerData.Companion.get(player: Player) = PlayerData[player.uniqueId]!!
fun DBLocation.bukkit() = Location(Bukkit.getWorld(world)!!, coords.first, coords.second, coords.third)
fun DBLocation.vec3i() = Vec3i(coords.first.toInt(), coords.second.toInt(), coords.third.toInt())

fun Location.db() = DBLocation(world.name, DoubleLocation(x, y, z))

val Chunk.minecraft: LevelChunk get() = (this as CraftChunk).getHandle(ChunkStatus.FULL) as LevelChunk // ChunkStatus.FULL guarantees a LevelChunk
val Player.minecraft: ServerPlayer get() = (this as CraftPlayer).handle
val World.minecraft: ServerLevel get() = (this as CraftWorld).handle

fun runnable(e: BukkitRunnable.() -> Unit): BukkitRunnable = object : BukkitRunnable() {
	override fun run() = e()
}

@Suppress("UNCHECKED_CAST")
fun <T : Entity> World.castSpawnEntity(location: Location, type: org.bukkit.entity.EntityType) =
	this.spawnEntity(location, type) as T

fun highlightBlock(bukkitPlayer: Player, pos: BlockPos, duration: Long) {
	val player = bukkitPlayer.minecraft
	val conn = player.connection
	val shulker =
		Shulker(EntityType.SHULKER, player.level).apply {
			setPos(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
			setGlowingTag(true)
			isInvisible = true
		}

	conn.send(ClientboundAddEntityPacket(shulker))
	shulker.entityData.refresh(player)

	Tasks.syncDelayTask(duration) { conn.send(ClientboundRemoveEntitiesPacket(shulker.id)) }
}

fun repeatString(string: String, count: Int): String {
	val builder = StringBuilder()

	for (x in 0 until count) {
		builder.append(string)
	}

	return builder.toString()
}
