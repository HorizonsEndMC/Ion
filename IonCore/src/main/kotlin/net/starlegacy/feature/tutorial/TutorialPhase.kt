package net.starlegacy.feature.tutorial

import net.starlegacy.PLUGIN
import net.starlegacy.feature.starship.StarshipDestruction
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.event.StarshipComputerOpenMenuEvent
import net.starlegacy.feature.starship.event.StarshipDetectEvent
import net.starlegacy.feature.starship.event.StarshipPilotEvent
import net.starlegacy.feature.starship.event.StarshipRotateEvent
import net.starlegacy.feature.starship.event.StarshipStartCruisingEvent
import net.starlegacy.feature.starship.event.StarshipStopCruisingEvent
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.feature.starship.event.StarshipUnpilotEvent
import net.starlegacy.feature.tutorial.message.ActionMessage
import net.starlegacy.feature.tutorial.message.PopupMessage
import net.starlegacy.feature.tutorial.message.TutorialMessage
import net.starlegacy.util.Tasks
import net.starlegacy.util.action
import net.starlegacy.util.colorize
import net.starlegacy.util.msg
import net.starlegacy.util.setDisplayNameAndGet
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.inventory.ItemStack

enum class TutorialPhase(vararg val messages: TutorialMessage, val cancel: Boolean = true) {
	GET_SHIP_CONTROLLER(
		PopupMessage("&a&l&oWelcome!", "&7Welcome to &6Star &eLegacy"),
		PopupMessage("&3Tutorial", "&4&lYou can leave by doing /tutorialexit"),
		PopupMessage("&3Tutorial", "&2SL has unique features to learn like spaceships"),
		PopupMessage("&3Tutorial", "&eThis tutorial teaches you how to fly a spaceship!"),
		PopupMessage("&9Controller", "First, you need a ship controller"),
		PopupMessage("&9Controller", "Ship controllers are needed to fly ships"),
		PopupMessage("&9Controller", "You can always get one with /kit controller"),
		PopupMessage("&9Controller", "&6&lEnter Command&8: &b/kit controller"),
		cancel = false
	) {
		override fun setupHandlers() = on<PlayerCommandPreprocessEvent>({ it.player }) { event, player ->
			if (event.message.removePrefix("/").equals("kit controller", ignoreCase = true)) {
				event.isCancelled = true

				val item = ItemStack(StarshipControl.CONTROLLER_TYPE, 1)

				player.world.dropItem(player.eyeLocation.add(player.location.direction.multiply(0.25)), item)

				nextStep(player)
			}
		}
	},
	PLACE_SHIP_COMPUTER(
		PopupMessage("&5Computer", "Now you need a ship computer"),
		PopupMessage("&5Computer", "Ship computers are used to start the ship"),
		ActionMessage("&5Computer", "You have been given one ship computer") { player ->
			val item = ItemStack(Material.JUKEBOX, 1).setDisplayNameAndGet("&rStarship Computer".colorize())
			player.inventory.addItem(item).forEach { (_, leftover) ->
				player.world.dropItem(player.eyeLocation, leftover)
			}
		},
		PopupMessage("&5Computer", "&d&lPlace ship computer (black jukebox)")
	) {
		override fun setupHandlers() = on<BlockPlaceEvent>({ it.player }) { event, player ->
			if (event.block.type == Material.JUKEBOX) {
				nextStep(player)
			}
		}
	},
	OPEN_COMPUTER_MENU(
		PopupMessage("&3Computer Menu", "Ship computers are used via their menu"),
		PopupMessage("&3Computer Menu", "&lLeft click computer with controller (clock)")
	) {
		override fun setupHandlers() = on<StarshipComputerOpenMenuEvent>({ it.player }) { event, player ->
			nextStep(player)
			Tasks.syncDelay(15, player::closeInventory)
		}
	},
	DETECT_SHIP(
		PopupMessage("&6Detection", "Now you need to detect the ship"),
		PopupMessage("&6Detection", "Detecting determines which blocks are your ship"),
		PopupMessage("&6Detection", "Some block types are detected, but not stone etc"),
		PopupMessage("&6Detection", "Use the ship computer to detect"),
		PopupMessage("&6Detection", "&e&lOpen the menu again & click &5&lRe-Detect")
	) {
		override fun setupHandlers() = on<StarshipDetectEvent>({ it.player }) { event, player ->
			nextStep(player)
		}
	},
	PILOT_SHIP(
		PopupMessage("&aPiloting", "Now you need to pilot the ship"),
		PopupMessage("&aPiloting", "Ships only move while they are piloted"),
		PopupMessage("&aPiloting", "Additionally, shields only work while piloted"),
		PopupMessage("&aPiloting", "&6&lRight click computer with controller (clock)")
	) {
		override fun setupHandlers() = on<StarshipPilotEvent>({ it.player }) { event, player ->
			nextStep(player)
		}
	},
	SHIFT_FLY_FORWARD(
		PopupMessage("&dMoving", "You can move ships while piloted"),
		PopupMessage("&dMoving", "There are various ways to move ships"),
		PopupMessage("&dMoving", "The most basic way is 'shift' flying"),
		PopupMessage("&dMoving", "To shift fly, first hold your controller"),
		PopupMessage("&dMoving", "Then, hold the sneak key (default key shift)"),
		PopupMessage("&dMoving", "This moves you the way you're facing"),
		PopupMessage("&dMoving", "For practice, shift fly forwards"),
		PopupMessage("&dMoving", "&6&lHold the controller, face the window, & sneak")
	) {
		override fun setupHandlers() = on<StarshipTranslateEvent>({ it.player }) { event, player ->
			nextStep(player)
		}
	},
	SHIFT_FLY_DOWN(
		PopupMessage("&2Moving Down", "You can shift fly any direction, even down"),
		PopupMessage("&2Moving Down", "Shift flying down lets you land on a planet"),
		PopupMessage("&2Moving Down", "&6&lHold the controller, face down, & sneak"),
		cancel = false // let them keep shift flying forward
	) {
		override fun setupHandlers() = on<StarshipTranslateEvent>({ it.player }) { event, player ->
			if (event.y < 0) {
				nextStep(player)
			} else {
				player action "&eYou're moving, but not straight down!"
			}
		}
	},
	TURN_RIGHT(
		PopupMessage("&dRotating", "Besides moving, you can turn your ship"),
		PopupMessage("&dRotating", "Ships can face the 4 directions (N/E/S/W)"),
		PopupMessage("&dRotating", "To turn your ship, you can use the helm sign"),
		PopupMessage("&dRotating", "Right click the sign with [helm] on it"),
		PopupMessage("&dRotating", "Then, holding the controller, click again"),
		PopupMessage("&dRotating", "Right click to turn right, left click for left"),
		PopupMessage("&dRotating", "&6&lHold the controller, right click the helm sign")
	) {
		override fun setupHandlers() = on<StarshipRotateEvent>({ it.player }) { event, player ->
			if (event.clockwise) {
				nextStep(player)
			}
		}
	},
	TURN_LEFT(
		PopupMessage("&dRotating", "&6&lNow left click the helm sign"),
		cancel = false // let them rotate
	) {
		override fun setupHandlers() = on<StarshipRotateEvent>({ it.player }) { event, player ->
			if (!event.clockwise) {
				nextStep(player)
			}
		}
	},
	CRUISE_START(
		PopupMessage("&9Cruising", "Cruise to move steadily over long distances"),
		PopupMessage("&9Cruising", "Cruising uses thrusters to determine speed"),
		PopupMessage("&9Cruising", "To cruise, right click the [cruise] sign"),
		PopupMessage("&9Cruising", "Right click again to cruise"),
		PopupMessage("&9Cruising", "Cruising works forwards and diagonally of it"),
		PopupMessage("&9Cruising", "If you can't face the right way, turn the ship"),
		PopupMessage("&9Cruising", "&6&lHold the controller & right click cruise sign")
	) {
		override fun setupHandlers() = on<StarshipStartCruisingEvent>({ it.player }) { event, player ->
			nextStep(player)
		}
	},
	CRUISE_STOP(
		PopupMessage("&9Stop Cruising", "&6&lLeft click the cruise sign to stop")
	) {
		override fun setupHandlers() = on<StarshipStopCruisingEvent>({ it.player }) { event, player ->
			nextStep(player)
		}
	},
	RELEASE_SHIP(
		PopupMessage("&7Releasing", "When done flying, release to stop piloting"),
		PopupMessage("&7Releasing", "Releasing also lets you leave the ship"),
		PopupMessage("&7Releasing", "&e&lType /release or right click the computer")
	) {
		override fun setupHandlers() = on<StarshipUnpilotEvent>({ it.player }) { event, player ->
			event.isCancelled = true
			StarshipDestruction.vanish(event.starship)
			nextStep(player)
		}
	};

	open fun onStart(player: Player) {}

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

		Bukkit.getPluginManager().registerEvents(object : Listener {
			@EventHandler(priority = EventPriority.NORMAL)
			fun onEvent(event: T) {
				val player: Player = getPlayer(event) ?: return

				if (TutorialManager.getPhase(player) == phase) {
					if (TutorialManager.isReading(player)) {
						if (event is Cancellable && this@TutorialPhase.cancel) {
							event.isCancelled = true
							player msg "&cFinish reading the messages! :P"
						}
						return
					}
					handler(event, player)
				}
			}
		}, PLUGIN)
	}

	protected fun nextStep(player: Player) {
		player action "&a&oCompleted $this"
		player.resetTitle()

		val next: TutorialPhase? = byOrdinal[ordinal + 1]

		if (next == null) {
			TutorialManager.stop(player) // if there is no next step, then stop instead
			return
		}

		TutorialManager.startPhase(player, next)
	}

	companion object {
		val FIRST: TutorialPhase = values().first()
		val LAST: TutorialPhase = values().last()

		private val byOrdinal: Map<Int, TutorialPhase> = values().associateBy(TutorialPhase::ordinal)
	}
}
