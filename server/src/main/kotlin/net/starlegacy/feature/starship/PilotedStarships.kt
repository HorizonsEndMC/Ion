package net.starlegacy.feature.starship

import net.horizonsend.ion.server.legacy.feedback.FeedbackType.INFORMATION
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.SUCCESS
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.USER_ERROR
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipPilotEvent
import net.starlegacy.feature.starship.event.StarshipPilotedEvent
import net.starlegacy.feature.starship.event.StarshipUnpilotEvent
import net.starlegacy.feature.starship.event.StarshipUnpilotedEvent
import net.starlegacy.feature.starship.subsystem.shield.ShieldSubsystem
import net.starlegacy.feature.starship.subsystem.shield.StarshipShields
import net.starlegacy.feature.transport.Extractors
import net.starlegacy.listen
import net.starlegacy.redis
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object PilotedStarships : SLComponent() {
	internal val map = mutableMapOf<Player, ActivePlayerStarship>()

	override fun onEnable() {
		listen<PlayerQuitEvent> { event ->
			map[event.player]?.let { unpilot(it, true) } // release the player's starship if they are piloting one
		}
	}

	fun pilot(starship: ActivePlayerStarship, player: Player) {
		Tasks.checkMainThread()
		check(!starship.isExploding)
		check(!map.containsKey(player)) { "${player.name} is already piloting a starship" }
		check(starship.isWithinHitbox(player)) { "${player.name} is not in their ship!" }
		removeFromCurrentlyRidingShip(player)
		map[player] = starship
		starship.pilot = player
		setupPassengers(starship)
		setupShieldDisplayIndicators(starship)
		StarshipShields.updateShieldBars(starship)
		saveLoadshipData(starship, player)
		removeExtractors(starship)
		StarshipPilotedEvent(starship, player).callEvent()
		starship.oldpilot = null
	}

	private fun removeFromCurrentlyRidingShip(player: Player) {
		ActiveStarships.findByPassenger(player)?.removePassenger(player.uniqueId)
	}

	private fun setupPassengers(starship: ActivePlayerStarship) {
		starship.addPassenger(starship.requirePilot().uniqueId)
		for (otherPlayer in starship.serverLevel.world.players) {
			if (!starship.isWithinHitbox(otherPlayer)) {
				continue
			}
			if (ActiveStarships.findByPassenger(otherPlayer) != null) {
				continue
			}
			starship.addPassenger(otherPlayer.uniqueId)
		}
	}

	private fun setupShieldDisplayIndicators(starship: ActivePlayerStarship) {
		starship.shields.map(ShieldSubsystem::name).distinct().associateWithTo(starship.shieldBars) { name: String ->
			// create the actual boss bar
			val bar: BossBar = Bukkit.createBossBar(name, BarColor.GREEN, BarStyle.SEGMENTED_10)
			// add all passengers
			starship.onlinePassengers.forEach(bar::addPlayer)
			starship.shieldBars[name] = bar
			return@associateWithTo bar
		}
	}

	private fun saveLoadshipData(starship: ActivePlayerStarship, player: Player) {
		val schematic = StarshipSchematic.createSchematic(starship)

		val key =
			"starships.lastpiloted.${player.uniqueId}.${starship.serverLevel.world.name.lowercase(Locale.getDefault())}"

		Tasks.async {
			redis {
				set(key, Blueprint.createData(schematic))
			}
		}
	}

	private fun removeExtractors(starship: ActivePlayerStarship) {
		starship.iterateBlocks { x, y, z ->
			if (starship.serverLevel.world.getBlockAt(x, y, z).type == Material.CRAFTING_TABLE) {
				Extractors.remove(starship.serverLevel.world, Vec3i(x, y, z))
			}
		}
	}

	fun isPiloted(starship: ActivePlayerStarship): Boolean {
		return starship.pilot != null
	}

	fun unpilot(starship: ActivePlayerStarship, normal: Boolean = false) {
		Tasks.checkMainThread()
		val player = starship.pilot ?: error("Starship $starship is not piloted")
		if (normal) {
			ActiveStarships.allPlayerShips().filter { it.oldpilot == player }.forEach {
				player.sendFeedbackActionMessage(
					INFORMATION,
					"You already have a ship unpiloted, on {0} at {1} {2} {3}, that ship will now be released.",
					it.serverLevel.world.name,
					it.centerOfMass.x,
					it.centerOfMass.y,
					it.centerOfMass.z
				)
				DeactivatedPlayerStarships.deactivateAsync(it)
			}
		}
		map.remove(player)
		starship.oldpilot = player
		starship.pilot = null
		starship.lastUnpilotTime = System.nanoTime()
		starship.clearPassengers()
		starship.shieldBars.values.forEach { it.removeAll() }
		starship.shieldBars.clear()

		starship.iterateBlocks { x, y, z ->
			if (starship.serverLevel.world.getBlockAt(x, y, z).type == Material.CRAFTING_TABLE) {
				Extractors.add(starship.serverLevel.world, Vec3i(x, y, z))
			}
		}

		StarshipUnpilotedEvent(starship, player).callEvent()
	}

	operator fun get(player: Player): ActivePlayerStarship? = map[player]
	fun tryPilot(player: Player, data: PlayerStarshipData, callback: (ActivePlayerStarship) -> Unit = {}): Boolean {
		if (!data.isPilot(player)) {
			val captain = SLPlayer.getName(data.captain) ?: "null, <red>something's gone wrong, please contact staff"

			player.sendFeedbackActionMessage(USER_ERROR, "You're not a pilot of this, the captain is {0}", captain)

			return false
		}
		if (!data.starshipType.canUse(player)) {
			player.sendFeedbackActionMessage(USER_ERROR, "You are not high enough level to pilot this!")
			return false
		}

		val pilotedStarship = PilotedStarships[player]
		if (pilotedStarship != null) {
			if (pilotedStarship.dataId == data._id) {
				tryRelease(pilotedStarship, player)
				return false
			}

			player.sendFeedbackActionMessage(USER_ERROR, "You're already piloting a starship!")
			return false
		}

		if (!StarshipPilotEvent(player, data).callEvent()) {
			return false
		}

		// handle starship being already activated
		val activeStarship = ActiveStarships[data._id]

		if (activeStarship != null) {
			if (isPiloted(activeStarship)) {
				player.sendFeedbackActionMessage(USER_ERROR, "That starship is already being piloted!")
				return false
			}

			if (!activeStarship.isWithinHitbox(player)) {
				player.sendFeedbackMessage(USER_ERROR, "You need to be inside the ship to pilot it")
				return false
			}

			pilot(activeStarship, player)
			player.sendFeedbackActionMessage(SUCCESS, "Piloted already activated starship")
			return false
		}

		val world: World = data.bukkitWorld()

		val state: PlayerStarshipState? = DeactivatedPlayerStarships.getSavedState(data)

		if (state == null) {
			player.sendFeedbackActionMessage(USER_ERROR, "Starship has not been detected")
			return false
		}

		for (nearbyPlayer in player.world.getNearbyPlayers(player.location, 500.0)) {
			nearbyPlayer.playSound(Sound.sound(Key.key("minecraft:block.beacon.activate"), Sound.Source.AMBIENT, 5f, 0.05f))
		}

		val carriedShips = mutableListOf<PlayerStarshipData>()

		for ((key: Long, blockData: BlockData) in state.blockMap) {
			val x: Int = blockKeyX(key)
			val y: Int = blockKeyY(key)
			val z: Int = blockKeyZ(key)
			val foundData: BlockData = world.getBlockAt(x, y, z).blockData

			if (blockData.material != foundData.material) {
				val expected: String = blockData.material.name
				val found: String = foundData.material.name
				player.sendFeedbackActionMessage(
					USER_ERROR,
					"Block at {0}, {1}, {2} does not match! Expected {3} but found {4}",
					x,
					y,
					z,
					expected,
					found
				)
				return false
			}

			if (foundData.material == StarshipComputers.COMPUTER_TYPE) {
				if (ActiveStarships.getByComputerLocation(world, x, y, z) != null) {
					player.sendFeedbackMessage(
						USER_ERROR,
						"Block at {0}, {1}, {2} is the computer of a piloted ship!",
						x,
						y,
						z
					)
					return false
				}

				DeactivatedPlayerStarships[world, x, y, z]?.takeIf { it._id != data._id }?.also { carried ->
					if (!carried.isPilot(player)) {
						player.sendFeedbackActionMessage(
							USER_ERROR,
							"Block at {0} {1} {2} is a ship computer which you are not a pilot of!",
							x,
							y,
							z
						)
						return false
					}

					carriedShips.add(carried)
				}
			}
		}

		DeactivatedPlayerStarships.activateAsync(data, state, carriedShips) { activePlayerStarship ->
			// if the player logs out while it is piloting, deactivate it
			if (!player.isOnline) {
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			if (!activePlayerStarship.isWithinHitbox(player)) {
				player.sendFeedbackMessage(USER_ERROR, "You need to be inside the ship to pilot it")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			pilot(activePlayerStarship, player)
			player.sendFeedbackActionMessage(
				SUCCESS,
				"Activated and piloted {0} with {1} blocks.",
				getDisplayName(data),
				activePlayerStarship.initialBlockCount
			)

			if (carriedShips.any()) {
				player.sendFeedbackMessage(
					INFORMATION,
					"{0} carried ship${if (carriedShips.size != 1) "s" else ""}.",
					carriedShips.size
				)
			}

			callback(activePlayerStarship)
		}

		return true
	}

	private fun tryRelease(starship: ActivePlayerStarship, player: Player): Boolean {
		if (!StarshipUnpilotEvent(starship, player).callEvent()) {
			return false
		}
		if (starship.serverLevel.world.name.contains("Hyperspace")) return false

		unpilot(starship)
		DeactivatedPlayerStarships.deactivateAsync(starship)
		for (nearbyPlayer in player.world.getNearbyPlayers(player.location, 500.0)) {
			nearbyPlayer.playSound(Sound.sound(Key.key("minecraft:block.beacon.deactivate"), Sound.Source.AMBIENT, 5f, 0.05f))
		}
		player.sendFeedbackActionMessage(SUCCESS, "Released {0}", getDisplayName(starship.data))
		return true
	}

	fun getDisplayName(data: PlayerStarshipData): String {
		return data.name ?: data.starshipType.formatted
	}

	fun getDisplayNameComponent(data: PlayerStarshipData): Component? {
		return data.name?.let { MiniMessage.miniMessage().deserialize(it) }
	}

	fun getRawDisplayName(data: PlayerStarshipData): String {
		return (MiniMessage.miniMessage().deserialize(getDisplayName(data)) as TextComponent).content()
	}
}
