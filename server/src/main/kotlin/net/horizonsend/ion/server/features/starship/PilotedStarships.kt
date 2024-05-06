package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.alertSubtitle
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.common.extensions.userErrorTitle
import net.horizonsend.ion.common.utils.configuration.redis
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ai.spawning.SpawningException
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.UnpilotedController
import net.horizonsend.ion.server.features.starship.event.StarshipPilotEvent
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.subsystem.LandingGearSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.MiningLaserSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import net.horizonsend.ion.server.features.transport.Extractors
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.bukkitWorld
import net.horizonsend.ion.server.miscellaneous.utils.createData
import net.horizonsend.ion.server.miscellaneous.utils.isPilot
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.Locale
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object PilotedStarships : IonServerComponent() {
	internal val map = mutableMapOf<Controller, ActiveControlledStarship>()

	override fun onEnable() {
		listen<PlayerQuitEvent> { event ->
			val loc = Vec3i(event.player.location)
			val controller = ActiveStarships.findByPilot(event.player)?.controller ?: return@listen

			val starship = map[controller] ?: return@listen
			unpilot(starship) // release the player's starship if they are piloting one

			starship.pilotDisconnectLocation = loc
		}

		listen<PlayerJoinEvent> { event ->
			val starship = getUnpiloted(event.player) ?: return@listen
			val loc = starship.pilotDisconnectLocation ?: return@listen
			tryPilot(event.player, starship.data)

			event.player.teleport(loc.toLocation(starship.world))
			event.player.success("Since you logged out while piloting, you were teleported back to your starship")
		}
	}

	fun pilot(starship: ActiveControlledStarship, controller: Controller, callback: (ActiveControlledStarship) -> Unit = {}) {
		Tasks.checkMainThread()

		check(!starship.isExploding)

		map[controller] = starship
		starship.setController(controller)

		setupPassengers(starship)

		setupShieldDisplayIndicators(starship)

		StarshipShields.updateShieldBars(starship)

		removeExtractors(starship)

		callback(starship)
	}

	fun pilot(starship: ActiveControlledStarship, player: Player) {
		ActiveStarships.findByPilot(player)?.controller?.let { check(!map.containsKey(it)) { "${player.name} is already piloting a starship" } }
		check(starship.isWithinHitbox(player)) { "${player.name} is not in their ship!" }
		removeFromCurrentlyRidingShip(player)

		pilot(starship, ActivePlayerController(player, starship)) { ship ->
			saveLoadshipData(ship, player)
			StarshipPilotedEvent(ship, player).callEvent()
		}
	}

	fun changeController(starship: ActiveControlledStarship, newController: Controller) {
		map.remove(starship.controller)

		map[newController] = starship
	}

	private fun removeFromCurrentlyRidingShip(player: Player) {
		ActiveStarships.findByPassenger(player)?.removePassenger(player.uniqueId)
	}

	private fun setupPassengers(starship: ActiveControlledStarship) {
		starship.playerPilot?.let { starship.addPassenger(it.uniqueId) }

		for (otherPlayer in starship.world.players) {
			if (!starship.isWithinHitbox(otherPlayer)) {
				continue
			}
			if (ActiveStarships.findByPassenger(otherPlayer) != null) {
				continue
			}
			starship.addPassenger(otherPlayer.uniqueId)
		}
	}

	private fun setupShieldDisplayIndicators(starship: ActiveControlledStarship) {
		starship.shields
			.distinctBy(ShieldSubsystem::name)
			.associateByTo(starship.shieldBars, ShieldSubsystem::name) { shield: ShieldSubsystem ->
				// create the actualStyle boss bar
				val bar: BossBar = Bukkit.createBossBar(shield.name, BarColor.GREEN, BarStyle.SEGMENTED_10)
				if (shield.isReinforcementActive()) bar.color = BarColor.PURPLE
				// add all passengers
				starship.onlinePassengers.forEach(bar::addPlayer)
				starship.shieldBars[shield.name] = bar
				bar
			}
	}

	private fun saveLoadshipData(starship: ActiveControlledStarship, player: Player) {
		val schematic = StarshipSchematic.createSchematic(starship)

		val key = "starships.lastpiloted.${player.uniqueId}.${starship.world.name.lowercase(Locale.getDefault())}"

		Tasks.async {
			redis {
				set(key, Blueprint.createData(schematic))
			}
		}
	}

	private fun removeExtractors(starship: ActiveControlledStarship) {
		starship.iterateBlocks { x, y, z ->
			if (starship.world.getBlockAt(x, y, z).type == Material.CRAFTING_TABLE) {
				Extractors.remove(starship.world, Vec3i(x, y, z))
			}
		}
	}

	fun isPiloted(starship: ActiveControlledStarship): Boolean {
		if (starship.controller is UnpilotedController) return false
		if (starship.controller is NoOpController) return false
		return true
	}

	fun isPiloting(player: Player): Boolean = map.any { (controller, _) -> (controller is ActivePlayerController) && controller.player == player }

	fun canTakeControl(starship: ActiveControlledStarship, player: Player): Boolean {
		return (starship.controller as? PlayerController)?.player?.uniqueId == player.uniqueId
	}

	fun unpilot(starship: ActiveControlledStarship) {
		Tasks.checkMainThread()
		val controller = starship.controller

		starship.setDirectControlEnabled(false)

		val unpilotedController = when (controller) {
			is PlayerController -> UnpilotedController(controller)
			else -> NoOpController(starship, starship.controller.damager)
		}

		map.remove(starship.controller)

		starship.setController(unpilotedController, updateMap = false)
		starship.lastUnpilotTime = System.nanoTime()

		starship.clearPassengers()

		starship.shieldBars.values.forEach { it.removeAll() }
		starship.shieldBars.clear()

		starship.iterateBlocks { x, y, z ->
			if (starship.world.getBlockAt(x, y, z).type == Material.CRAFTING_TABLE) {
				Extractors.add(starship.world, Vec3i(x, y, z))
			}
		}

		StarshipUnpilotedEvent(starship, controller).callEvent()
	}

	fun getUnpiloted(player: Player): ActiveControlledStarship? = map.filter { (controller, _) ->
		controller is UnpilotedController && controller.player.uniqueId == player.uniqueId
	}.values.firstOrNull()

	operator fun get(player: Player): ActiveControlledStarship? = get(player.uniqueId)

	operator fun get(player: UUID): ActiveControlledStarship? = map.entries.firstOrNull { (controller, _) ->
		(controller as? PlayerController)?.player?.uniqueId == player
	}?.value

	operator fun get(controller: Controller) = map[controller]

	fun activateWithoutPilot(
		feedbackDestination: Audience,
		data: StarshipData,
		createController: (ActiveControlledStarship) -> Controller,
		callback: (ActiveControlledStarship) -> Unit = {}
	): Boolean {
		val world: World = data.bukkitWorld()

		val state: StarshipState = DeactivatedPlayerStarships.getSavedState(data) ?: throw SpawningException("Not detected.", world, Vec3i(data.blockKey))

		for ((key: Long, blockData: BlockData) in state.blockMap) {
			val x: Int = blockKeyX(key)
			val y: Int = blockKeyY(key)
			val z: Int = blockKeyZ(key)
			val foundData: BlockData = world.getBlockAt(x, y, z).blockData

			if (blockData.material != foundData.material) {
				val expected: String = blockData.material.name
				val found: String = foundData.material.name

				throw SpawningException(
					"Block at $x, $y, $z does not match! Expected $expected but found $found",
					world,
					Vec3i(data.blockKey)
				)
			}

			if (foundData.material == StarshipComputers.COMPUTER_TYPE) {
				if (ActiveStarships.getByComputerLocation(world, x, y, z) != null) {
					throw SpawningException(
						"Block at $x, $y, $z is the computer of a piloted ship!",
						world,
						Vec3i(data.blockKey)
					)
				}
			}
		}

		DeactivatedPlayerStarships.activateAsync(feedbackDestination, data, state, listOf()) { activePlayerStarship ->
			pilot(activePlayerStarship, createController(activePlayerStarship))

			activePlayerStarship.sendMessage(
				Component.text("Activated and piloted ").color(NamedTextColor.GREEN)
					.append(getDisplayName(data))
					.append(Component.text(" with ${activePlayerStarship.initialBlockCount} blocks."))
			)

			callback(activePlayerStarship)
		}

		return true
	}

	fun tryPilot(player: Player, data: StarshipData, callback: (ActiveControlledStarship) -> Unit = {}): Boolean {
		if (data !is PlayerStarshipData) {
			player.userErrorTitle("You cannot pilot a non-player starship!")

			return false
		}

		if (!data.isPilot(player)) {
			val captain = SLPlayer.getName(data.captain) ?: "null, <red>something's gone wrong, please contact staff"

			player.userErrorActionMessage("You're not a pilot of this, the captain is $captain")

			return false
		}

		if (!data.starshipType.actualType.canUse(player)) {
			player.userErrorActionMessage("You are not high enough level to pilot this!")
			return false
		}

		val pilotedStarship = PilotedStarships[player]
		if (pilotedStarship != null) {
			if (pilotedStarship.dataId == data._id) {
				tryRelease(pilotedStarship)
				return false
			}

			player.userErrorActionMessage("You're already piloting a starship!")
			return false
		}

		if (!StarshipPilotEvent(player, data).callEvent()) {
			return false
		}

		// handle starship being already activated
		val activeStarship = ActiveStarships[data._id]

		if (activeStarship != null) {
			if (!canTakeControl(activeStarship, player)) {
				player.userErrorActionMessage("That starship is already being piloted!")
				return false
			}

			if (!activeStarship.isWithinHitbox(player)) {
				player.userErrorAction("You need to be inside the ship to pilot it")
				return false
			}

			pilot(activeStarship, player)
			player.successActionMessage("Piloted already activated starship")
			return false
		}

		val world: World = data.bukkitWorld()

		val state: StarshipState? = DeactivatedPlayerStarships.getSavedState(data)

		if (state == null) {
			player.userErrorActionMessage("Starship has not been detected")
			return false
		}

		when (data.starshipType) {
			"BATTLECRUISER" ->
				for (nearbyPlayer in player.world.getNearbyPlayers(player.location, 69420.0)) {
					nearbyPlayer.playSound(Sound.sound(Key.key("horizonsend:starship.pilot.battlecruiser"), Sound.Source.AMBIENT, 7f, 1.0f))
				}
			"CRUISER" ->
				for (nearbyPlayer in player.world.getNearbyPlayers(player.location, 5000.0)) {
					nearbyPlayer.playSound(Sound.sound(Key.key("horizonsend:starship.pilot.cruiser"), Sound.Source.AMBIENT, 5f, 1.0f))
				}
			else -> for (nearbyPlayer in player.world.getNearbyPlayers(player.location, 500.0)) {
				nearbyPlayer.playSound(Sound.sound(Key.key("minecraft:block.beacon.activate"), Sound.Source.AMBIENT, 5f, 0.05f))
			}
		}


		val carriedShips = mutableListOf<StarshipData>()

		for ((key: Long, blockData: BlockData) in state.blockMap) {
			val x: Int = blockKeyX(key)
			val y: Int = blockKeyY(key)
			val z: Int = blockKeyZ(key)
			val foundData: BlockData = world.getBlockAt(x, y, z).blockData

			if (blockData.material != foundData.material) {
				val expected: String = blockData.material.name
				val found: String = foundData.material.name
				player.userError(
					"Block at $x, $y, $z does not match! Expected $expected but found $found"
				)
				return false
			}

			if (foundData.material == StarshipComputers.COMPUTER_TYPE) {
				if (ActiveStarships.getByComputerLocation(world, x, y, z) != null) {
					player.userError(
						"Block at $x, $y, $z is the computer of a piloted ship!"
					)
					return false
				}

				DeactivatedPlayerStarships[world, x, y, z]?.takeIf { it._id != data._id }?.also { carried ->
					if (carried is PlayerStarshipData) { //TODO access system for non-player ships
						if (!carried.isPilot(player)) {
							player.userError(
								"Block at $x $y $z is a ship computer which you are not a pilot of!"
							)
							return false
						}

						carriedShips.add(carried)
					} else {
						player.userError("Block at $x, $y, $z is a non-player ship computer!")

						return false
					}
				}
			}
		}

		DeactivatedPlayerStarships.activateAsync(player, data, state, carriedShips) { activePlayerStarship ->
			// if the player logs out while it is piloting, deactivate it
			if (!player.isOnline) {
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			if (!activePlayerStarship.isWithinHitbox(player)) {
				player.userErrorAction("You need to be inside the ship to pilot it.")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			if (activePlayerStarship.drillCount > 16) {
				player.userErrorAction("Ships cannot have more than 16 drills! Count: ${activePlayerStarship.drillCount}")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			if (activePlayerStarship.type == StarshipType.BATTLECRUISER && (!SpaceWorlds.contains(activePlayerStarship.world) && !Hyperspace.isHyperspaceWorld(activePlayerStarship.world))) {
				player.userError("Battlecruisers can only be piloted in space!")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			// Check required subsystems
			for (requiredSubsystem in activePlayerStarship.balancing.requiredMultiblocks) {
				if (!requiredSubsystem.checkRequirements(activePlayerStarship.subsystems)) {
					player.userError("Subsystem requirement not met! ${requiredSubsystem.failMessage}")
					DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
					return@activateAsync
				}
			}

			// Limit mining laser tiers and counts
			val miningLasers = activePlayerStarship.subsystems.filterIsInstance<MiningLaserSubsystem>()
			if (miningLasers.any { it.multiblock.tier != activePlayerStarship.type.miningLaserTier }) {
				player.userErrorAction("Your starship can only support tier ${activePlayerStarship.type.miningLaserTier} mining lasers!")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			val landingGear = activePlayerStarship.subsystems.filterIsInstance<LandingGearSubsystem>()
			for (landingGearSubsystem in landingGear) {
				landingGearSubsystem.setExtended(false)
			}

			pilot(activePlayerStarship, player)

			player.sendMessage(
				Component.text("Activated and piloted ").color(NamedTextColor.GREEN)
					.append(activePlayerStarship.getDisplayName())
					.append(Component.text(" with ${activePlayerStarship.initialBlockCount} blocks."))
			)

			if (carriedShips.any()) {
				player.information(
					"${carriedShips.size} carried ship${if (carriedShips.size != 1) "s" else ""}."
				)
			}

			callback(activePlayerStarship)
		}

		return true
	}

	fun tryRelease(starship: ActiveControlledStarship): Boolean {
		val controller = starship.controller

		if (!StarshipUnpilotEvent(starship, controller).callEvent()) return false
		if (Hyperspace.isMoving(starship)) {
			starship.alertSubtitle("Cannot release while in hyperspace!")
			return false
		}

		unpilot(starship)
		DeactivatedPlayerStarships.deactivateAsync(starship)

		when {
			starship.type == StarshipType.BATTLECRUISER ->
				for (nearbyPlayer in starship.world.getNearbyPlayers(starship.centerOfMass.toLocation(starship.world), 69420.0)) {
					nearbyPlayer.playSound(Sound.sound(Key.key("horizonsend:starship.release.battlecruiser"), Sound.Source.AMBIENT, 7f, 1.0f))
				}
			starship.type == StarshipType.CRUISER ->
				for (nearbyPlayer in starship.world.getNearbyPlayers(starship.centerOfMass.toLocation(starship.world), 5000.0)) {
					nearbyPlayer.playSound(Sound.sound(Key.key("horizonsend:starship.release.cruiser"), Sound.Source.AMBIENT, 5f, 1.0f))
				}
			else ->
				for (nearbyPlayer in starship.world.getNearbyPlayers(starship.centerOfMass.toLocation(starship.world), 500.0)) {
					nearbyPlayer.playSound(Sound.sound(Key.key("minecraft:block.beacon.deactivate"), Sound.Source.AMBIENT, 5f, 0.05f))
				}
		}


		controller.successActionMessage("Released ${starship.getDisplayNameMiniMessage()}")
		return true
	}

	fun getDisplayName(data: StarshipData): Component = data.name?.let { MiniMessage.miniMessage().deserialize(it) } ?: data.starshipType.actualType.displayNameComponent
}
