package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.get
import net.horizonsend.ion.server.database.int
import net.horizonsend.ion.server.database.nullable
import net.horizonsend.ion.server.database.oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.database.schema.nations.territories.SiegeTerritory
import net.horizonsend.ion.server.database.string
import net.minecraft.core.BlockPos
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.util.Tasks
import net.starlegacy.util.component1
import net.starlegacy.util.component2
import net.starlegacy.util.component3
import net.starlegacy.util.d
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.squared
import org.bukkit.Material
import org.bukkit.entity.Player

class RegionSiegeBeacon(beacon: SiegeBeacon) : Region<SiegeBeacon>(beacon), RegionTopLevel {
	override val priority: Int = 0
	var name: String = beacon.name; private set

	var siegeTerritory: Oid<SiegeTerritory> = beacon.siegeTerritory

	override var world: String = beacon.world
	var x: Int = beacon.x; private set
	var y: Int = beacon.y; private set
	var z: Int = beacon.z; private set

	var blocks: LongArray = beacon.blocks

	var points: Int = beacon.points

	var owner: Oid<Nation>? = beacon.owner
	var attacker: Oid<Nation> = beacon.attacker

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		val radiusSquared = SiegeBeacon.BEACON_DETECTION_RADIUS.squared()
		return distanceSquared(x.d(), 0.0, z.d(), this.x.d(), 0.0, this.z.d()) <= radiusSquared
	}

	override fun onCreate() {
		NationsMap.addSiegeBeacon(this)
		super.onCreate()
	}

	override fun update(delta: ChangeStreamDocument<SiegeBeacon>) {
		delta[SiegeBeacon::world]?.let { world = it.string() }
		delta[SiegeBeacon::name]?.let { name = it.string() }
		delta[SiegeBeacon::owner]?.let { owner = it.nullable()?.oid() }
		delta[SiegeBeacon::attacker]?.let { attacker = it.oid() }
		delta[SiegeBeacon::x]?.let { x = it.int() }
		delta[SiegeBeacon::y]?.let { y = it.int() }
		delta[SiegeBeacon::z]?.let { z = it.int() }
		delta[SiegeBeacon::points]?.let { points = it.int() }

		NationsMap.updateSiegeBeacon(this)
	}

	override fun onDelete() {
		NationsMap.removeSiegeBeacon(this)

		val bukkitWorld = bukkitWorld ?: return
		val air = Material.AIR.createBlockData()

		Tasks.sync {
			// Delete the beacon to prevent regen
			for (block in blocks) {
				val blockpos = BlockPos.of(block)
				val (x, y, z) = blockpos

				bukkitWorld.setBlockData(x, y, z, air)
			}

			// Explode beacon location upon deletion
			bukkitWorld.createExplosion(
				x.toDouble(),
				y.toDouble(),
				z.toDouble(),
				20F,
				true,
				true,
			)
		}
	}

	override fun calculateInaccessMessage(player: Player): String? {
		return null
	}
}
