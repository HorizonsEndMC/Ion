package net.horizonsend.ion.server.listener.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionKothZone
import net.horizonsend.ion.server.features.nations.sieges.KingOfTheHills
import net.horizonsend.ion.server.features.nations.sieges.KingOfTheHills.getKOTHS
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent

object PointsGainListener: SLEventListener() {

	@EventHandler
	fun onPlayerDeath(entity: PlayerDeathEvent) {
		if (entity.player.killer !is Player) return
		val victim = entity.player
		val killer = entity.player.killer?: return
		val killerNationId: Oid<FrontierNation> = PlayerCache[killer].frontierNationOid ?: return
		val victimNationId: Oid<FrontierNation> = PlayerCache[victim].frontierNationOid ?: return
		if (killerNationId == victimNationId) return
		FrontierNation.updatePoints(killerNationId, 1)
		killer.success("Gained 1 point")
	}

	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val victim = event.starship
		val controller = event.previousController as? PlayerController ?: return
		val controllerId = controller.player
		val damagers = victim.damagers
			.filter { it.key is PlayerDamager }

		val highestDamager = damagers
			.maxByOrNull { it.value.points.get() }?.key as? PlayerDamager ?: return
		val highestDamagerId = highestDamager.player

		var points = when {
			victim.initialBlockCount <= 4000 -> 3
			victim.initialBlockCount in 4001..12000 -> 5
			else -> 10
		}
		//2x if a tech 2 ship, 2x if in a siege ring
		if (victim.type.tech2) points *= 2

		for (koth: KingOfTheHills.Koths in getKOTHS()) {
			val thisKoth = koth.kothId
			val kothRegion: RegionKothZone = Regions[thisKoth]
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return
			if (kothRegion.contains(victim.centerOfMass.toLocation(world))) points *= 2
		}

		val killerNationId: Oid<FrontierNation>? = PlayerCache[highestDamagerId].frontierNationOid
		val victimNationId: Oid<FrontierNation>? = PlayerCache[controllerId].frontierNationOid
		if (killerNationId != null && victimNationId != killerNationId) {
			FrontierNation.updatePoints(killerNationId, points)
			highestDamager.player.success("Gained $points points")
		}

		for (damager in damagers) {
			if (damager.key.starship?.controller !is PlayerController) continue
			val controller = damager.key.starship?.controller as PlayerController
			val player = controller.player
			if (player == highestDamagerId) continue
			val frontierNation = PlayerCache[player].frontierNationOid
			if (frontierNation == null || frontierNation == victimNationId) continue
			else {
				FrontierNation.updatePoints(frontierNation, 1)
				player.success("Gained 1 point")
			}
		}
	}
}
