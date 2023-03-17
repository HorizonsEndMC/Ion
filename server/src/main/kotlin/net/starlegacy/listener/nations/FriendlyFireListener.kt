package net.starlegacy.listener.nations

import net.starlegacy.SETTINGS
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.listener.SLEventListener
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent

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

		if (NationRelation.getRelationActual(damagedNation, damagerNation).ordinal >= 5) {
			return true
		}

		return false
	}

	fun isFriendlyFire(damaged: OfflinePlayer, damager: OfflinePlayer): Boolean {
		if (SETTINGS.allowFriendlyFire) {
			return false
		}

		val damagedData = SLPlayer[damaged.uniqueId]!!
		val damagedSettlement = damagedData.settlement ?: return false

		val damagerData = SLPlayer[damager.uniqueId]!!
		val damagerSettlement = damagerData.settlement ?: return false

		if (damagedSettlement == damagerSettlement) {
			return true
		}

		val damagedNation: Oid<Nation> = damagedData.nation ?: return false
		val damagerNation: Oid<Nation> = damagerData.nation ?: return false

		if (NationRelation.getRelationActual(damagedNation, damagerNation).ordinal >= 5) {
			return true
		}

		return false
	}
}
