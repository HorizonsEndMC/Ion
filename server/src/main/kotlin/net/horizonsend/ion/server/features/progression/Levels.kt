package net.horizonsend.ion.server.features.progression

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles.sharedDataFolder
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max
import kotlin.math.roundToInt

/** Balancing config for e.g. level up cost */
internal lateinit var LEVEL_BALANCING: LevelsConfig

@Serializable
data class LevelsConfig(val creditsPerXP: Double = 2.5, val cost: CostSection = CostSection()) {
	@Serializable
	data class CostSection(val base: Int = 500, val increase: Double = 50.0, val step: Int = 10)
}

/** Maximum attainable level */
internal const val MAX_LEVEL = 100

object Levels : IonServerComponent() {
	private val queue = ConcurrentLinkedQueue<UUID>()

	override fun onEnable() {
		reloadConfig()

		Tasks.asyncRepeat(delay = 20, interval = 20) {
			queue.poll()?.let {
				tryLevelUp(it)
			}
		}
	}

	fun reloadConfig() {
		LEVEL_BALANCING = Configuration.load(sharedDataFolder, "level_balancing")
	}

	/** Marks the player to be checked in the secondly level-up check */
	fun markForCheck(playerID: UUID) {
		queue.offer(playerID)
	}

	private fun tryLevelUp(playerId: UUID) {
		val player: Player = Bukkit.getPlayer(playerId)
			?: return

		val name: String = player.name

		val slPlayer: SLPlayer = SLPlayer[playerId] ?: return

		val level: Int = slPlayer.level
		val currentXP: Int = slPlayer.xp

		doLevelUp(level, currentXP, playerId, player, name)
	}

	private fun doLevelUp(
		level: Int,
		currentXP: Int,
		playerID: UUID,
		player: Player,
		name: String,
		previousCost: Int = 0
	): Boolean {
		val newLevel = level + 1
		if (level >= MAX_LEVEL) return false

		val cost = getLevelUpCost(newLevel)
		if (cost > currentXP) return false

		PlayerXPLevelCache.addSLXP(playerID, -cost)
		PlayerXPLevelCache.setLevel(playerID, newLevel)

		// if this is the last level up then announce it
		if (!doLevelUp(newLevel, currentXP - cost, playerID, player, name, previousCost + cost)) {
			Tasks.sync {
				player.showTitle(Title.title(text("LEVEL UP!", DARK_PURPLE, TextDecoration.BOLD), text("Level $newLevel", GOLD, TextDecoration.ITALIC)))
				player.sendMessage(template(text("Leveled up to level {0} for {1} HEXP", DARK_PURPLE), GOLD, newLevel, previousCost + cost))

				val message = template(
					text("{0} leveled up to {1}!", GREEN),
					text(name, GOLD),
					text("Level $newLevel", DARK_PURPLE)
				)

				Notify.chatAndGlobal(message)

				when (newLevel) {
					in 80..100 -> Achievement.LEVEL_80
					in 40..100 -> Achievement.LEVEL_40
					in 20..100 -> Achievement.LEVEL_20
					in 10..100 -> Achievement.LEVEL_10
					else -> null
				}?.let {
					player.rewardAchievement(it)
				}
			}
		}

		return true
	}

	/**
	 * Method to calculate the cost to level up
	 *
	 * Current Equation:
	 * y = A * B^x
	 *
	 * Where `y` is XP cost,
	 * `A` is base cost in the config,
	 * `B` is multiplier in the config,
	 * and `x` is level
	 *
	 * If the given level is above the max level it returns -1
	 * @return The result of the equation, the cost to level up to nextLevel from the level below it.
	 */
	fun getLevelUpCost(nextLevel: Int): Int {
		if (nextLevel == 1) return 0 // no cost to level up to level 1

		val levelMax = MAX_LEVEL
		if (nextLevel > levelMax) return -1

		val base = LEVEL_BALANCING.cost.base
		val increase = LEVEL_BALANCING.cost.increase
		val step = LEVEL_BALANCING.cost.step

		val cost = (increase * nextLevel) * max(1, nextLevel / step) + base
		return cost.roundToInt()
	}

	private const val MIN_VALUE = 0
	private const val MAX_VALUE = 3999
	private val RN_M = arrayOf("", "M", "MM", "MMM")
	private val RN_C = arrayOf("", "C", "CC", "CCC", "CD", "d", "DC", "DCC", "DCCC", "CM")
	private val RN_X = arrayOf("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC")
	private val RN_I = arrayOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")

	fun toRomanNumeral(level: Int): String {
		if (level < MIN_VALUE || level > MAX_VALUE) {
			throw IllegalArgumentException(
				String.format(
					"The number must be in the range [%d, %d]",
					MIN_VALUE,
					MAX_VALUE
				)
			)
		}

		return StringBuilder()
			.append(RN_M[level / 1000])
			.append(RN_C[level % 1000 / 100])
			.append(RN_X[level % 100 / 10])
			.append(RN_I[level % 10])
			.toString()
	}

	/**
	 * Get cached level of an online player
	 * Requires the player to be online
	 *
	 * @param player The player to get the level of
	 * @return The level value stored in the player's cache
	 */
	fun getCached(player: Player): Int {
		require(player.isOnline)

		return PlayerXPLevelCache[player].level
	}

	/**
	 * @see getCached
	 */
	operator fun get(player: Player): Int = getCached(player)

}
