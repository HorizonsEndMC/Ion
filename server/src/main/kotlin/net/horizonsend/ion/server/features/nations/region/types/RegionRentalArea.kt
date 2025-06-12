package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.document
import net.horizonsend.ion.common.database.double
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.long
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.economy.StationRentalArea
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.spacestation.NPCSpaceStation
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.server.features.economy.misc.StationRentalAreas
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.setValue

class RegionRentalArea(zone: StationRentalArea) : Region<StationRentalArea>(zone) {
	override val priority: Int = 1

	override val world: String = zone.world

	var station: Oid<NPCSpaceStation> = zone.station; private set

	var signLocation: Vec3i = Vec3i(zone.signLocation); private set
	var minPoint: Vec3i = Vec3i(zone.minPoint); private set
	var maxPoint: Vec3i = Vec3i(zone.maxPoint); private set

	var name: String = zone.name; private set

	var owner: SLPlayerId? = zone.owner; private set
	var trustedPlayers: Set<SLPlayerId> = zone.trustedPlayers; private set
	var trustedSettlements: Set<Oid<Settlement>> = zone.trustedSettlements; private set
	var trustedNations: Set<Oid<Nation>> = zone.trustedNations; private set
	var collectRentFromOwnerBalance: Boolean = zone.collectRentFromOwnerBalance; private set

	var rent: Double = zone.rent; private set
	var rentBalance: Double = zone.rentBalance; private set
	var rentLastCharged: Long = zone.rentLastCharged; private set

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		if (x > maxPoint.x || x < minPoint.x) return false
		if (y > maxPoint.y || y < minPoint.y) return false
		return !(z > maxPoint.z || z < minPoint.z)
	}

	override fun update(delta: ChangeStreamDocument<StationRentalArea>) {
		delta[StationRentalArea::name]?.let { name = it.string() }

		delta[StationRentalArea::station]?.let { station = it.oid() }

		delta[StationRentalArea::signLocation]?.let { signLocation = Vec3i(it.document<DBVec3i>()) }
		delta[StationRentalArea::minPoint]?.let { minPoint = Vec3i(it.document<DBVec3i>()) }
		delta[StationRentalArea::maxPoint]?.let { maxPoint = Vec3i(it.document<DBVec3i>()) }

		delta[StationRentalArea::owner]?.let { owner = it.nullable()?.slPlayerId() }
		delta[StationRentalArea::trustedPlayers]?.let { trustedPlayers = it.mappedSet { entry -> entry.slPlayerId() } }
		delta[StationRentalArea::trustedSettlements]?.let { trustedSettlements = it.mappedSet { entry -> entry.oid() } }
		delta[StationRentalArea::trustedNations]?.let { trustedNations = it.mappedSet { entry -> entry.oid() } }
		delta[StationRentalArea::collectRentFromOwnerBalance]?.let { collectRentFromOwnerBalance = it.boolean() }

		delta[StationRentalArea::rent]?.let { rent = it.double() }
		delta[StationRentalArea::rentBalance]?.let { rentBalance = it.double() }
		delta[StationRentalArea::rentLastCharged]?.let { rentLastCharged = it.long() }

		StationRentalAreas.refreshSign(this)
	}

	override fun onDelete() {
		val owner = this.owner ?: return
		// Return balance
		Tasks.sync {
			VAULT_ECO.depositPlayer(Bukkit.getOfflinePlayer(owner.uuid), rentBalance)
		}

		getParentRegion().children.remove(this)
	}

	init {
		getParentRegion().children.add(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		if (player.slPlayerId != owner) return "You don't own this zone!".intern()
		return null
	}

	override fun onCreate() {
		StationRentalAreas.refreshSign(this)
	}

	fun getParentRegion(): RegionNPCSpaceStation = Regions[station]

	fun setCollectRentFromBalance(newValue: Boolean) {
		collectRentFromOwnerBalance = newValue
		Tasks.async { StationRentalArea.updateById(id, setValue(StationRentalArea::collectRentFromOwnerBalance, newValue)) }
	}
}

