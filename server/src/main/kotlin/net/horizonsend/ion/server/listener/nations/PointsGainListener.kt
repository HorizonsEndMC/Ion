package net.horizonsend.ion.server.listener.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.extensions.success
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
import org.bukkit.event.entity.EntityDeathEvent

object PointsGainListener: SLEventListener() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	@EventHandler
	fun onPlayerDeath(entity: EntityDeathEvent) {
		if (entity.entity !is Player || entity.entity.killer !is Player) return
		val victim = entity.entity as Player
		val killer = entity.entity as Player
		val killerNationId: Oid<FrontierNation> = SLPlayer[killer.name]?.frontierNation ?: return
		val victimNationId: Oid<FrontierNation> = SLPlayer[victim.name]?.frontierNation ?: return
		if (killerNationId == victimNationId) return
		FrontierNation.updatePoints(killerNationId, 1)
		killer.success("Gained 1 point")
	}

	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val victim = event.starship
		val controller = event.previousController as? PlayerController ?: return
		val damagers = event.starship.damagers
			.filter { it.key is PlayerDamager }
			.filter { SLPlayer[(it.key as PlayerDamager).player.name]?.frontierNation != SLPlayer[controller.player.name]?.frontierNation }
		val highestDamager = damagers
			.maxByOrNull { it.value.points.get() }?.key as? PlayerDamager ?: return

		var points = when {
			victim.initialBlockCount <= 4000 -> 3
			victim.initialBlockCount in 4001..12000 -> 5
			victim.initialBlockCount > 12000 -> 10
			else -> 1 //Shouldn't happen
		}
		//2x if a tech 2 ship, 2x if in a siege ring
		if (victim.type.tech2) points *= 2

		for (koth: KingOfTheHills.Koths in getKOTHS()) {
			val thisKoth = koth.kothId
			val kothRegion: RegionKothZone = Regions[thisKoth]
			val world: World = Bukkit.getWorld(kothRegion.world) ?: return
			if (kothRegion.contains(victim.centerOfMass.toLocation(world))) points *= 2
		}

		val killerNationId: Oid<FrontierNation>? = SLPlayer[highestDamager.player.name]?.frontierNation
		val victimNationId: Oid<FrontierNation>? = SLPlayer[controller.player.name]?.frontierNation
		if (killerNationId != null /*&& victimNationId != killerNationId*/) {
			FrontierNation.updatePoints(killerNationId, points)
			highestDamager.player.success("Gained $points points")
		}

		for (damager in damagers) {
			if (damager.key.starship?.controller !is PlayerController) continue
			val controller = damager.key.starship!!.controller as PlayerController
			val player = controller.player
			val frontierNation = SLPlayer[player.name]?.frontierNation
			if (frontierNation == null /*|| frontierNation == victimNationId*/) continue
			else {
				FrontierNation.updatePoints(frontierNation, 1)
				player.success("Gained 1 point")
			}
		}
	}
}
