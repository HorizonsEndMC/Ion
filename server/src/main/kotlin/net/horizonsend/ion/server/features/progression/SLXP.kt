package net.horizonsend.ion.server.features.progression

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.progression.SLXP.getCached
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.Future

object SLXP : IonServerComponent() {
	/**
	 * Gives XP to the specified player.
	 * Not guaranteed to update instantly.
	 *
	 * @param player The player, online or offline, to give XP to
	 * @param amount The amount of XP to give to the player
	 * @param message Whether to message the player after giving them the XP, if they are online.
	 */
	fun addAsync(player: OfflinePlayer, amount: Int, message: Boolean = true): Future<*> {
		return addAsync(player.uniqueId, amount, message)
	}

	/**
	 * Gives XP to the specified player.
	 * Not guaranteed to update instantly.
	 *
	 * @param uuid The UUID of the player to give XP to
	 * @param amount The amount of XP to give to the player
	 * @param message Whether to message the player after giving them the XP, if they are online.
	 */
	fun addAsync(uuid: UUID, amount: Int, message: Boolean = true): Future<*> = PlayerXPLevelCache.async {
		addSLXP(uuid, amount)

		// everything after this requires the player to be online
		val player = Bukkit.getPlayer(uuid) ?: return@async

		if (message) {
			player.success("Received $amount HEXP!")
		}

		Levels.markForCheck(uuid)
	}

	/**
	 * Gives [amount] of power to the specified player's [uuid], then sends them a [message] if online.
	 * Not guaranteed to update instantly.
	 */
	fun addPowerAsync(uuid: UUID, amount: Int, message: Boolean = true): Future<*> = PlayerXPLevelCache.async {
		val currentPower = fetchPower(uuid)
		val powerToAdd = (currentPower + amount).coerceIn(-20, 20) - currentPower // ensure that power cannot go below -20 or above 20
		addPower(uuid, powerToAdd)

		val player = Bukkit.getPlayer(uuid) ?: return@async

		if (message) {
			player.success("${if(amount > 0) ("Gained") else ("Lost")} $amount power")
		}
	}

	/**
	 * Sets the XP of the specified player.
	 * Not guaranteed to update instantly.
	 *
	 * @param player The player to set the XP of
	 * @param newValue What to set the XP to
	 */
	fun setAsync(player: OfflinePlayer, newValue: Int): Future<*> =
		PlayerXPLevelCache.async { setSLXP(player.uniqueId, newValue) }

	/**
	 * Sets the XP of the specified player.
	 * Not guaranteed to update instantly.
	 *
	 * @param uuid The UUID of the player to set the XP of
	 * @param newValue What to set the XP to
	 */
	fun setAsync(uuid: UUID, newValue: Int): Future<*> = PlayerXPLevelCache.async { setSLXP(uuid, newValue) }

	fun setPowerAsync(uuid: UUID, newValue: Int): Future<*> = PlayerXPLevelCache.async {
		setPower(uuid, newValue)
	}

	/**
	 * Get cached XP of an online player.
	 * Requires the player to be online.
	 *
	 * @param player The player to get the XP of
	 * @return The XP value stored in the player's cache
	 */
	fun getCached(player: Player): Int {
		require(player.isOnline)

		return PlayerXPLevelCache[player].xp
	}

	/**
	 * @see getCached
	 */
	operator fun get(player: Player): Int = getCached(player)

}
