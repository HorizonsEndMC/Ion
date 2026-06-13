package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

object WreckChestListener : SLEventListener() {
	@EventHandler
	fun onBreakWreckChest(event: BlockBreakEvent) {
		val chest = event.block.state as? Chest ?: return
		if (!chest.persistentDataContainer.has(NamespacedKeys.WRECK_CHEST, PersistentDataType.BOOLEAN)) return
		event.isCancelled = true
		event.player.sendMessage(text("This wreck chest cannot be broken!", NamedTextColor.RED))
	}

	@EventHandler
	fun onOpenWreckChest(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		val block = event.clickedBlock ?: return
		if (block.type != Material.CHEST) return
		val chest = block.state as? Chest ?: return

		if (!chest.persistentDataContainer.has(NamespacedKeys.WRECK_CHEST, PersistentDataType.BOOLEAN)) return

		event.isCancelled = true
		event.player.world.playSound(Sound.sound(key("horizonsend:wrecks.hacking.open"), Sound.Source.PLAYER, 5.0f, 1.0f), event.player)
		WreckHackingGui(event.player, chest).openGui()
	}
}
