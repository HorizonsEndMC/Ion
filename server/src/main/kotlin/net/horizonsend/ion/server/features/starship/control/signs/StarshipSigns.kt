package net.horizonsend.ion.server.features.starship.control.signs

import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import java.util.Locale
import java.util.UUID

enum class StarshipSigns(val undetectedText: String, val baseLines: Array<Component?>) {
	CRUISE("[cruise]", arrayOf(
		text("Cruise", AQUA),
		text("Control", DARK_GRAY),
		text("- Look Direction", RED),
		text("- /cruise ", RED))) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val starship = findPilotedPlayerStarship(player) ?: return
			val controller = ActiveStarships.findByPilot(player)?.controller ?: return

			if (rightClick) {
				val dir = player.location.direction.setY(0).normalize()

				StarshipCruising.startCruising(controller, starship, dir)
			} else {
				StarshipCruising.stopCruising(controller, starship)
			}
		}
	},
	HELM("[helm]", arrayOf(
		text("\\  ||  /"),
		text("==      =="),
		text("/  ||  \\"),
		text("Press Q or F", RED))) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val starship = findPilotedPlayerStarship(player) ?: return

			starship.tryRotate(rightClick)
		}
	},
	NODE("[node]", arrayOf(text("Node", GOLD), null, null, null)),
	WEAPON_SET("[weaponset]", arrayOf(text("Weapon Set", DARK_RED), null, null, null)) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val starship = findPlayerStarship(player) ?: return
			val set = sign.front().line(1).plainText().lowercase(Locale.getDefault())

			if (!starship.weaponSets.containsKey(set)) {
				player.userError("No nodes for $set")
				return
			}

			val current: UUID? = starship.weaponSetSelections.inverse().remove(set)

			if (current != null) {
				if (current == player.uniqueId) {
					player.information("Released weapon set $set")
					return
				}

				informOfSteal(current, starship, player, set)
			}

			starship.weaponSetSelections[player.uniqueId] = set

			player.information("Took control of weapon set $set")
		}
	},
	POWER_MODE("[powermode]", arrayOf(
		text("<", RED)
			.append(text("<", GREEN))
			.append(text("<", DARK_BLUE))
			.append(text("Power Mode", LIGHT_PURPLE))
			.append(text(">", DARK_BLUE))
			.append(text(">", GREEN))
			.append(text(">", RED)),
		null, null, null)) {
		private fun getPower(line: String): Int = line.split(" ")[1].toInt()

		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			val shield = getPower(sign.front().line(1).plainText())
			val weapon = getPower(sign.front().line(2).plainText())
			val thruster = getPower(sign.front().line(3).plainText())

			player.performCommand("powermode $shield $weapon $thruster")
		}

		private val prefixes = arrayOf(
			text("n/a"),
			text("Shield", DARK_AQUA).append(text(":", DARK_GRAY)).append(text(" ", AQUA)),
			text("Weapon", DARK_RED).append(text(":", DARK_GRAY)).append(text(" ", RED)),
			text("Thruster", GOLD).append(text(":", DARK_GRAY)).append(text(" ", YELLOW))
		)

		override fun onDetect(player: Player, sign: Sign): Boolean {
			var total = 0

			for (i in 1..3) {
				val lineText = sign.front().line(i).plainText()
				try {
					val number = Integer.parseInt(lineText)
					if (number < 10) {
						player.userError("Percentage must be at least 10")
						return false
					} else if (number > 50) {
						player.userError("Percentage must be no more than 50")
						return false
					}
					total += number
					sign.front().line(i, prefixes[i].append(text(number)))
				} catch (_: NumberFormatException) {
					player.userError("$lineText isn't a number")
					return false
				}
			}

			if (total != 100) {
				player.userError("Total is $total, but it needs to be 100")
				return false
			}
			return true
		}
	},
	/*
	BOARDING_RAMP("[boardingramp]", arrayOf(BoardingRamps.FIRST_LINE, null, null, null)) {
		override fun onDetect(player: Player, sign: Sign): Boolean {
			return BoardingRamps.shut(player, sign)
		}

		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			BoardingRamps.toggle(player, sign)
		}
	},
	 */
	DIRECT_CONTROL("[directcontrol]", arrayOf(text("Direct", DARK_GREEN), text("Control", DARK_GRAY), null, null)) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			MiscStarshipCommands.onDirectControl(player)
		}
	},
	// Because I couldn't figure out how to make the original enum accept a [dc]
	DIRECT_CONTROL_2("[dc]", arrayOf(text("Direct", DARK_GREEN), text("Control", DARK_GRAY), null, null)) {
		override fun onClick(player: Player, sign: Sign, rightClick: Boolean) {
			MiscStarshipCommands.onDirectControl(player)
		}
	};

	open fun onDetect(player: Player, sign: Sign): Boolean = true

	open fun onClick(player: Player, sign: Sign, rightClick: Boolean) {}

	protected fun findPlayerStarship(player: Player): ActiveControlledStarship? {
		val activeStarship = ActiveStarships.findByPassenger(player)

		if (activeStarship == null) {
			player.userError("You can only use this in an active player starship")
			return null
		}

		return activeStarship
	}

	protected fun findPilotedPlayerStarship(player: Player): ActiveControlledStarship? {
		val starship = ActiveStarships.findByPassenger(player)

		if (starship == null) {
			player.userError("You can only use this in an active player starship")
			return null
		}

		if (starship.playerPilot != player) {
			player.userError("You must be the pilot of the starship to do this!")
			return null
		}

		return starship
	}

	companion object {
		fun informOfSteal(current: UUID, starship: ActiveControlledStarship, player: Player, set: String) {
			// only message if the player is still online
			val currentPlayer = Bukkit.getPlayer(current)
			if (currentPlayer != null && starship.isPassenger(current)) {
				currentPlayer.information("${player.name} took over weapon set $set from you")
				player.information("Took weapon set $set from ${currentPlayer.name}")
			}
		}
	}
}
