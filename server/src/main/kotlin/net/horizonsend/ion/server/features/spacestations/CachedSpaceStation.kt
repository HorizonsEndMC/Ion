package net.horizonsend.ion.server.features.spacestations

import com.mongodb.client.result.UpdateResult
import net.horizonsend.ion.server.features.spacestations.SpaceStations.SpaceStationPermission
import net.horizonsend.ion.server.database.DbObject
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.NationRole
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.SettlementRole
import net.horizonsend.ion.server.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SpaceStationInterface
import net.horizonsend.ion.server.database.schema.nations.spacestation.SpaceStationCompanion
import net.horizonsend.ion.server.database.uuid
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.util.squared
import org.bukkit.Bukkit
import org.bukkit.Color
import org.litote.kmongo.Id
import kotlin.math.roundToInt

abstract class CachedSpaceStation<T: SpaceStationInterface<O>, O: DbObject, C: SpaceStationCompanion<O, T>> {
	abstract val databaseId: Oid<T>

	abstract val owner: Id<O>

	abstract val name: String

	abstract var world: String
	abstract var x: Int
	abstract var z: Int
	abstract var radius: Int

	abstract var trustedPlayers: Set<SLPlayerId>
	abstract var trustedSettlements: Set<Oid<Settlement>>
	abstract var trustedNations: Set<Oid<Nation>>

	abstract var trustLevel: SpaceStations.TrustLevel

	abstract val companion: C

	abstract val ownerName: String
	abstract val ownershipType: String

	abstract val color: Int

	abstract fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission): Boolean

	/** Checks if their nation, settlement, or player owns the station **/
	abstract fun hasOwnershipContext(player: SLPlayerId): Boolean

	fun setLocation(x: Int, z: Int, world: String) {
		this.x = x
		this.z = z
		this.world = world

		companion.setLocation(databaseId, x, z, world)
	}

	fun rename(newName: String) = companion.rename(databaseId, newName)

	fun changeRadius(newRadius: Int) = companion.setRadius(databaseId, newRadius)

	fun isTrusted(id: SLPlayerId): Boolean = trustedPlayers.contains(id)
	fun isSettlementTrusted(id: Oid<Settlement>): Boolean = trustedSettlements.contains(id)
	fun isNationTrusted(id: Oid<Nation>): Boolean = trustedNations.contains(id)

	fun trustPlayer(player: SLPlayerId): UpdateResult = companion.trustPlayer(databaseId, player)
	fun trustSettlement(id: Oid<Settlement>): UpdateResult = companion.trustSettlement(databaseId, id)
	fun trustNation(id: Oid<Nation>): UpdateResult = companion.trustNation(databaseId, id)

	fun unTrustPlayer(player: SLPlayerId): UpdateResult = companion.unTrustPlayer(databaseId, player)
	fun unTrustSettlement(id: Oid<Settlement>): UpdateResult = companion.unTrustSettlement(databaseId, id)
	fun unTrustNation(id: Oid<Nation>): UpdateResult = companion.unTrustNation(databaseId, id)
	fun changeTrustLevel(level: SpaceStations.TrustLevel): UpdateResult = companion.setTrustLevel(databaseId, level)

	fun abandon() = companion.delete(databaseId)

	fun invalidate(recreate: Boolean = true) {
		val database = companion.findById(databaseId) ?: return

		SpaceStations.invalidate(database)

		if (recreate) SpaceStations.createCached(database)
	}

	companion object {
		fun calculateCost(oldRadius: Int, newRadius: Int): Int {
			/*  A_1 = pi * r^2
					A_2 = pi * r_f^2
					dA = A_2-A_1
					dA = pi * r_f^2 - pi * r^2
					dA = pi * (r_f^2 - r^2) */
			val deltaArea = Math.PI * (newRadius.squared() - oldRadius.squared())
			return (deltaArea * NATIONS_BALANCE.nation.costPerSpaceStationBlock).roundToInt()
		}
	}
}

class CachedNationSpaceStation(
	override val databaseId: Oid<NationSpaceStation>,
	override val owner: Oid<Nation>,

	override val name: String,

	override var world: String,
	override var x: Int,
	override var z: Int,
	override var radius: Int,

	override var trustedPlayers: Set<SLPlayerId>,
	override var trustedSettlements: Set<Oid<Settlement>>,
	override var trustedNations: Set<Oid<Nation>>,
	override var trustLevel: SpaceStations.TrustLevel,
) : CachedSpaceStation<NationSpaceStation, Nation, NationSpaceStation.Companion>() {
	override val companion = NationSpaceStation.Companion
	override val ownerName: String get() = NationCache[owner].name
	override val ownershipType: String = "Nation"

	override val color: Int get() = NationCache[owner].color

	override fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission) =
		NationRole.hasPermission(player, permission.nation)

	override fun hasOwnershipContext(player: SLPlayerId): Boolean = PlayerCache[player].nationOid == owner
}

class CachedSettlementSpaceStation(
	override val databaseId: Oid<SettlementSpaceStation>,
	override val owner: Oid<Settlement>,

	override val name: String,

	override var world: String,
	override var x: Int,
	override var z: Int,
	override var radius: Int,

	override var trustedPlayers: Set<SLPlayerId>,
	override var trustedSettlements: Set<Oid<Settlement>>,
	override var trustedNations: Set<Oid<Nation>>,
	override var trustLevel: SpaceStations.TrustLevel,
) : CachedSpaceStation<SettlementSpaceStation, Settlement, SettlementSpaceStation.Companion>() {
	override val companion = SettlementSpaceStation.Companion
	override val ownerName: String get() = SettlementCache[owner].name
	override val ownershipType: String = "Settlement"

	override val color: Int = Color.BLUE.asRGB()

	override fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission) =
		SettlementRole.hasPermission(player, permission.settlement)

	override fun hasOwnershipContext(player: SLPlayerId): Boolean = PlayerCache[player].settlementOid == owner
}

class CachedPlayerSpaceStation(
	override val databaseId: Oid<PlayerSpaceStation>,
	override val owner: SLPlayerId,

	override val name: String,

	override var world: String,
	override var x: Int,
	override var z: Int,
	override var radius: Int,

	override var trustedPlayers: Set<SLPlayerId>,
	override var trustedSettlements: Set<Oid<Settlement>>,
	override var trustedNations: Set<Oid<Nation>>,
	override var trustLevel: SpaceStations.TrustLevel,
) : CachedSpaceStation<PlayerSpaceStation, SLPlayer, PlayerSpaceStation.Companion>() {
	override val companion = PlayerSpaceStation.Companion
	override val ownerName = Bukkit.getPlayer(owner.uuid)?.name ?: SLPlayer.getName(owner) ?: error("No such player $owner")
	override val ownershipType: String = "Player"

	override val color: Int = Color.WHITE.asRGB()

	override fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission) = owner == player

	override fun hasOwnershipContext(player: SLPlayerId): Boolean = player == owner
}
