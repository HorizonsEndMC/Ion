package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.common.utils.DoubleLocation
import dev.cubxity.plugins.metrics.api.UnifiedMetricsProvider
import java.util.EnumSet
import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.text.Component
import net.milkbowl.vault.economy.Economy
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Shulker
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.chunk.ChunkStatus
import net.starlegacy.util.Tasks
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.*
import net.starlegacy.util.Vec3i
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk
import org.bukkit.entity.Entity

val vaultEconomy = try {
	Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
} catch (exception: NoClassDefFoundError) {
	null
}

val metrics =
	if (Bukkit.getPluginManager().isPluginEnabled("UnifiedMetrics")) UnifiedMetricsProvider.get() else null

val gayColors = arrayOf(
	Color.fromRGB(255, 0, 24),
	Color.fromRGB(255, 165, 44),
	Color.fromRGB(255, 255, 65),
	Color.fromRGB(0, 128, 24),
	Color.fromRGB(0, 0, 249),
	Color.fromRGB(134, 0, 125)
)

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

fun Player.worldBorderEffect(duration: Long) {
	val start = ClientboundSetBorderWarningDistancePacket(WorldBorder().apply { this.warningBlocks = Int.MAX_VALUE })
	val end = ClientboundSetBorderWarningDistancePacket(
		WorldBorder().apply { this.warningBlocks = this@worldBorderEffect.worldBorder?.warningDistance ?: 0 }
	)

	this.minecraft.connection.send(start)

	Tasks.syncDelayTask(duration) {
		this.minecraft.connection.send(end)
	}
}

fun Location.triple() = DoubleLocation(x, y, z)
fun Sign.lines(lines: Array<Component>) {
	for (line in 0..3) {
		this.line(line, lines[line])
	}
}

fun <K>MutableMap<K, Int>.putOrIterate(new: K): Unit = if (this.containsKey(new)) this[new] = this[new]!! + 1 else this[new] = 1

fun <K>Collection<Pair<K, *>>.firsts(): List<K> = this.map { it.first }
fun <V>Collection<Pair<*, V>>.seconds(): List<V> = this.map { it.second }

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
