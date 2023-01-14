package net.starlegacy.feature.starship.active

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.minecraft.core.BlockPos
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.database.schema.starships.SubCraftData
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.control.StarshipCruising
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.boss.BossBar
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.TimeUnit

class ActivePlayerStarship(
	val data: PlayerStarshipData,
	blocks: LongOpenHashSet,
	mass: Double,
	centerOfMass: BlockPos,
	hitbox: ActiveStarshipHitbox,
	subShips: MutableMap<SubCraftData, LongOpenHashSet>,
	// map of carried ship to its blocks
	carriedShips: Map<PlayerStarshipData, LongOpenHashSet>
) : ActiveStarship(
	(data.bukkitWorld() as CraftWorld).handle,
	blocks,
	subShips,
	mass,
	centerOfMass,
	hitbox
) {
	val carriedShips: MutableMap<PlayerStarshipData, LongOpenHashSet> = carriedShips.toMutableMap()
	override val type: StarshipType = data.starshipType
	override val interdictionRange: Int = data.starshipType.interdictionRange

	var lastUnpilotTime: Long = 0

	var pilot: Player? = null

	var oldpilot: Player? = null

	val minutesUnpiloted = if (pilot != null) 0 else TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - lastUnpilotTime)

	var speedLimit = -1

	val dataId: Oid<PlayerStarshipData> = data._id

	val shieldBars = mutableMapOf<String, BossBar>()

	var cruiseData = StarshipCruising.CruiseData(this)

	override val weaponColor: Color
		get() = pilot?.let { PlayerCache[it].nation }?.let { Color.fromRGB(NationCache[it].color) } ?: Color.RED

	fun requirePilot(): Player = requireNotNull(pilot) { "Starship must be piloted!" }

	override fun removePassenger(playerID: UUID) {
		super.removePassenger(playerID)
		val player = Bukkit.getPlayer(playerID) ?: return
		for (shieldBar in shieldBars.values) {
			shieldBar.removePlayer(player)
		}
	}

	override fun clearPassengers() {
		for (passenger in onlinePassengers) {
			for (shieldBar in shieldBars.values) {
				shieldBar.removePlayer(passenger)
			}
		}
		super.clearPassengers()
	}
}
