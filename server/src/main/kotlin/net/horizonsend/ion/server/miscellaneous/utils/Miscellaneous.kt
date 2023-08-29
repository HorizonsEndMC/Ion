package net.horizonsend.ion.server.miscellaneous.utils

import dev.cubxity.plugins.metrics.api.UnifiedMetricsProvider
import net.horizonsend.ion.common.utils.DoubleLocation
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.admin.IonCommand
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.machine.AreaShields
import net.milkbowl.vault.economy.Economy
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.chunk.ChunkStatus
import net.minecraft.world.level.chunk.LevelChunk
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.EnumSet

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

fun <K> Collection<Pair<K, *>>.firsts(): List<K> = this.map { it.first }
fun <V> Collection<Pair<*, V>>.seconds(): List<V> = this.map { it.second }
fun <K, V : Comparable<V>> Map<K, V>.keysSortedByValue(): List<K> = this.keys.sortedBy { this[it]!! }

val Chunk.minecraft: LevelChunk get() = (this as CraftChunk).getHandle(ChunkStatus.FULL) as LevelChunk // ChunkStatus.FULL guarantees a LevelChunk
val Player.minecraft: ServerPlayer get() = (this as CraftPlayer).handle
val World.minecraft: ServerLevel get() = (this as CraftWorld).handle

fun runnable(e: BukkitRunnable.() -> Unit): BukkitRunnable = object : BukkitRunnable() {
	override fun run() = e()
}

@Suppress("UNCHECKED_CAST")
fun <T : Entity> World.castSpawnEntity(location: Location, type: org.bukkit.entity.EntityType) =
	this.spawnEntity(location, type) as T

fun debugHighlightBlock(x: Number, y: Number, z: Number) {
	IonCommand.debugEnabledPlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach {
		if (it.location.distanceSquared(
				Location(
					it.world,
					x.toDouble(),
					y.toDouble(),
					z.toDouble()
				)
			) > 30 * 30
		) return@forEach

		highlightBlock(it, BlockPos(x.toInt(), y.toInt(), z.toInt()), 5L)
	}
}

fun areaDebugMessage(x: Number, y: Number, z: Number, msg: String) {
	IonCommand.debugEnabledPlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach {
		if (it.location.distanceSquared(
				Location(
					it.world,
					x.toDouble(),
					y.toDouble(),
					z.toDouble()
				)
			) > 30 * 30
		) return@forEach

		it.debug(msg)
	}
}

fun highlightBlock(bukkitPlayer: Player, pos: BlockPos, duration: Long) {
	val player = bukkitPlayer.minecraft
	val conn = player.connection
	val slime =
		Slime(EntityType.SLIME, player.level).apply {
			setPos(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
			setGlowingTag(true)
			isInvisible = true
		}

	conn.send(ClientboundAddEntityPacket(slime))
	slime.entityData.refresh(player)

	Tasks.syncDelayTask(duration) { conn.send(ClientboundRemoveEntitiesPacket(slime.id)) }
}

fun repeatString(string: String, count: Int): String {
	val builder = StringBuilder()

	for (x in 0 until count) {
		builder.append(string)
	}

	return builder.toString()
}

/**
 * Abuse explosion regen to allow block changes to regenerate
 *
 * @param source: The source entity
 * @param origin: The origin block
 * @param changedBlocks: The list of blocks (before they were changed)
 * @param yield: The yield of the explosion, for the event call
 * @param bypassAreaShields: Whether this block change will bypass area shields
 *
 * @return whether the explosion was cancelled
 **/
fun regeneratingBlockChange(source: Entity?, origin: Block, changedBlocks: MutableList<Block>, yield: Float, bypassAreaShields: Boolean): Boolean {
	val world = origin.world
	val location = origin.location.toCenterLocation()
	val blockExplodeEvent = BlockExplodeEvent(origin, changedBlocks, yield)

	if (bypassAreaShields) AreaShields.bypassShieldEvents.add(blockExplodeEvent)

	if (source != null) origin.world.createExplosion(source, 1f, false, false) else
		world.createExplosion(location, 1f, false, false)

	world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 10f, 0.5f)

	return blockExplodeEvent.callEvent()
}
