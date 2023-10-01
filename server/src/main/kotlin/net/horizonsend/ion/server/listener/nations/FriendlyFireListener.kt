package net.horizonsend.ion.server.listener.nations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.LegacySettings
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Bukkit
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
		if (LegacySettings.allowFriendlyFire) {
			return false
		}

		val damagedData = PlayerCache[damaged]
		val damagedSettlement = damagedData.settlementOid ?: return false

		val damagerData = PlayerCache[damager]
		val damagerSettlement = damagerData.settlementOid ?: return false

		if (damagedSettlement == damagerSettlement) {
			return true
		}

		val damagedNation: Oid<Nation> = damagedData.nationOid ?: return false
		val damagerNation: Oid<Nation> = damagerData.nationOid ?: return false

		return RelationCache[damagedNation, damagerNation].ordinal >= 5
	}
}
