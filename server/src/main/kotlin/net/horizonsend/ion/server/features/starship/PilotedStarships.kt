package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorActionMessage
import net.horizonsend.ion.server.features.starship.subsystem.MiningLaserSubsystem
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.StarshipPilotEvent
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.features.starship.subsystem.shield.ShieldSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.shield.StarshipShields
import net.horizonsend.ion.server.features.transport.Extractors
import net.horizonsend.ion.common.redis
import net.horizonsend.ion.server.miscellaneous.utils.*
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object PilotedStarships : IonServerComponent() {
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
				player.information(
					"You already have a ship unpiloted, on ${it.serverLevel.world.name} at " +
						"${it.centerOfMass.x} ${it.centerOfMass.y} ${it.centerOfMass.z}, " +
						"that ship will now be released."
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
				tryRelease(pilotedStarship, player)
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
			if (isPiloted(activeStarship)) {
				player.userErrorActionMessage("That starship is already being piloted!")
				return false
			}

			if (!activeStarship.isWithinHitbox(player)) {
				player.userError("You need to be inside the ship to pilot it")
				return false
			}

			pilot(activeStarship, player)
			player.successActionMessage("Piloted already activated starship")
			return false
		}

		val world: World = data.bukkitWorld()

		val state: PlayerStarshipState? = DeactivatedPlayerStarships.getSavedState(data)

		if (state == null) {
			player.userErrorActionMessage("Starship has not been detected")
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
					if (!carried.isPilot(player)) {
						player.userError(
							"Block at $x $y $z is a ship computer which you are not a pilot of!"
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
				player.userError("You need to be inside the ship to pilot it")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			if (activePlayerStarship.drillCount > 16) {
				player.userError("Ships can not have more that 16 drills! Count: ${activePlayerStarship.drillCount}")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			val miningLasers = activePlayerStarship.subsystems.filterIsInstance<MiningLaserSubsystem>()
			if (miningLasers.any { it.multiblock.tier != activePlayerStarship.type.miningLaserTier }) {
				player.userError("Your starship can only support tier ${activePlayerStarship.type.miningLaserTier} mining lasers!")
				DeactivatedPlayerStarships.deactivateAsync(activePlayerStarship)
				return@activateAsync
			}

			pilot(activePlayerStarship, player)

			player.sendMessage(
				Component.text("Activated and piloted ").color(NamedTextColor.GREEN)
					.append(getDisplayNameComponent(data))
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

	fun tryRelease(starship: ActivePlayerStarship, player: Player): Boolean {
		if (starship.serverLevel.world.name.contains("Hyperspace")) return false
		if (!StarshipUnpilotEvent(starship, player).callEvent()) {
			return false
		}
		unpilot(starship)

		//check safezones
		val isProtected = Regions.find(player.location)
				.filterIsInstance<RegionTerritory>()
				.find { it.isProtected } != null

		val pilotNation: Oid<Nation>? = PlayerCache[player].nationOid

	    if(!isProtected && pilotNation !=null){
			for (nearbyPlayer in player.world.getNearbyPlayers(player.location, 1000.0)) {

				val otherNation: Oid<Nation> = PlayerCache[nearbyPlayer].nationOid ?: continue

				//if relation to the other person is less than none, unpilot instead
				if (NationRelation.getRelationActual(pilotNation, otherNation).ordinal <= NationRelation.Level.UNFRIENDLY.ordinal){
					starship.controller?.information("Possible Enemy Nearby: Cannot release, un-piloting instead")
					return false
				}
			}
		}
		DeactivatedPlayerStarships.deactivateAsync(starship)
		for (nearbyPlayer in player.world.getNearbyPlayers(player.location, 500.0)) {
			nearbyPlayer.playSound(Sound.sound(Key.key("minecraft:block.beacon.deactivate"), Sound.Source.AMBIENT, 5f, 0.05f))
		}
		player.successActionMessage("Released ${getDisplayName(starship.data)}")
		return true
	}

	fun getDisplayName(data: PlayerStarshipData): String {
		return data.name ?: data.starshipType.actualType.formatted
	}

	fun getDisplayNameComponent(data: PlayerStarshipData): Component = data.name?.let {
		MiniMessage.miniMessage().deserialize(it)
	} ?: MiniMessage.miniMessage().deserialize(data.starshipType.actualType.formatted)

	fun getRawDisplayName(data: PlayerStarshipData): String {
		return (MiniMessage.miniMessage().deserialize(getDisplayName(data)) as TextComponent).content()
	}
}
