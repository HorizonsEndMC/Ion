package net.horizonsend.ion.server.database.schema.nations.spacestation

import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.objId
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.features.spacestations.SpaceStations
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

	override var trustLevel: SpaceStations.TrustLevel
) : SpaceStation<SLPlayer> {

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
			trustLevel: SpaceStations.TrustLevel,
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
