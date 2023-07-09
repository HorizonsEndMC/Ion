package net.horizonsend.ion.common.database.schema.nations.spacestation

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.objId
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import org.litote.kmongo.Id
data class PlayerSpaceStation(
    override val _id: Oid<PlayerSpaceStation>,
    override var owner: Id<SLPlayer>,

    override var name: String,
    override var world: String,
    override var x: Int,
    override var z: Int,
    override var radius: Int,

    override var trustedPlayers: Set<SLPlayerId>,
    override var trustedSettlements: Set<Oid<Settlement>>,
    override var trustedNations: Set<Oid<Nation>>,

    override var trustLevel: SpaceStationCompanion.TrustLevel
) : SpaceStationInterface<SLPlayer> {

	companion object : SpaceStationCompanion<SLPlayer, PlayerSpaceStation>(
		PlayerSpaceStation::class,
		PlayerSpaceStation::owner,
		PlayerSpaceStation::name,
		PlayerSpaceStation::world,
		PlayerSpaceStation::x,
		PlayerSpaceStation::z,
		PlayerSpaceStation::radius,
		PlayerSpaceStation::trustedPlayers,
		PlayerSpaceStation::trustedSettlements,
		PlayerSpaceStation::trustedNations,
		PlayerSpaceStation::trustLevel,
	) {
		override fun new(
			owner: Id<SLPlayer>,
			name: String,
			world: String,
			x: Int,
			z: Int,
			radius: Int,
			trustLevel: TrustLevel,
		): PlayerSpaceStation {
			val id = objId<PlayerSpaceStation>()

			return PlayerSpaceStation(
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
