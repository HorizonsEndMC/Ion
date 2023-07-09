package net.horizonsend.ion.common.database.schema.nations.spacestation

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import org.litote.kmongo.Id

data class SettlementSpaceStation(
    override val _id: Oid<SettlementSpaceStation>,
    override var owner: Id<Settlement>,

    override var name: String,
    override var world: String,
    override var x: Int,
    override var z: Int,
    override var radius: Int,

    override var trustedPlayers: Set<SLPlayerId>,
    override var trustedSettlements: Set<Oid<Settlement>>,
    override var trustedNations: Set<Oid<Nation>>,

    override var trustLevel: SpaceStationCompanion.TrustLevel
) : SpaceStationInterface<Settlement> {

	companion object : SpaceStationCompanion<Settlement, SettlementSpaceStation>(
		SettlementSpaceStation::class,
		SettlementSpaceStation::owner,
		SettlementSpaceStation::name,
		SettlementSpaceStation::world,
		SettlementSpaceStation::x,
		SettlementSpaceStation::z,
		SettlementSpaceStation::radius,
		SettlementSpaceStation::trustedPlayers,
		SettlementSpaceStation::trustedSettlements,
		SettlementSpaceStation::trustedNations,
		SettlementSpaceStation::trustLevel,
	) {
		override fun new(
			owner: Id<Settlement>,
			name: String,
			world: String,
			x: Int,
			z: Int,
			radius: Int,
			trustLevel: TrustLevel,
		): SettlementSpaceStation {
			val id = objId<SettlementSpaceStation>()

			return SettlementSpaceStation(
				id,
				owner = owner,
				name = name,
				world = world,
				x = x,
				z = z,
				radius = radius,
				trustLevel = trustLevel,
				trustedPlayers = setOf(),
				trustedSettlements = setOf(),
				trustedNations = setOf(),
			)
		}
	}
}
