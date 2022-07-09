package net.horizonsend.ion.server.customitems

import net.horizonsend.ion.common.database.PlayerData
import org.bukkit.Color
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

abstract class Blaster : CustomItem() {
	protected fun getCosmeticColor(entity: LivingEntity): Color = Color.fromRGB(
		(entity as? Player)?.run {
			if (hasPermission("ion.cosmetic.color")) transaction { PlayerData.findById(uniqueId)?.cosmeticColor } else null
		} ?: 0xff7f3f
	)
}