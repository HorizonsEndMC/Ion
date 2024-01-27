package net.horizonsend.ion.server.features.tutorial

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.HORIZONS_END
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.destruction.StarshipDestruction
import net.horizonsend.ion.server.features.starship.event.StarshipComputerOpenMenuEvent
import net.horizonsend.ion.server.features.starship.event.StarshipDetectedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipPilotEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipRotateEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStartCruisingEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStopCruisingEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.features.tutorial.message.ActionMessage
import net.horizonsend.ion.server.features.tutorial.message.PopupMessage
import net.horizonsend.ion.server.features.tutorial.message.TutorialMessage
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.action
import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.DARK_RED
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.inventory.ItemStack

enum class TutorialPhase(
	vararg val messages: TutorialMessage,
	val cancel: Boolean = true,
	val showCompleted: Boolean = false
) {
	GET_SHIP_CONTROLLER(
		PopupMessage(text("Welcome!", WHITE), ofChildren(text("Welcome to ", HE_MEDIUM_GRAY), HORIZONS_END)),
		PopupMessage(text("Tutorial", DARK_AQUA), text("You can leave by doing /tutorialexit", DARK_RED)),
		PopupMessage(text("Tutorial", DARK_AQUA), ofChildren(HORIZONS_END, text(" has unique features to learn like spaceships", DARK_GREEN))),
		PopupMessage(text("Tutorial", DARK_AQUA), text("This tutorial teaches you how to fly a spaceship!")),
		PopupMessage(text("Controller", BLUE), text("First, you need a ship controller")),
		PopupMessage(text("Controller", BLUE), text("Ship controllers are needed to fly ships")),
		PopupMessage(text("Controller", BLUE), text("You can always get one with /kit controller")),
		PopupMessage(text("Controller", BLUE), text("Enter the command: /kit controller", GOLD, BOLD)),
		cancel = false
	) {
		override fun setupHandlers() = on<PlayerCommandPreprocessEvent>({ it.player }) { event, player ->
			println(event.message)
			println(event.player)
			if (event.message.removePrefix("/").equals("kit controller", ignoreCase = true)) {
				event.isCancelled = true

				val item = ItemStack(StarshipControl.CONTROLLER_TYPE, 1)

				player.world.dropItem(player.eyeLocation.add(player.location.direction.multiply(0.25)), item)

				nextStep(player)
			}
		}
	},
	PLACE_SHIP_COMPUTER(
		PopupMessage(text("Computer", DARK_PURPLE), text("Now you need a ship computer")),
		PopupMessage(text("Computer", DARK_PURPLE), text("Ship computers are used to start the ship")),
		ActionMessage(text("Computer", DARK_PURPLE), text("You have been given one ship computer")) { player ->
			val item = ItemStack(Material.JUKEBOX, 1).setDisplayNameAndGet("&rStarship Computer".colorize())
			player.inventory.addItem(item).forEach { (_, leftover) ->
				player.world.dropItem(player.eyeLocation, leftover)
			}
		},
		PopupMessage(text("Computer", DARK_PURPLE), text("Place ship computer (black jukebox)", LIGHT_PURPLE, BOLD))
	) {
		override fun setupHandlers() = on<BlockPlaceEvent>({ it.player }) { event, player ->
			if (event.block.type == Material.JUKEBOX) {
				nextStep(player)
			}
		}
	},
	OPEN_COMPUTER_MENU(
		PopupMessage(text("Computer Menu", DARK_AQUA), text("Ship computers are used via their menu")),
		PopupMessage(text("Computer Menu", DARK_AQUA), text("Left click computer with controller (clock)", GOLD, BOLD))
	) {
		override fun setupHandlers() = on<StarshipComputerOpenMenuEvent>({ it.player }) { _, player ->
			nextStep(player)
			Tasks.syncDelay(15, player::closeInventory)
		}
	},
	DETECT_SHIP(
		PopupMessage(text("Detection", GOLD), text("Now you need to detect the ship")),
		PopupMessage(text("Detection", GOLD), text("Detecting determines which blocks are your ship")),
		PopupMessage(text("Detection", GOLD), text("Some block types are detected, but not stone etc")),
		PopupMessage(text("Detection", GOLD), text("Use the ship computer to detect")),
		PopupMessage(text("Detection", GOLD), "<yellow><bold>Open the menu again & click <dark_purple><bold>Re-Detect".miniMessage())
	) {
		override fun setupHandlers() = on<StarshipDetectedEvent>({ it.player }) { _, player ->
			nextStep(player)
		}
	},
	PILOT_SHIP(
		PopupMessage(text("Piloting", GREEN), text("Now you need to pilot the ship")),
		PopupMessage(text("Piloting", GREEN), text("Ships only move while they are piloted")),
		PopupMessage(text("Piloting", GREEN), text("Additionally, shields only work while piloted")),
		PopupMessage(text("Piloting", GREEN), text("Right click computer with controller (clock)", GOLD, BOLD))
	) {
		override fun setupHandlers() = on<StarshipPilotEvent>({ it.player }) { _, player ->
			nextStep(player)
		}
	},
	SHIFT_FLY_FORWARD(
		PopupMessage(text("Moving", LIGHT_PURPLE), text("You can move ships while piloted")),
		PopupMessage(text("Moving", LIGHT_PURPLE), text("There are various ways to move ships")),
		PopupMessage(text("Moving", LIGHT_PURPLE), text("The most basic way is 'shift' flying")),
		PopupMessage(text("Moving", LIGHT_PURPLE), text("To shift fly, first hold your controller")),
		PopupMessage(text("Moving", LIGHT_PURPLE), text("Then, hold the sneak key (default key shift)")),
		PopupMessage(text("Moving", LIGHT_PURPLE), text("This moves you the way you're facing")),
		PopupMessage(text("Moving", LIGHT_PURPLE), text("For practice, shift fly forwards")),
		PopupMessage(text("Moving", LIGHT_PURPLE), text("Hold the controller, face the window, & sneak", GOLD, BOLD))
	) {
		override fun setupHandlers() = on<StarshipTranslateEvent>({ it.starship.playerPilot }) { _, player ->
			nextStep(player)
		}
	},
	SHIFT_FLY_DOWN(
		PopupMessage(text("Moving Down", DARK_GREEN), text("You can shift fly any direction, even down")),
		PopupMessage(text("Moving Down", DARK_GREEN), text("Shift flying down lets you land on a planet")),
		PopupMessage(text("Moving Down", DARK_GREEN), text("Hold the controller, face down, & sneak", GOLD, BOLD)),
		cancel = false // let them keep shift flying forward
	) {
		override fun setupHandlers() = on<StarshipTranslateEvent>({ it.starship.playerPilot }) { event, player ->
			if (event.y < 0) {
				nextStep(player)
			} else {
				player action "&eYou're moving, but not straight down!"
			}
		}
	},
	TURN_RIGHT(
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Besides moving, you can turn your ship")),
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Ships can face the 4 directions (N/E/S/W)")),
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("To turn your ship, you can use the helm sign")),
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Right click the sign with [helm] on it")),
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Then, holding the controller, click again")),
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Right click to turn right, left click for left")),
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Hold the controller, right click the helm sign", GOLD, BOLD))
	) {
		override fun setupHandlers() = on<StarshipRotateEvent>({ it.starship.playerPilot }) { event, player ->
			if (event.clockwise) {
				nextStep(player)
			}
		}
	},
	TURN_LEFT(
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Now left click the helm sign", GOLD, BOLD)),
		cancel = false // let them rotate
	) {
		override fun setupHandlers() = on<StarshipRotateEvent>({ it.starship.playerPilot }) { event, player ->
			if (!event.clockwise) {
				nextStep(player)
			}
		}
	},
	CRUISE_START(
		PopupMessage(text("Cruising", BLUE), text("Cruise to move steadily over long distances")),
		PopupMessage(text("Cruising", BLUE), text("Cruising uses thrusters to determine speed")),
		PopupMessage(text("Cruising", BLUE), text("To cruise, right click the [cruise] sign")),
		PopupMessage(text("Cruising", BLUE), text("Right click again to cruise")),
		PopupMessage(text("Cruising", BLUE), text("Cruising works forwards and diagonally of it")),
		PopupMessage(text("Cruising", BLUE), text("If you can't face the right way, turn the ship")),
		PopupMessage(text("Cruising", BLUE), text("Hold the controller & right click cruise sign", GOLD, BOLD))
	) {
		override fun setupHandlers() = on<StarshipStartCruisingEvent>({ it.starship.playerPilot }) { event, player ->
			nextStep(player)
		}
	},
	CRUISE_STOP(
		PopupMessage(text("Stop Cruising", BLUE), text("Left click the cruise sign to stop", GOLD, BOLD))
	) {
		override fun setupHandlers() = on<StarshipStopCruisingEvent>({ it.starship.playerPilot }) { event, player ->
			nextStep(player)
		}
	},
	RELEASE_SHIP(
		PopupMessage(text("Releasing", GRAY), text("When done flying, release to stop piloting")),
		PopupMessage(text("Releasing", GRAY), text("Releasing also lets you leave the ship")),
		PopupMessage(text("Releasing", GRAY), text("Type /release or right click the computer", YELLOW, BOLD))
	) {
		override fun setupHandlers() = on<StarshipUnpilotEvent>({ (it.controller as? PlayerController)?.player }) { event, player ->
			event.isCancelled = true
			StarshipDestruction.vanish(event.starship)
			nextStep(player)
		}
	}

	;

	open fun onStart(player: Player) {}

	open fun onEnd(player: Player) {}

	abstract fun setupHandlers()

	/**
	 * Runs the code on the given event if the player retrieved from getPlayer
	 * is in the same phase as the phase which called this method in its initialization
	 */
	protected inline fun <reified T : Event> on(
		crossinline getPlayer: (T) -> Player?,
		crossinline handler: (T, Player) -> Unit
	) {
		val phase = this

		listen<T>(EventPriority.NORMAL) { event: T ->
			val player: Player = getPlayer(event) ?: return@listen

			if (TutorialManager.getPhase(player) == phase) {
				if (TutorialManager.isReading(player)) {
					if (event is Cancellable && this@TutorialPhase.cancel) {
						event.isCancelled = true
						player.userError("Finish reading the messages! :P")
					}

					return@listen
				}

				handler(event, player)
			}
		}
	}

	protected fun nextStep(player: Player) {
		if (showCompleted) player.success("Completed $this")
		player.resetTitle()

		val next: TutorialPhase? = byOrdinal[ordinal + 1]

		if (next == null) {
			TutorialManager.stop(player) // if there is no next step, then stop instead
			return
		}

		onEnd(player)
		TutorialManager.startPhase(player, next)
	}

	companion object {
		val FIRST: TutorialPhase = values().first()
		val LAST: TutorialPhase = values().last()

		private val byOrdinal: Map<Int, TutorialPhase> = values().associateBy(TutorialPhase::ordinal)
	}
}
