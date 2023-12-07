package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.specialAction
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Material
import org.bukkit.block.Skull
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.System.currentTimeMillis
import java.util.UUID

@Suppress("Unused")
class HeadListener : SLEventListener() {
	private val coolDowns = mutableMapOf<UUID, Long>()

	@EventHandler
	@Suppress("Unused")
	fun onHeadClick(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return

		val block = event.clickedBlock?.state as? Skull ?: return
		val skullPlayer = SLPlayer[block.owningPlayer?.uniqueId?.slPlayerId ?: return] ?: return
		val clickerPlayer = SLPlayer[event.player.slPlayerId] ?: return

		val relationColor = if (skullPlayer.nation == null || clickerPlayer.nation == null) "" else
			"<${RelationCache[
				clickerPlayer.nation!!,
				skullPlayer.nation!!
			].textStyle}>"

		event.player.specialAction("This head belonged to $relationColor${skullPlayer.lastKnownName}.")
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		val victim = event.player

//		if (NPCFakePilot.isFakePilot(victim)) return

		// Player Head Drops
		val headDropCooldownEnd = coolDowns.getOrDefault(victim.uniqueId, 0)
		coolDowns[victim.uniqueId] = currentTimeMillis() + 1000 * 60 * 60
		if (headDropCooldownEnd > currentTimeMillis()) return

		val head = ItemStack(Material.PLAYER_HEAD)
		head.editMeta(SkullMeta::class.java) {
			it.owningPlayer = victim
		}

		event.entity.world.dropItem(victim.location, head)
		// Skulls end
	}
}
