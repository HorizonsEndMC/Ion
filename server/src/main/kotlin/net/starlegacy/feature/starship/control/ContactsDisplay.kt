package net.starlegacy.feature.starship.control

import net.horizonsend.ion.server.legacy.commands.GracePeriod
import net.horizonsend.ion.server.IonComponent
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.listen
import net.starlegacy.util.Tasks
import net.starlegacy.util.colorize
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.Bukkit.getScoreboardManager
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.util.Vector
import java.util.Collections
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

object ContactsDisplay : IonComponent() {
	const val range = 6000
	const val sqRange = range * range

	const val objectiveName = "contacts"

	val scoreboards: MutableMap<UUID, Scoreboard> = Collections.synchronizedMap(mutableMapOf<UUID, Scoreboard>())

	override fun onEnable() {
		listen<PlayerJoinEvent> { e ->
			val scoreboard = getScoreboardManager().newScoreboard
			scoreboards[e.player.uniqueId] = scoreboard
			e.player.scoreboard = scoreboard
		}

		listen<PlayerQuitEvent> { e ->
			scoreboards.remove(e.player.uniqueId)
		}

		Tasks.asyncRepeat(2L, 2L, ContactsDisplay::update)
		getOnlinePlayers().forEach {
			val scoreboard = scoreboards.computeIfAbsent(it.uniqueId) { getScoreboardManager().newScoreboard }
			if (it.scoreboard != scoreboard) it.scoreboard = scoreboard
		}
	}

	@Synchronized
	private fun update() {
		for (player in getOnlinePlayers()) {
			val scoreboard = scoreboards[player.uniqueId] ?: continue

			scoreboard.entries.forEach(scoreboard::resetScores)

			val objective: Objective = scoreboard.getObjective(objectiveName)
				?: scoreboard.registerNewObjective(objectiveName, "dummy")

			val loc: Location = player.location

			val playerDirection: Vector = loc.direction
			playerDirection.setY(0)
			playerDirection.normalize()

			var directionString = " "

			if (playerDirection.z != 0.0 && abs(playerDirection.z) > 0.4) {
				directionString += if (playerDirection.z > 0) "south" else "north"
			}

			if (playerDirection.x != 0.0 && abs(playerDirection.x) > 0.4) {
				directionString += if (playerDirection.x > 0) "east " else "west "
			}

			var worldName = player.world.key.toString().substringAfterLast(":")
				.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

			if (worldName == "Overworld") {
				worldName = player.world.name
					.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
			}

			objective.displayName = "&2${worldName.split("_")[0]}&a$directionString".colorize()
			objective.displaySlot = DisplaySlot.SIDEBAR

			val players: List<Player> = if (worldName != "Tutorial") {
				getOnlinePlayers().filter {
					it.world == player.world && it !== player && it.gameMode != GameMode.SPECTATOR &&
						it.location.distanceSquared(loc) <= sqRange &&
						ActiveStarships.findByPilot(it) != null
				}.sortedBy { it.location.distanceSquared(loc) }.reversed()
			} else {
				listOf()
			}

			val vec: Vector = loc.toVector()
			val locationString = (
				"&bx:${vec.blockX} y:${vec.blockY} z:${vec.blockZ}&7 " +
					"${loc.yaw.toInt()}/${loc.pitch.toInt()}"
				).colorize()

			val infoString = StringBuilder(locationString.colorize())

			var spaces = max(0, 40 - infoString.length)
			for (i in 0 until spaces) {
				infoString.append(" ")
			}

			objective.getScore(infoString.toString()).score = players.size

			for (score in players.indices) {
				val otherPlayer = players[score]
				var color = ChatColor.GRAY
				val distance = otherPlayer.location.distance(loc).toInt()

				when {
					distance < 500 -> color = ChatColor.RED
					distance < 1500 -> color = ChatColor.GOLD
					distance < 2500 -> color = ChatColor.YELLOW
					distance < 3500 -> color = ChatColor.DARK_GREEN
					distance < 6000 -> color = ChatColor.GREEN
				}

				var text = StringBuilder(color.toString() + otherPlayer.name)
				val vector = otherPlayer.location.toVector()
				val diff = vector.clone().subtract(loc.toVector()).normalize()

				var direction = ""

				if (diff.z != 0.0 && abs(diff.z) > 0.4) {
					direction += if (diff.z > 0) "S" else "N"
				}

				if (diff.x != 0.0 && abs(diff.x) > 0.4) {
					direction += if (diff.x > 0) "E" else "W"
				}

				val info = String.format("%s %dy %dm", direction, vector.blockY, distance)
				spaces = max(0, 40 - text.length - info.length)
				for (i in 0 until spaces) {
					text.append(" ")
				}
				text.append(info)

				if (text.length > 40) {
					println("$text is longer than 40!")

					text = StringBuilder(otherPlayer.name)
					if (ActiveStarships.findByPilot(otherPlayer)!!.isInterdicting) {
						text = StringBuilder(otherPlayer.name + "⍉")
					}
					if (GracePeriod.isGracePeriod) {
						text = java.lang.StringBuilder(otherPlayer.name + "☆")
					} else if (GracePeriod.isGracePeriod && ActiveStarships.findByPilot(otherPlayer)!!.isInterdicting) {
						text =
							java.lang.StringBuilder(otherPlayer.name + "☆" + "⍉")
					}
				}

				objective.getScore(text.toString()).score = score
			}
//            if(player.scoreboard != scoreboard) player.scoreboard = scoreboard
		}
	}
}
