package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.IonServer
import net.milkbowl.vault.economy.Economy
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.monster.Shulker
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

val vaultEconomy = try {
	Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)?.provider
} catch (exception: NoClassDefFoundError) {
	null
}

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

val Player.minecraft: ServerPlayer
	get() = (this as CraftPlayer).handle

val World.minecraft: ServerLevel
	get() = (this as CraftWorld).handle

fun highlightBlock(bukkitPlayer: Player, pos: BlockPos) {
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

	Tasks.syncDelayTask(10 * 20) { conn.send(ClientboundRemoveEntitiesPacket(shulker.id)) }
}
