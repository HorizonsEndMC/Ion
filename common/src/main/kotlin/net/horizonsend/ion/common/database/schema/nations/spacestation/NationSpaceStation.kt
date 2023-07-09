package net.horizonsend.ion.common.database.schema.nations.spacestation

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import org.litote.kmongo.Id

data class NationSpaceStation(
    override val _id: Oid<NationSpaceStation>,
    override var owner: Id<Nation>,

    override var name: String,
    override var world: String,
    override var x: Int,
    override var z: Int,
    override var radius: Int,

    override var trustedPlayers: Set<SLPlayerId>,
    override var trustedSettlements: Set<Oid<Settlement>>,
    override var trustedNations: Set<Oid<Nation>>,

    override var trustLevel: SpaceStationCompanion.TrustLevel
) : SpaceStationInterface<Nation> {

	companion object : SpaceStationCompanion<Nation, NationSpaceStation>(
		NationSpaceStation::class,
		NationSpaceStation::owner,
		NationSpaceStation::name,
		NationSpaceStation::world,
		NationSpaceStation::x,
		NationSpaceStation::z,
		NationSpaceStation::radius,
		NationSpaceStation::trustedPlayers,
		NationSpaceStation::trustedSettlements,
		NationSpaceStation::trustedNations,
		NationSpaceStation::trustLevel
	) {
		override fun new(
			owner: Id<Nation>,
			name: String,
			world: String,
			x: Int,
			z: Int,
			radius: Int,
			trustLevel: TrustLevel,
		): NationSpaceStation {
			val id = objId<NationSpaceStation>()

			return NationSpaceStation(
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
