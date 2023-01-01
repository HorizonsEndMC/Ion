package net.starlegacy.listener.nations

import net.starlegacy.SETTINGS
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.listener.SLEventListener
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.litote.kmongo.eq

object FriendlyFireListener : SLEventListener() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	@EventHandler()
	fun onDamage(event: EntityDamageByEntityEvent) {
		if (event.damager.type != EntityType.PLAYER || event.entity.type != EntityType.PLAYER) {
			return
		}

		// don't check for NPCs
		if (Bukkit.getPlayer(event.entity.name) == null) {
			return
		}

		val damaged = event.entity as? Player ?: return
		val damager = event.damager as? Player ?: return

		if (isFriendlyFire(damaged, damager)) {
			event.isCancelled = true
		}
	}

	fun isFriendlyFire(damaged: Player, damager: Player): Boolean {
		if (SETTINGS.allowFriendlyFire) {
			return false
		}

		val damagedData = PlayerCache[damaged]
		val damagedSettlement = damagedData.settlement ?: return false

		val damagerData = PlayerCache[damager]
		val damagerSettlement = damagerData.settlement ?: return false

		if (damagedSettlement == damagerSettlement) {
			return true
		}

		val damagedNation: Oid<Nation> = damagedData.nation ?: return false
		val damagerNation: Oid<Nation> = damagerData.nation ?: return false
		for (relation in NationRelation.find(NationRelation::nation eq damagedNation)) {
			if (relation.other == damagerNation && (relation.actual == NationRelation.Level.ALLY) || (damagedNation == damagerNation)) {
				return true
			}
		}

		return false
	}
}
