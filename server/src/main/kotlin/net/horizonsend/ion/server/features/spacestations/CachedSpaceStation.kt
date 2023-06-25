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
import net.horizonsend.ion.server.database.schema.nations.spacestation.SpaceStation
import net.horizonsend.ion.server.database.schema.nations.spacestation.SpaceStationCompanion
import net.horizonsend.ion.server.database.uuid
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.util.squared
import org.bukkit.Bukkit
import org.litote.kmongo.Id
import kotlin.math.roundToInt

abstract class CachedSpaceStation<T: SpaceStation<O>, O: DbObject, C: SpaceStationCompanion<O, T>> {
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

	abstract fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission): Boolean

	fun setLocation(x: Int, z: Int, world: String) {
		this.x = x
		this.z = z
		this.world = world

		companion.setLocation(databaseId, x, z, world)
	}
	fun setRadius(newRadius: Int) {
		companion.setRadius(databaseId, newRadius)
	}

	fun isTrusted(id: SLPlayerId): Boolean = trustedPlayers.contains(id)
	fun isTrusted(id: Oid<Settlement>): Boolean = trustedSettlements.contains(id)
	fun isTrusted(id: Oid<Nation>): Boolean = trustedNations.contains(id)

	fun trustPlayer(player: SLPlayerId): UpdateResult = companion.trustPlayer(databaseId, player)
	fun trustSettlement(id: Oid<Settlement>): UpdateResult = companion.trustSettlement(databaseId, id)
	fun trustNation(id: Oid<Nation>): UpdateResult = companion.trustNation(databaseId, id)
	fun unTrustPlayer(player: SLPlayerId): UpdateResult = companion.unTrustPlayer(databaseId, player)
	fun unTrustSettlement(id: Oid<Settlement>): UpdateResult = companion.unTrustSettlement(databaseId, id)
	fun unTrustNation(id: Oid<Nation>): UpdateResult = companion.unTrustNation(databaseId, id)

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

	override fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission) =
		NationRole.hasPermission(player, permission.nation)
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

	override fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission) =
		SettlementRole.hasPermission(player, permission.settlement)
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

	override fun hasPermission(player: SLPlayerId, permission: SpaceStationPermission) = owner == player
}
