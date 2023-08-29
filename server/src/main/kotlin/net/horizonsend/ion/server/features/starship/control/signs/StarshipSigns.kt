package net.horizonsend.ion.server.features.starship.control.signs

import net.horizonsend.ion.server.features.starship.BoardingRamps
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.StarshipCruising
import net.horizonsend.ion.server.features.starship.controllers.ActivePlayerController
import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.msg
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import java.util.Locale
import java.util.UUID

enum class StarshipSigns(val undetectedText: String, val baseLines: Array<String?>) {
	CRUISE("[cruise]", arrayOf("&3Cruise".colorize(), "&8Control".colorize(), "&c- Look Direction".colorize(), "&c- /cruise ".colorize())) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val starship = findPilotedPlayerStarship(player) ?: return
			val controller = ActivePlayerController[player] ?: return

			if (rightClick) {
				StarshipCruising.startCruising(controller, starship)
			} else {
				StarshipCruising.stopCruising(controller, starship)
			}
		}
	},
	HELM("[helm]", arrayOf("\\  ||  /", "==      ==", "/  ||  \\", "&cPress Q or F".colorize())) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val starship = findPilotedPlayerStarship(player) ?: return

			starship.tryRotate(rightClick)
		}
	},
	NODE("[node]", arrayOf("&6Node".colorize(), null, null, null)),
	WEAPON_SET("[weaponset]", arrayOf("&4Weapon Set".colorize(), null, null, null)) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val starship = findPlayerStarship(player) ?: return
			val lines = sign.lines
			val set = lines[1].lowercase(Locale.getDefault())

			if (!starship.weaponSets.containsKey(set)) {
				player msg "&cNo nodes for $set"
				return
			}

			val current: UUID? = starship.weaponSetSelections.inverse().remove(set)

			if (current != null) {
				if (current == player.uniqueId) {
					player msg "&7Released weapon set &b$set"
					return
				}

				informOfSteal(current, starship, player, set)
			}

			starship.weaponSetSelections[player.uniqueId] = set

			player msg "&7Took control of weapon set &b$set"
		}
	},
	POWER_MODE("[powermode]", arrayOf("&c<&a<&1<&dPower Mode&1>&a>&c>".colorize(), null, null, null)) {
		private fun getPower(line: String): Int = line.split(" ")[1].toInt()

		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val lines = sign.lines

			val shield = getPower(lines[1])
			val weapon = getPower(lines[2])
			val thruster = getPower(lines[3])

			player.performCommand("powermode $shield $weapon $thruster")
		}

		private val prefixes = arrayOf(
			"n/a",
			"&3Shield&8:&b ".colorize(),
			"&4Weapon&8:&c ".colorize(),
			"&6Thruster&8:&e ".colorize()
		)

		override fun onDetect(player: Player, sign: Sign): Boolean {
			var total = 0

			for (i in 1..3) {
				val text = sign.getLine(i)
				try {
					val number = Integer.parseInt(text)
					if (number < 10) {
						player.msg("Percentage must be at least 10")
						return false
					} else if (number > 50) {
						player.msg("Percentage must be no more than 50")
						return false
					}
					total += number
					sign.setLine(i, prefixes[i] + number)
				} catch (e: NumberFormatException) {
					player.msg("$text isn't a number")
					return false
				}
			}

			if (total != 100) {
				player.msg("Total is $total, but it needs to be 100")
				return false
			}
			return true
		}
	},
	BOARDING_RAMP("[boardingramp]", arrayOf(BoardingRamps.FIRST_LINE, null, null, null)) {
		override fun onDetect(player: Player, sign: Sign): Boolean {
			return BoardingRamps.shut(player, sign)
		}

		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			BoardingRamps.toggle(player, sign)
		}
	};

	open fun onDetect(player: Player, sign: Sign): Boolean = true

	open fun onClick(player: Player, sign: Sign, rightClick: Boolean) {}

	protected fun findPlayerStarship(player: Player): ActiveControlledStarship? {
		val activeStarship = ActiveStarships.findByPassenger(player) as? ActiveControlledStarship

		if (activeStarship == null) {
			player msg "&cYou can only use this in an active player starship"
			return null
		}

		return activeStarship
	}

	protected fun findPilotedPlayerStarship(player: Player): ActiveControlledStarship? {
		val starship = ActiveStarships.findByPassenger(player) as? ActiveControlledStarship

		if (starship == null) {
			player msg "&cYou can only use this in an active player starship"
			return null
		}

		if (starship.playerPilot != player) {
			player msg "&cYou must be the pilot of the starship to do this!"
			return null
		}

		return starship
	}

	companion object {
		fun informOfSteal(current: UUID, starship: ActiveControlledStarship, player: Player, set: String) {
			// only message if the player is still online
			val currentPlayer = Bukkit.getPlayer(current)
			if (currentPlayer != null && starship.isPassenger(current)) {
				currentPlayer msg "&e${player.name} took over weapon set $set from you"
				player msg "&eTook weapon set $set from ${currentPlayer.name}"
			}
		}
	}
}
