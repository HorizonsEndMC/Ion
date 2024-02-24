package net.horizonsend.ion.server.features.tutorial.tutorials

import io.papermc.paper.util.Tick
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.HORIZONS_END
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.StarshipDestruction
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.event.StarshipComputerOpenMenuEvent
import net.horizonsend.ion.server.features.starship.event.StarshipDetectedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipPilotEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipRotateEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStartCruisingEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipStopCruisingEvent
import net.horizonsend.ion.server.features.starship.event.movement.StarshipTranslateEvent
import net.horizonsend.ion.server.features.tutorial.Tutorials
import net.horizonsend.ion.server.features.tutorial.message.ActionMessage
import net.horizonsend.ion.server.features.tutorial.message.PopupMessage
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.execConsoleCmd
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.paste
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameSimple
import net.kyori.adventure.text.Component.keybind
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
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.title.Title.title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import java.io.File
import java.lang.ref.WeakReference
import java.time.Duration
import kotlin.math.abs

@Suppress("unused")
data object FlightTutorial : Tutorial() {
	private val GET_SHIP_CONTROLLER = registerSimplePhase(
		PopupMessage(subtitle = text("You can leave by doing /tutorialexit", DARK_RED)),
		PopupMessage(subtitle = ofChildren(HORIZONS_END, text(" has unique features to learn like spaceships", DARK_GREEN))),
		PopupMessage(subtitle = text("This tutorial teaches you how to fly a spaceship!")),
		PopupMessage(text("Controller", BLUE), text("First, you need a ship controller")),
		PopupMessage(subtitle = text("Ship controllers are needed to fly ships")),
		PopupMessage(subtitle = text("You can always get one with /kit controller")),
		PopupMessage(subtitle = text("Enter the command: /kit controller", GOLD, BOLD)),
		cancelEvent = false
	) {
		on<PlayerCommandPreprocessEvent>({ it.player }) on@{ event, player ->
			if (!event.message.removePrefix("/").equals("kit controller", ignoreCase = true)) return@on

			event.isCancelled = true

			val item = ItemStack(StarshipControl.CONTROLLER_TYPE, 1)
			player.world.dropItem(player.eyeLocation.add(player.location.direction.multiply(0.25)), item)

			moveToNextStep(player)
		}
	}

	private val PLACE_SHIP_COMPUTER = registerSimplePhase(
		PopupMessage(text("Computer", DARK_PURPLE), text("Now you need a ship computer")),
		PopupMessage(subtitle = text("Ship computers are used to start the ship")),
		ActionMessage(subtitle = text("You have been given one ship computer")) { player ->
			val item = ItemStack(Material.JUKEBOX, 1).setDisplayNameSimple("Starship Computer")

			player.inventory.addItem(item).forEach { (_, leftover) ->
				player.world.dropItem(player.eyeLocation, leftover)
			}
		},
		PopupMessage(text("Computer", DARK_PURPLE), text("Place ship computer (black jukebox)", LIGHT_PURPLE, BOLD))
	) {
		on<BlockPlaceEvent>({ it.player }) { event, player -> if (event.block.type == Material.JUKEBOX) moveToNextStep(player) }
	}

	private val OPEN_COMPUTER_MENU = registerSimplePhase(
		PopupMessage(text("Computer Menu", DARK_AQUA), text("Ship computers are used via their menu")),
		PopupMessage(subtitle = text("Left click computer with controller (clock)", GOLD, BOLD))
	) {
		on<StarshipComputerOpenMenuEvent>({ it.player }) { _, player ->
			moveToNextStep(player)
			Tasks.syncDelay(15, player::closeInventory)
		}
	}

	private val DETECT_SHIP = registerSimplePhase(
		PopupMessage(text("Detection", GOLD), text("Now you need to detect the ship")),
		PopupMessage(subtitle = text("Detecting determines which blocks are your ship")),
		PopupMessage(subtitle = text("Some block types are detected, but not stone etc")),
		PopupMessage(subtitle = text("Use the ship computer to detect")),
		PopupMessage(subtitle = "<yellow><bold>Open the menu again & click <dark_purple><bold>Re-Detect".miniMessage())
	) {
		on<StarshipDetectedEvent>({ it.player }) { _, player ->
			moveToNextStep(player)
		}
	}

	private val PILOT_SHIP = registerSimplePhase(
		PopupMessage(text("Piloting", GREEN), text("Now you need to pilot the ship")),
		PopupMessage(subtitle = text("Ships only move while they are piloted")),
		PopupMessage(subtitle = text("Additionally, shields only work while piloted")),
		PopupMessage(subtitle = text("Right click computer with controller (clock)", GOLD, BOLD))
	) {
		on<StarshipPilotEvent>({ it.player }) { _, player -> moveToNextStep(player) }
	}

	private val SHIFT_FLY_FORWARD = registerSimplePhase(
		PopupMessage(text("Moving", LIGHT_PURPLE), text("You can move ships while piloted")),
		PopupMessage(subtitle = text("There are various ways to move ships")),
		PopupMessage(subtitle = text("The most basic way is 'shift' flying")),
		PopupMessage(subtitle = text("To shift fly, first hold your controller")),
		PopupMessage(subtitle = text("Then, hold the sneak key (default key shift)")),
		PopupMessage(subtitle = text("This moves you the way you're facing")),
		PopupMessage(subtitle = text("For practice, shift fly forwards")),
		PopupMessage(subtitle = text("Hold the controller, face the window, & sneak", GOLD, BOLD))
	) {
		on<StarshipTranslateEvent>({ it.starship.playerPilot }) { _, player -> moveToNextStep(player) }
	}

	private val SHIFT_FLY_DOWN = registerSimplePhase(
		PopupMessage(text("Moving Down", DARK_GREEN), text("You can shift fly any direction, even down")),
		PopupMessage(subtitle = text("Shift flying down lets you land on a planet")),
		PopupMessage(subtitle = text("Hold the controller, face down, & sneak", GOLD, BOLD)),
		cancelEvent = false // let them keep shift flying forward
	) {
		on<StarshipTranslateEvent>({ it.starship.playerPilot }) { event, player ->
			if (event.y < 0) {
				moveToNextStep(player)
			} else {
				player.userError("You're moving, but not straight down!")
			}
		}
	}

	private val TURN_RIGHT = registerSimplePhase(
		PopupMessage(text("Rotating", LIGHT_PURPLE), text("Besides moving, you can turn your ship")),
		PopupMessage(subtitle = text("To turn your ship, you can use the helm sign or via your ship controller.")),
		PopupMessage(subtitle = ofChildren(text("Hold your controller and press the "), keybind("key.drop"), text(" key."))),
		PopupMessage(subtitle = ofChildren(text("Then, still holding the controller, press "), keybind("key.swapOffhand"))),
		PopupMessage(subtitle = ofChildren(keybind("key.drop"), text(" to turn left, "), keybind("key.swapOffhand"), text(" to turn right."))),
		PopupMessage(subtitle = ofChildren(text("Hold the controller, and press ", GOLD, BOLD), keybind("key.drop", GOLD, BOLD), text(" to turn left.", GOLD, BOLD)))
	) {
		on<StarshipRotateEvent>({ it.starship.playerPilot }) { event, player ->
			if (!event.clockwise) moveToNextStep(player)
		}
	}

	private val TURN_LEFT = registerSimplePhase(
		PopupMessage(subtitle = ofChildren(text("Now press ", GOLD, BOLD), keybind("key.swapOffhand", GOLD, BOLD), text(" to turn right.", GOLD, BOLD))),
		cancelEvent = false // let them rotate
	) {
		on<StarshipRotateEvent>({ it.starship.playerPilot }) { event, player ->
			if (event.clockwise) moveToNextStep(player)
		}
	}

	private val CRUISE_START = registerSimplePhase(
		PopupMessage(text("Cruising", BLUE), text("Cruise to move steadily over long distances")),
		PopupMessage(subtitle = text("Cruising uses thrusters to determine speed")),
		PopupMessage(subtitle = text("To cruise, right click the [cruise] sign")),
		PopupMessage(subtitle = text("Right click again to cruise")),
		PopupMessage(subtitle = text("Cruising works forwards and diagonally of it")),
		PopupMessage(subtitle = text("If you can't face the right way, turn the ship")),
		PopupMessage(subtitle = text("Hold the controller & right click cruise sign", GOLD, BOLD))
	) {
		on<StarshipStartCruisingEvent>({ it.starship.playerPilot }) { event, player -> moveToNextStep(player) }
	}

	private val CRUISE_STOP = registerSimplePhase(
		PopupMessage(text("Stop Cruising", BLUE), text("Left click the cruise sign to stop", GOLD, BOLD))
	) {
		on<StarshipStopCruisingEvent>({ it.starship.playerPilot }) { event, player -> moveToNextStep(player) }
	}

	private val ReleaseShip = registerSimplePhase(
		PopupMessage(text("Releasing", GRAY), text("When done flying, release to stop piloting")),
		PopupMessage(subtitle = text("Releasing also lets you leave the ship")),
		PopupMessage(subtitle = text("Type /release or right click the computer", YELLOW, BOLD))
	) {
		on<StarshipUnpilotEvent>({ (it.controller as? PlayerController)?.player }) { event, player ->
			event.isCancelled = true
			StarshipDestruction.vanish(event.starship)
			moveToNextStep(player)
		}
	}

	override val firstPhase: TutorialPhase = GET_SHIP_CONTROLLER
	override val lastPhase: TutorialPhase = ReleaseShip

	private const val WORLD_NAME: String = "FlightTutorial"

	override fun setup() {
		listen<StarshipRotateEvent> { event ->
			val player = (event.starship.controller as? PlayerController)?.player ?: return@listen

			if (isTutorialWorld(player.world) && getOrdinal(getPhase(player) ?: lastPhase) < getOrdinal(TURN_RIGHT)) {
				event.isCancelled = true
			}
		}

		listen<StarshipStartCruisingEvent> { event ->
			val player = (event.starship.controller as? PlayerController)?.player ?: return@listen

			if (isTutorialWorld(player.world) && getOrdinal(getPhase(player) ?: lastPhase) < getOrdinal(CRUISE_START)) {
				event.isCancelled = true
			}
		}

		listen<StarshipTranslateEvent> { event ->
			val player = (event.starship.controller as? PlayerController)?.player ?: return@listen

			if (isTutorialWorld(player.world) && getOrdinal(getPhase(player) ?: lastPhase) < getOrdinal(SHIFT_FLY_FORWARD)
			) {
				event.isCancelled = true
			}
		}

		// if someone places a ship computer in an existing one, overwrite it
		listen<BlockPlaceEvent>(priority = EventPriority.LOWEST) { event ->
			if (isTutorialWorld(event.block.world) && event.block.type == Material.JUKEBOX) {
				val loc = event.block.location

				DeactivatedPlayerStarships[loc.world, loc.blockX, loc.blockY, loc.blockZ]?.let { state ->
					log.warn("Deleted computer ${loc.world.name}@${Vec3i(loc)} because someone placed over it")

					DeactivatedPlayerStarships.destroyAsync(state)
				}
			}
		}

		listen<StarshipUnpilotEvent>(priority = EventPriority.LOW) { event ->
			val controller = event.controller

			if (controller !is PlayerController) return@listen

			val player = controller.player

			if (!isTutorialWorld(player.world) || playerPhases[player.uniqueId] == lastPhase) {
				return@listen
			}

			player.userError("You unpiloted your starship, stopping tutorial")

			endTutorial(player)

			StarshipDestruction.vanish(event.starship)
			event.isCancelled = true

			player.showTitle(
				title(
					text("Tutorial Cancelled", RED, BOLD),
					text("Unpiloted (right clicked computer) before the tutorial end", GRAY),
					net.kyori.adventure.title.Title.Times.times(
						Duration.of(10, Tick.tick()),
						Duration.of(Int.MAX_VALUE - 20L, Tick.tick()),
						Duration.of(0, Tick.tick())
					)
				)
			)
		}

		listen<PlayerJoinEvent> { event ->
			val player = event.player

			player.resetTitle()
			playerPhases.remove(player.uniqueId) // who knows...
		}

		listen<BlockBreakEvent> { event ->
			if (isTutorialWorld(event.block.world)) event.isCancelled = true
		}

		// Disable all damage in the world
		listen<EntityDamageEvent> { event ->
			if (isTutorialWorld(event.entity.world)) {
				event.isCancelled = true
			}
		}

		// erase chunks in the world
		listen<ChunkUnloadEvent> { event ->
			if (!isTutorialWorld(event.world)) {
				return@listen
			}

			val chunk = event.chunk
			val chunkReference = WeakReference(chunk)

			val worldShipData = DeactivatedPlayerStarships.getInChunk(chunk)
			if (worldShipData.any()) {
				log.warn("Deleting " + worldShipData.size + " starship computers in tutorial world")
				DeactivatedPlayerStarships.destroyManyAsync(worldShipData) {
					Tutorials.clearChunk(chunkReference)
				}
				return@listen
			}

			Tutorials.clearChunk(chunkReference)
		}
	}

	private fun teleportToStart(player: Player) {
		execConsoleCmd("warp tutorialstart ${player.name}")
	}

	private fun teleportToEnd(player: Player) {
		execConsoleCmd("warp tutorialend ${player.name}")

		player.sendRichMessage(
			"Thanks for completing the tutorial! This server can be a bit challenging to learn, but the community has come together to produce a wealth of resources for new players. " +
			"You can check out articles on our wiki at https://wiki.horizonsend.net/, or YouTube tutorials on our channel over at https://www.youtube.com/@horizonsend782/videos."
		)
	}

	override fun startTutorial(player: Player) {
		require(PilotedStarships[player] == null)

		playerPhases[player.uniqueId] = firstPhase

		val loc: Location = getSafeLocation()
		loadShip(loc)

		player.teleport(loc)
		player.teleport(loc) // teleport a second time, because, well... minecraft

		startPhase(player, firstPhase)
	}

	override fun endTutorial(player: Player) {
		readTimes.remove(player.uniqueId)

		val phase: TutorialPhase? = playerPhases.remove(player.uniqueId)

		if (!isTutorialWorld(player.world)) return

		player.resetTitle()

		Tasks.syncDelay(10) {
			when (phase) {
				lastPhase -> teleportToEnd(player)
				else -> teleportToStart(player)
			}
		}
	}

	private val distance = 1000

	private fun getSafeLocation(): Location {
		var x = distance
		while (getWorld().players.any { abs(it.location.blockX - x) <= distance }) {
			x += distance
		}
		return Location(getWorld(), x.toDouble() + 0.5, (getWorld().maxHeight / 2).toDouble(), 0.5)
	}

	private fun getWorld() = Bukkit.getWorld(WORLD_NAME)!!
	private fun isTutorialWorld(world: World) = world.uid == getWorld().uid

	private fun loadShip(loc: Location) {
		val file = File(IonServer.dataFolder, "tutorial_ship.schem")

		if (!file.exists()) {
			error("${file.absolutePath} doesn't exist!")
		}

		val clipboard = readSchematic(file) ?: error("Failed to read ${file.path}")
		clipboard.paste(loc.world, loc.blockX, loc.blockY, loc.blockZ)
	}
}
